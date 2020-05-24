package com.buaa.blockchain.core;


import com.buaa.blockchain.consensus.BaseConsensus;
import com.buaa.blockchain.consensus.SBFTConsensusImpl;
import com.buaa.blockchain.crypto.HashUtil;
import com.buaa.blockchain.entity.Block;
import com.buaa.blockchain.entity.Message;
import com.buaa.blockchain.entity.Times;
import com.buaa.blockchain.entity.Transaction;
import com.buaa.blockchain.exception.ShutDownManager;
import com.buaa.blockchain.mapper.BlockMapper;
import com.buaa.blockchain.mapper.TransactionMapper;
import com.buaa.blockchain.message.MessageCallBack;
import com.buaa.blockchain.message.MessageService;
import com.buaa.blockchain.txpool.RedisTxPool;
import com.buaa.blockchain.txpool.TxPool;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 区块链的核心流程，用于接收peer的消息，并且进行状态转换
 * 状态转移图如下：
 * firstTimeSetup --> (startNewRound ~~> verifyBlock ~~> storeBlock --> startNewRound ...)
 * 这里规定，共识协议之间的状态转换只能通过协议接口，区块链状态转换只能通过区块链接口
 * 比如sbftVoteBroadcastReceived只能转换到sbftExecute，在sbftExecute中调用的storeBlock可以转换到startNewRound
 *     但是不推荐在sbftVoteBroadcastReceived中直接出现storeBlock或者startNewRound
 *
 * @author hitty
 * */
@Slf4j
@Component
@MapperScan(basePackages ="com.buaa.blockchain.mapper")
@ComponentScan(basePackages = "com.buaa.blockchain.*")
public class BlockchainServiceImpl implements BlockchainService {

    /* 消息服务 */
    final MessageService messageService;
    /* 交易池 */
    final RedisTxPool redisTxpool;
    /* Block的持久化 */
    final BlockMapper blockMapper;
    /* Transaction的持久化 */
    final TransactionMapper transactionMapper;
    /* 投票处理 */
    final VoteHandler voteHandler;
    /* 状态树 */
    final WorldState worldState;
    /* 超时管理 */
    final TimeoutHelper timeoutHelper;
    /* 关闭管理 */
    final ShutDownManager shutDownManager;
    /*********************** 属性字段 ***********************/

    /* 节点名 */
    @Value("${buaa.blockchain.nodename}")
    public String nodeName;
    /* 节点公钥 */
    @Value("${buaa.blockchain.sign}")
    private String nodeSign;
    /* 主节点轮询交易池的间隔时间（毫秒） */
    @Value("${buaa.blockchain.round-sleeptime}")
    private int sleepTime;
    /* 数据摘要生成算法名 */
    @Value("${buaa.blockchain.hash-algorithm}")
    private String hashAlgorithm;
    /* 区块链版本 */
    @Value("${buaa.blockchain.version}")
    private String version;
    /* 共识协议名称 */
    @Value("${buaa.blockchain.consensus}")
    private String consensusType;
    /* 共识协议 */
    private BaseConsensus consensus = null;


    // 运行时
    // 集群大小
    public int clusterSize = 1;
    // 当前轮的高度（尚未做块）
    public int height = 1;
    // 当前轮数
    public int round = 0;
    public String preHash="";
    // 同步器，用于超时管理
    public CountDownLatch countDownLatch;

    @Autowired
    public BlockchainServiceImpl(MessageService messageService, RedisTxPool redisTxpool, BlockMapper blockMapper,
                                 TransactionMapper transactionMapper, VoteHandler voteHandler, WorldState worldState, TimeoutHelper timeoutHelper, ShutDownManager shutDownManager) {
        this.messageService = messageService;
        this.redisTxpool = redisTxpool;
        this.blockMapper = blockMapper;
        this.transactionMapper = transactionMapper;
        this.voteHandler = voteHandler;
        this.worldState = worldState;
        this.timeoutHelper = timeoutHelper;
        this.shutDownManager = shutDownManager;
    }


    /************* 区块链服务接口 *************/

    /**
     * 入口，用于建立区块链服务
     * */
    @Override
    public void firstTimeSetup(){
        // 初始化共识
        initConsensus();
        // 初始化数据摘要工具
        testMessageDigest(this.hashAlgorithm);
        log.info("firstTimeSetup(): init complete. buaa-blockchain version:"+this.version);
        // 判断是否为第一次启动
        if(blockMapper.findBlockNum(HashUtil.sha256("0")) == 0){
            Block block = generateFirstBlock();
            blockMapper.insertBlock(block);
            log.info("firstTimeSetup(): Generate first block complete.");
        }else{
            // 不是第一次启动
            int maxHeight = blockMapper.findMaxHeight();
            String maxHeightStateRoot =  blockMapper.findStatRoot(maxHeight);
            if(!worldState.switchRoot(maxHeightStateRoot)){
                log.error("firstTimeSetup(): cannot sync data between block and state! Shut down!");
                shutDownManager.shutDown();
            }
        }

        // 同步当前的height和pre_hash
        this.height = blockMapper.findMaxHeight() + 1;
        this.round = 0;
        this.preHash = blockMapper.findPreHashByHeight(this.height - 1);
        this.clusterSize = messageService.getClusterAddressList().size();
        // 以本地的区块信息，开始新一轮的做块
        startNewRound(this.height,this.round);
    }

    /**
     * 新一轮的做块
     * @param height 区块高度
     * @param round 轮数
     * */
    @Override
    public void startNewRound(int height, int round) {
        if(isSelfLeader(height,round)){
            log.info("startNewRound(): leaderNode="+this.nodeName+", height="+height+", round="+round+" try to build new block...");
            // 轮询交易池中是否有可以新的可以做块的交易
            while(true){
                if(!isSelfLeader(height,round)){
                    startNewRound(height,round);
                    return ;
                }
                if(redisTxpool.size(TxPool.TXPOOL_LABEL_TRANSACTION) < 1){
                    try{
                        log.info("startNewRound(): no transaction found in txpool, wait for "+this.sleepTime+"ms.");
                        Thread.sleep(this.sleepTime);
                        continue;
                    }catch (Exception e){
                        shutDownManager.shutDown();
                        // TODO 处理异常
                    }
                }else{
                    // 交易池非空，跳出循环
                    break;
                }
            }
            // 开始做块
            Block block = createNewBlock(height,round);
            if(null != block){
                // 开始共识
                Message message = new Message(this.nodeName,height,round,block);
                this.consensus.setup(message);
            }else{
                log.info("startNewRound(): leader node="+nodeName+" fail to create new block at height="+height+", round="+round+".");
                startNewRound(height,round);
            }
        }else{
            log.info("startNewRound(): not leader node, waiting for leader or timeout");
        }
    }


    @Override
    public void startNewRound() {
        startNewRound(this.height,0);
    }

    @Override
    public synchronized void storeBlock(Block block) {
        log.warn("storeBlock(): block="+block.toString());
        // 检查交易数量
        if(block.getTx_length() < 1){
            log.info("storeBlock(): get Block blockhash="+block.getHash()+" with no transation, storeBlock stop.");
            return ;
        }
        // 执行交易
        List<Transaction> transactions = block.getTrans();
        TxExecuter.baseExecute(transactions,worldState);
        // 存储当前状态树根的值到block中
        block.setState_root(worldState.getRootHash());
        // 处理交易池
        for(Transaction ts : transactions){
            if(null == redisTxpool.get(TxPool.TXPOOL_LABEL_TRANSACTION,ts.getTran_hash())){
                // 本地交易池中没有此交易，认为是还没有收到（可能是网络时延）
                redisTxpool.put(TxPool.TXPOOL_LABEL_DEL_TRANSACTION,ts.getTran_hash(),ts);
            }else{
                // 本地交易池存在该交易，删除该交易
                redisTxpool.delete(TxPool.TXPOOL_LABEL_TRANSACTION,ts.getTran_hash());
            }
            ts.setBlock_hash(block.getHash());
        }
        // 持久化交易和区块
        worldState.sync();
        blockMapper.insertBlock(block);
        log.info("storeBlock(): Done! block="+block.toString());
        // 通知
        this.timeoutHelper.notified(this.height,this.round);
        // 同步
        this.height = block.getHeight() + 1;
        this.round = 0;
        this.preHash = block.getHash();

    }

    /**
     * 验证区块是否合法
     * 当前实现中，检查了区块高度、前区块hash、区块merkle树根、区块头部hash。任何一个出错即返回false
     * */
    @Override
    public boolean verifyBlock(Block block, int height, int round) {
        try{
            Block rawBlock = (Block) block;
            // 检查height
            if(height - 1 < blockMapper.findMaxHeight()){
                log.info("verifyBlock(): height="+height+" has already recorded in database!");
                return false;
            }
            // 检查preHash
            String preHash = blockMapper.findPreHashByHeight(height - 1);
            if(!rawBlock.getPre_hash().equals(preHash)) {
                log.info("verifyBlock(): wrong preHash! Block preHash="+rawBlock.getPre_hash()+", database prehash="+preHash);
                return false;
            }
            // 检查状态树，仍旧是和mysql数据库中的表项做对比
            String preStateHash = blockMapper.findStatRoot(height - 1);
            if(!rawBlock.getPre_state_root().equals(preStateHash)){
                log.info("verifyBlock(): wrong preStateHash! Block preStateHash="+rawBlock.getPre_state_root()+", database preStateHash="+preStateHash);
                return false;
            }

            // 检查交易merkleRoot
            List<Transaction> transactionList = rawBlock.getTrans();
            Collections.sort(transactionList);
            String merkleRoot = getMerkleRoot(transactionList);
            if(null == merkleRoot || null == rawBlock.getMerkle_root()){
                log.info("verifyBlock(): Block merkle-root is null!");
                return false;
            }
            if(!merkleRoot.equals(rawBlock.getMerkle_root())){
                    log.info("verifyBlock(): wrong merkle-root! Block merkle-root="+rawBlock.getMerkle_root() + ", local compute merkle-root="+merkleRoot);
                    return false;
            }
            // 认为block中的交易正确，计算block的hash
            String headerHash = getBlockHeaderHash(rawBlock.getPre_hash(),
                    rawBlock.getMerkle_root(),rawBlock.getPre_state_root(),
                    rawBlock.getHeight()+"",rawBlock.getSign(),
                    rawBlock.getTimestamp(),rawBlock.getVersion());
            if(null == headerHash || null == rawBlock.getHash()){
                log.info("verifyBlock(): block hash is null!");
                return false;
            }
            if(!headerHash.equals(rawBlock.getHash())){
                log.info("verifyBlock(): wrong blockHash! Block blockHash="+rawBlock.getHash()+" , local compute blockHash="+headerHash);
                return false;
            }
            log.info("verifyBlock(): "+rawBlock.toString()+" has been confirmed.");
            return true;

        }catch (Exception e){
            log.warn("verifyBlock(): exception in verify");
            // TODO 验证出现错误
            e.printStackTrace();
            return false;
        }

    }

    /**
     * 创世区块的生成
     * */
    @Override
    public Block generateFirstBlock() {
        Block block = new Block();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = sdf.format(new Date());
        String hash = HashUtil.sha256("0");
        String pre_hash = HashUtil.sha256("-1");
        String merkle_root = "";
        // 状态树
        String pre_state_root = "";
        String state_root = this.worldState.getRootHash();;
        // 填写区块
        block.setPre_hash(pre_hash);
        block.setHash(hash);
        block.setPre_state_root(pre_state_root);
        block.setState_root(state_root);
        block.setMerkle_root(merkle_root);
        block.setHeight(0);
        block.setSign(this.nodeSign);
        block.setTimestamp(timestamp);
        block.setVersion("1.0");
        block.setExtra("");
        block.setArgs(pre_hash,hash,"","-1",0,this.nodeSign,timestamp,this.version,null,0);
        // 返回
        return block;
    }

    /**
     * 获取String类型数据的摘要
     * 这里使用的是JDK中实现的数据摘要。
     * 考虑到线程安全，每一次调用getDigest()方法都会新建一个messageDigest实例，防止互相干扰
     * MessageDigest.getInstance()方法并不会带来过多的开销。
     * @param data 输入数据
     * @return 生成的摘要
     * */
    @Override
    public String getDigest(String data) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(this.hashAlgorithm);
            // TODO 需要指定编码么？
            messageDigest.update(data.getBytes());
            StringBuilder ans = new StringBuilder();
            for (byte bt : messageDigest.digest()) {
                ans.append(String.format("%02X", bt));
            }
            return ans.toString();
        } catch (Exception e) {
            log.error("getDigest(): error in get digest, data="+data);
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 投票相关
     * */
    @Override
    public void voteForBlock(String tag,int height, int round, String blockHash, String nodeName, Boolean voteValue) {
        this.voteHandler.vote(tag,height,round,blockHash,nodeName,voteValue);
    }

    @Override
    public int getAgreeVoteCount(String tag,int height, int round, String blockHash) {
        return this.voteHandler.getVoteRecordAgree(tag,height,round,blockHash);
    }

    @Override
    public int getAgainstVoteCount(String tag,int height, int round, String blockHash) {
        return this.voteHandler.getVoteRecordAgainst(tag,height,round,blockHash);
    }

    @Override
    public void removeVote(String tag,int height, int round, String blockHash) {
        this.voteHandler.remove(tag,height,round,blockHash);
    }
    /**
     * 节点通信相关
     * */
    @Override
    public void broadcasting(Object message) {
        this.messageService.broadcasting(message);
    }

    @Override
    public int getClusterNodeSize() {
        return this.clusterSize;
    }

    @Override
    public void setClusterNodeSize(int size) {
        this.clusterSize = size;
    }

    @Override
    public void setMessageCallBack(MessageCallBack messageCallBack) {
        this.messageService.setMessageCallBack(messageCallBack);
    }

    /**
     * 自身属性相关
     * */
    @Override
    public String getName() {
        return this.nodeName;
    }

    @Override
    public String getVersion() {
        return this.version;
    }

    @Override
    public String getSign() {
        return this.nodeSign;
    }

    /****************************************************/
    /****************  private functions ****************/
    /****************************************************/

    /**
     * 初始化MessageService的callback
     * */
    private void initConsensus(){
        BlockchainServiceImpl bs = this;
        try {
            //TODO 暂时这么写 需要修改
            if(this.consensusType.equals(BaseConsensus.SBFT)){
                this.consensus = new SBFTConsensusImpl(bs);
            }
        } catch (Exception e) {
            log.error("initConsensus(): Failed init consensus "+consensusType+" in constructor.");
            e.printStackTrace();
        }
        if(null == this.consensus){
            log.error("initConsensus(): Null consensus. Shut down!");
            shutDownManager.shutDown();
            return;
        }
        log.info("initConsensus(): "+consensusType+" init complete.");
    }

    /**
     * 测试数据摘要方法是否可用
     * @param algorithm 算法名
     * */
    private void testMessageDigest(String algorithm){
        MessageDigest test = null;
        try {
            test = MessageDigest.getInstance(algorithm);
        } catch (Exception e) {
            if(e instanceof NoSuchAlgorithmException){
                log.warn("initMessageDigest(): cannot init messageDigest, caused by no such algorithm \'"+hashAlgorithm+"\'.");
            }else{
                log.warn("initMessageDigest(): cannot init messageDigest.");
                e.printStackTrace();
            }
        }
        // 检查是否成功init，若没有则使用默认的sha256
        if(null != test){
            log.info("testMessageDigest(): init complete.");
        }else{
            // 硬编码为sha256
            this.hashAlgorithm = "SHA-256";
        }

    }

    /**
     * 判断本地节点是否为主节点
     * 根据块高和轮数，使用轮询的方式考察当前节点列表。
     * 若节点的集群信息一致，使用相同的height和round可以算出同一个主节点index
     * @param height 块高
     * @param round 轮数
     * */
    private boolean isSelfLeader(int height, int round){
        if(messageService.getClusterAddressList().size() <= 1){
            // 当前网络只有一个节点
            log.info("isSelfLeader(): single node.");
            return true;
        }
        // 生成一个index，用于寻找主节点
        int index = 0;
        try {
            index = (height+""+round).hashCode();
            index = index > 0 ? index : index * (-1);
            index = index ^ (index >>> 16);
        } catch (Exception e) {
            index = 0;
            // TODO 处理选举异常
            e.printStackTrace();
        }
        index = index % messageService.getClusterAddressList().size();
        // 获取位于index的元素
        Iterator iterator = messageService.getClusterAddressList().iterator();
        String leader = (String) iterator.next();
        for(int i = 0;i < index;i++){
            leader = (String) iterator.next();
        }
        log.info("isSelfLeader(): multi node, index="+index+", "+this.messageService.getClusterAddressList()+", leader="+leader);
        // 比较
        return leader.equals(messageService.getLocalAddress());
    }

    /**
     * 基于height和round，从交易池中取出交易，做块
     * @param height
     * @param round
     * */
    private Block createNewBlock(int height, int round){
        // 做块计时开始
        Date createStart = new Date();
        int tranSeq = 0;
        // 从交易池中获取交易列表，最多5000个
        List<Transaction> rawTransList = redisTxpool.getList(TxPool.TXPOOL_LABEL_TRANSACTION,5000);
        // 被筛选出的可以入块执行的交易列表
        List<Transaction> validTransList = new ArrayList<Transaction>();
        // 逐个检查交易列表中的交易，交易在交易池中和数据库中没有记录，则为合法交易
        for(Transaction transaction : rawTransList){
            // 该交易在交易池中属于延时导致的无效交易
            if(null == redisTxpool.get(TxPool.TXPOOL_LABEL_DEL_TRANSACTION,transaction.getTran_hash())){
                // 该交易是否已经被持久化在数据库中
                if(null == transactionMapper.findTransByHash(transaction.getTran_hash())){
                    // 交易可用
                    transaction.setTranSeq(tranSeq++);
                    validTransList.add(transaction);
                }else{
                    // TODO 交易不在无效交易池中，并且已被持久化
                }
            }else{
                // 交易失效，删除
                // 此处出现的原因是本地节点在之前执行投票通过的区块交易时，执行了本地交易池中不存在的交易；可以认为是本地交易池的延时导致的
                redisTxpool.delete(TxPool.TXPOOL_LABEL_TRANSACTION,transaction.getTran_hash());
            }
        }
        log.info("createNewBlock(): Get valid transaction list, size="+validTransList.size());
        if(validTransList.size() < 1){
            // 未出现可做块的交易
            return null;
        }else{
            // 生成新的区块
            Block block = new Block();
            Times times = new Times();
            times.setStartCompute(new Date().getTime());
            // 交易排序
            Collections.sort(validTransList);
            // 填写block字段
            height = blockMapper.findMaxHeight() + 1;
            String preHash = blockMapper.findPreHashByHeight(height - 1);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String timestamp = sdf.format(new Date());
            String merkleRoot = getMerkleRoot(validTransList);
            String preStateRoot = blockMapper.findStatRoot(height - 1);
            // 生成头部，参数需要按照顺序，header作为区块的hash值
            String hash = getBlockHeaderHash(preHash,merkleRoot,preStateRoot,String.valueOf(height),this.nodeSign,timestamp,this.version);
            block.setArgs(preHash,hash,merkleRoot,preStateRoot,height,this.nodeSign,timestamp,this.version,
                    (ArrayList<Transaction>) validTransList,validTransList.size());
            // 做块计时结束
            Date createEnd = new Date();
            log.info("createNewBlock(): new block created, hash="+block.getHash()+", time cost="+(createEnd.getTime()-createStart.getTime())+"ms.");
            block.setExtra(String.valueOf(createStart.getTime()));
            // 填写时间信息
            times.setBlock_hash(hash);
            times.setTx_length(validTransList.size());
            times.setBroadcast(new Date().getTime());
            block.setTimes(times);
            return block;


        }

    }

    /**
     * 生成区块头部hash值。
     * 按顺序将参数String值合并，然后哈希
     * */
    private String getBlockHeaderHash(String preHash,String merkleRoot,String preStateRoot,String height,String nodeSign,String timestamp,String version){
        StringBuilder sb = new StringBuilder();
        sb.append(preHash);
        sb.append(merkleRoot);
        sb.append(preStateRoot);
        sb.append(height);
        sb.append(nodeSign);
        sb.append(timestamp);
        sb.append(version);
        return getDigest(sb.toString());
    }

    /**
     * 计算交易列表的merkle_root
     * @param transactionList 交易列表
     * @return 交易列表数据的merkle树根
     * */
    private String getMerkleRoot(List<Transaction> transactionList){
        // 先将交易列表生成对应的交易摘要列表
        List<String> digestList = new ArrayList<>();
        for(Transaction transaction : transactionList){
            digestList.add(this.getDigest(transaction.toString()));
        }
        // 两两hash，拼接计算
        while(digestList.size() != 1){
            digestList = updateListByMerkle(digestList);
        }
        return digestList.get(0);
    }

    /**
     * 对列表做一次merklehash
     * */
    private List<String> updateListByMerkle(List<String> list){
        int len = list.size();
        StringBuilder sb = new StringBuilder();
        List<String> res = new ArrayList<>();
        for(int i = 0;i < len;i += 2){
            if(len - i > 1){
                sb.append(list.get(i));
                sb.append(list.get(i + 1));
            }else{
                sb.append(list.get(i));
                sb.append(list.get(i));
            }
            res.add(this.getDigest(sb.toString()));
            sb.setLength(0);
        }
        return res;
    }

    /**
     * 返回一个列表内容的字符串摘要。
     * 将内容拼接起来，再算出摘要
     * @param list
     * @return
     * */
    private String getListDigest(List<String> list){
        int len = list.size();
        StringBuilder sb = new StringBuilder();
        for(String s : list){
            sb.append(s);
        }
        return this.getDigest(sb.toString());

    }


}
