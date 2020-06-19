package com.buaa.blockchain.core;


import com.buaa.blockchain.consensus.BaseConsensus;
import com.buaa.blockchain.consensus.PBFTConsensusImpl;
import com.buaa.blockchain.consensus.SBFTConsensusImpl;
import com.buaa.blockchain.crypto.HashUtil;
import com.buaa.blockchain.entity.Block;
import com.buaa.blockchain.message.Message;
import com.buaa.blockchain.entity.Times;
import com.buaa.blockchain.entity.Transaction;
import com.buaa.blockchain.exception.ShutDownManager;
import com.buaa.blockchain.mapper.BlockMapper;
import com.buaa.blockchain.mapper.TransactionMapper;
import com.buaa.blockchain.message.JGroupsMessageImpl;
import com.buaa.blockchain.message.MessageCallBack;
import com.buaa.blockchain.message.MessageService;
import com.buaa.blockchain.message.nettyimpl.NettyMessageImpl;
import com.buaa.blockchain.txpool.RedisTxPool;
import com.buaa.blockchain.txpool.TxPool;

import com.buaa.blockchain.utils.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

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
    /* 消息服务名称 */
    @Value("${buaa.blockchain.network}")
    private String messageServiceType;
    /* 消息服务器ip */
    @Value("${buaa.blockchain.msg.ipv4}")
    public String msgIp;
    /* 消息服务器端口 */
    @Value("${buaa.blockchain.msg.port}")
    public int msgPort;
    /* 节点消息服务器地址 */
    @Value("${buaa.blockchain.msg.address}")
    public String msgAddressList;
    /* 是否提前做块并缓存 */
    @Value("${buaa.blockchain.cache-blocks}")
    public Boolean cacheEnable;
    /* 单个区块内期望交易量 */
    @Value("${buaa.blockchain.tx-max-amount}")
    public int txMaxAmount;

    /*********************** 功能模块 ***********************/
    // 消息服务
    private MessageService messageService = null;
    // 集群大小
    public int clusterSize = 1;
    // 当前轮数
    public AtomicInteger round = new AtomicInteger(0);
    // 同步器，用于超时管理
    public CountDownLatch countDownLatch;
    // 提前做块的缓存队列，需要用height和round同时确定一个cache区块
    // TODO 长度限定
    private ConcurrentHashMap<Integer,Block> cacheBlockList = new ConcurrentHashMap<>();
    // 守护线程
    private Thread daeThread = null;
    // 标志位，是否初始化完毕
    private Boolean isSetup = false;
    // startNewRound的线程记录器
    private ConcurrentHashMap<Long,Boolean> threadMap = new ConcurrentHashMap<>();



    @Autowired
    public BlockchainServiceImpl(RedisTxPool redisTxpool, BlockMapper blockMapper,
                                 TransactionMapper transactionMapper, VoteHandler voteHandler, WorldState worldState, TimeoutHelper timeoutHelper, ShutDownManager shutDownManager) {
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
        log.info("Preparing for setup...");
        this.daeThread = new Thread();
        this.daeThread.setDaemon(true);
        this.daeThread.start();
        // 初始化共识
        initConsensus();
        // 初始化消息服务
        initMessageService();
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
        this.round.set(0);
        this.clusterSize = messageService.getClusterAddressList().size();
        try{
            Thread.sleep(5000);
        }catch (Exception e){

        }
        isSetup = true;
        // 以本地的区块信息，开始新一轮的做块
        startNewRound(BLOCKCHAIN_SERVICE_STATE_SUCCESS);
    }

    /**
     * 新一轮的做块
     * 当一轮做块开始后，主节点轮询查找交易池中的交易，其他节点无状态（非主节点仅仅检查后发现自己不是主节点，然后startNewRound执行完毕）
     * 主节点创建了新的块时，startNewRound也执行完毕，此时各个节点均处于等待区块投票结束和下一轮开始的状态。
     *
     * 需要检查当前有没有其他的线程在运行主节点的while循环。如果出现多次进入startNewRound并且自己为主节点时，会出现多个线程执行startNewRound的while
     * @param height 区块高度
     * @param round 轮数
     * */
    @Override
    public void startNewRound(int height, int round) {
        // 通过threadMap尝试关闭其他的startNewRound线程
        synchronized (isSetup){
            for(Long l : threadMap.keySet()){
                threadMap.put(l,false);
            }
            threadMap.put(Thread.currentThread().getId(),true);
        }
        // 判断是否为主节点
        try{
            if(isSelfLeader()){
                log.info("startNewRound(): leaderNode="+this.nodeName+", height="+height+", round="+round+" try to build new block...");
                Block block = null;
                int waitCount = 0;
                // 轮询交易池中是否有可以新的可以做块的交易
                while(threadMap.get(Thread.currentThread().getId())){
                    // 若集群变动，这里的检查用于自动跳出循环
                    if(!isSelfLeader()){
                        log.info("startNewRound(): round height="+height+", round="+round+" fired, start new round.");
                        return ;
                    }
                    // 首先检查缓存是否能命中
                    if(this.cacheEnable == true && cacheBlockList.keySet().contains(height)){
                        // 尝试从缓存中获取
                        block = cacheBlockList.get(height);
                        if(null != block){
                            // 填写缓存区块中尚未填补的字段
                            block.setExtra(Long.toString(System.currentTimeMillis()));
                            block.setPre_state_root(blockMapper.findStatRoot(height - 1));
                            block.setPre_hash(blockMapper.findHashByHeight(height - 1));
                            String hash = getBlockHeaderHash(block.getPre_hash(),block.getMerkle_root(),block.getPre_state_root(),
                                    String.valueOf(block.getHeight()),block.getSign(),block.getTimestamp(),block.getVersion());
                            block.setHash(hash);
                            block.getTimes().setBlock_hash(hash);
                            log.info("startNewRound(): hit the cache, height="+block.getHeight()+", block="+block.getHash());
                            // 删除命中的缓存区块
                            cacheBlockList.remove(height);
                            // 开始共识
                            Message message = new Message(this.nodeName,height,round,block);
                            this.consensus.setup(message);
                            return;
                        }
                    }else if(redisTxpool.size(TxPool.TXPOOL_LABEL_TRANSACTION) > 0){
                        // 交易池非空
                        block = createNewBlock(height,round);
                        if(null != block){
                            // 开始共识
                            Message message = new Message(this.nodeName,height,round,block);
                            this.consensus.setup(message);
                            return;
                        }else{
                            log.info("startNewRound(): leader node="+nodeName+" fail to create new block at height="+height+", round="+round+".");
                            continue;
                        }
                    } else{
                        try{
                            log.info("startNewRound(): not enough transactions found, wait for "+this.sleepTime+"ms.");
                            Thread.sleep(this.sleepTime);
                            waitCount++;
                            continue;
                        }catch (Exception e){
                            shutDownManager.shutDown();
                            // TODO 处理异常
                        }
                    }
                }
                log.warn("startNewRound(): thread "+Thread.currentThread().getId()+" shut down.");
                return ;
            }else{
                log.info("startNewRound(): not leader node, waiting for leader or timeout");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * startNewRound的总入口
     * 在一个节点中，startNewRound可能是瞬间执行完毕（非主节点），或者持续执行（主节点）
     * */
    @Override
    public void startNewRound(int code) {
        if(!isSetup){
            log.warn("startNewRound(): cannot enable new round!");
            return;
        }
        if(code == BLOCKCHAIN_SERVICE_STATE_SUCCESS){
            round.set(0);
            startNewRound(blockMapper.findMaxHeight()+1,round.get());
        }else if(code == BLOCKCHAIN_SERVICE_STATE_FAIL){
            round.addAndGet(1);
            startNewRound(blockMapper.findMaxHeight()+1,round.get());
        }
    }

    /**
     * 存储区块
     * */
    @Override
    public synchronized void storeBlock(Block block) {
        log.warn("storeBlock(): block="+block.toString());
        // 检查交易数量
        if(block.getTx_length() < 1){
            log.info("storeBlock(): get Block blockhash="+block.getHash()+" with no transation, storeBlock stop.");
            return ;
        }
        block.getTimes().setStoreBlock(System.currentTimeMillis());
        // 存储当前状态树根的值到block中
        block.setState_root(worldState.getRootHash());
        // 处理交易池
        for(Transaction ts : block.getTrans()){
            if(null == redisTxpool.get(TxPool.TXPOOL_LABEL_TRANSACTION,ts.getTran_hash())){
                // 本地交易池中没有此交易，认为是还没有收到（可能是网络时延）
                redisTxpool.put(TxPool.TXPOOL_LABEL_DEL_TRANSACTION,ts.getTran_hash(),ts);
            }else{
                // 本地交易池存在该交易，删除该交易
                redisTxpool.delete(TxPool.TXPOOL_LABEL_TRANSACTION,ts.getTran_hash());
            }
            ts.setBlock_hash(block.getHash());
        }
        block.getTimes().setEndTime(System.currentTimeMillis());
        // 计算耗时
        long cost = block.getTimes().getEndTime() - Long.valueOf(block.getExtra());
        block.setExtra(Long.toString(cost));
        // 持久化交易和区块
        worldState.sync();
        blockMapper.insertTimes(block.getTimes());
        blockMapper.insertBlock(block);
        log.info("storeBlock(): Done! block="+block.toString());
        insertTransactionList(block);
        // 删除缓存
        cacheBlockList.remove(block.getHeight());

        // 通知
        // this.timeoutHelper.notified(this.height,this.round);
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
            String preHash = blockMapper.findHashByHeight(height - 1);
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
            log.info("verifyBlock(): confirm block="+rawBlock.toString());
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
     * 基于height和round，将交易列表做块
     * @param height
     * @param round
     * */
    @Override
    public Block createNewBlock(int height, int round){
        List<Transaction> rawTransList = redisTxpool.getList(TxPool.TXPOOL_LABEL_TRANSACTION,txMaxAmount);
        // 做块计时开始
        Date createStart = new Date();
        int tranSeq = 0;
        // 被筛选出的可以入块执行的交易列表
        List<Transaction> validTransList = new ArrayList<Transaction>();
        log.info("createNewBlock(): get raw transaction list, size="+rawTransList.size());
        // 逐个检查交易列表中的交易，交易在交易池中和数据库中没有记录，则为合法交易
        for(Transaction transaction : rawTransList){
            if(isValidTransaction(transaction)){
                // 交易合法
                transaction.setTranSeq(tranSeq++);
                validTransList.add(transaction);
            }
        }
        log.info("createNewBlock(): Get valid transaction list, size="+validTransList.size());
        if(validTransList.size() < 1){
            // 未出现可做块的交易，此时交易池应该为空
            return null;
        }else{
            // 生成新的区块
            Block block = new Block();
            Times times = new Times();
            times.setStartCompute(new Date().getTime());
            // 交易排序
            Collections.sort(validTransList);
            // 填写block字段
            String preHash = blockMapper.findHashByHeight(height - 1);
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
            block.setExtra(String.valueOf(System.currentTimeMillis()));
            // 填写时间信息
            times.setBlock_hash(hash);
            times.setTx_length(validTransList.size());
            times.setBroadcast(System.currentTimeMillis());
            block.setTimes(times);
            return block;
        }

    }

    /**
     * 提前做块并且放入缓存队列中，按照高度作为键值来区分缓存的块，每一个高度只对应一个块，不按照轮数
     * 在SBFT中，提前做块的实际在于收到第一轮广播后，所有的节点可以提前做块，但是做块使用的交易需要避开该广播中的交易
     *
     * TODO 由于使用redisTxpool缓存交易，内部遍历hashmap的顺序是不固定的，暂时的方案是在本方法中遍历(txMaxAmout+transLength)个交易并且删除和正在投票区块重复的交易
     *
     * 和createNewBlock中用于直接广播的区块不同的是，提前做块的区块中仅仅包含了被打包的交易，区块头、状态根等需要上一个已完成的区块信息，故不填写
     *
     * @param height
     * @param round
     * @param baseBlock 调用者需要提供收到的区块中交易的数量，提前做块时需要跳过
     * 该方法需要异步执行，否则会降低加速的意义
     * */
    @Override
    public void createNewCacheBlock(int height, int round, Block baseBlock) {
        int transLength = baseBlock.getTx_length();
        if(cacheEnable == false || cacheBlockList.keySet().contains(height) || transLength <= 0){
            return ;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {

                log.info("createNewCacheBlock(): start, height="+height+", round="+round);
                TreeSet<String> treeSet = new TreeSet<>();
                for(Transaction ts : baseBlock.getTrans()){
                    treeSet.add(ts.getTran_hash());
                }
                int tranSeq = 0;
                List<Transaction> rawTransList = redisTxpool.getList(TxPool.TXPOOL_LABEL_TRANSACTION,txMaxAmount+transLength);
                // 被筛选出的可以入块执行的交易列表
                List<Transaction> validTransList = new ArrayList<Transaction>();
                // 逐个检查交易列表中的交易，交易在交易池中和数据库中没有记录，则为合法交易
                for(Transaction transaction : rawTransList){
                    if(isValidTransaction(transaction) && !treeSet.contains(transaction.getTran_hash())){
                        // 交易合法
                        transaction.setTranSeq(tranSeq++);
                        validTransList.add(transaction);
                    }
                }
                if(validTransList.size() < 1){
                    return ;
                }else{
                    // 生成新的区块
                    Block block = new Block();
                    Times times = new Times();
                    times.setStartCompute(new Date().getTime());
                    Collections.sort(validTransList);
                    String merkleRoot = getMerkleRoot(validTransList);
                    // 填写属性
                    block.setHeight(height);
                    block.setMerkle_root(merkleRoot);
                    block.setTimestamp((new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date()));
                    block.setTrans((ArrayList<Transaction>) validTransList);
                    block.setTx_length(validTransList.size());
                    block.setVersion(version);
                    block.setSign(nodeSign);
                    // 填写时间
                    times.setBlock_hash(block.getHash());
                    times.setTx_length(validTransList.size());
                    times.setBroadcast(System.currentTimeMillis());
                    block.setTimes(times);
                    // 放入缓存队列中
                    cacheBlockList.put(height,block);
                    log.info("createNewCacheBlock(): Done, height="+height+", round="+round+", txLength="+block.getTx_length());
                }
            }
        }).start();

    }

    /**
     * 向其他节点发起请求，获取从nowHeight到aimHeight高度的区块数据
     * @param nowHeight 本地最高块
     * */
    @Override
    public void requestSyncBlocks(int nowHeight) {
        // 生成Message用于请求block
        Message message = new Message();
        message.setSenderAddress(messageService.getLocalAddress());
        message.setTopic(CORE_MESSAGE_TOPIC_SYNC);
        message.setHeight(blockMapper.findMaxHeight());
        // 广播
        broadcasting(message);
    }

    /**
     * 回复requestSyncBlocks，具体需要将搜索本地区块数据并且打包发送给需要的节点
     * */
    @Override
    public synchronized void replySyncBlocks() {

    }

    /**
     * 将参数中的blockList在本地执行并且存储
     * */
    @Override
    public synchronized void syncBlocks(List<Block> blockList) {

    }

    /**
     * 模拟执行交易，用于PBFT共识协议
     * */
    @Override
    public synchronized String transactionExec(String stateRoot, Block block) {
        log.info("transactionExec(): start exec transactions in block="+block.getHash()+", tranactions size="+block.getTx_length());
        if(null == stateRoot){
            List<Transaction> transactions = block.getTrans();
            TxExecuter.baseExecute(transactions,worldState);
            return worldState.getRootHash();
        }
        return null;
    }

    /**
     * 撤销交易执行，回复到上一次
     * */
    @Override
    public void undoTransactionExec() {
        log.info("undoTransactionExec(): undo trie...");
        String pre = worldState.getRootHash();
        String rootInDb = blockMapper.findStatRoot(blockMapper.findMaxHeight());
        worldState.undo();
        String now = worldState.getRootHash();

        // 检查是否回复到当前最高区块所记录的stateRoot值
        if(!now.equals(rootInDb)){
            // TODO undo失败
            log.error("undoTransactionExec(): cannot undo to root in db, root in db is "+
                    rootInDb+", now="+now+", pre="+pre);
        }
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

    @Override
    public void deployContract(Object o) {

    }

    @Override
    public void syncContract(String contractId) {

    }

    /**
     * 节点通信相关
     * */
    @Override
    public void broadcasting(Message message) {
        this.messageService.broadcasting(JsonUtil.message2JsonString(message));
    }

    @Override
    public int getClusterNodeSize() {
        return this.messageService.getClusterAddressList().size();
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
     * 初始化MessageService
     * */
    private void initMessageService(){
        BlockchainServiceImpl bs = this;
        try{
            if(this.messageServiceType.equals(MessageService.JGROUPS)){
                this.messageService = new JGroupsMessageImpl();
            }else if(this.messageServiceType.equals(MessageService.NETTY)){
                this.messageService = new NettyMessageImpl(msgIp,msgPort,msgAddressList);
            }else{
                log.error("initMessageService(): "+this.messageServiceType+" not found! Shut down.");
                shutDownManager.shutDown();
            }
            this.messageService.setMessageCallBack(new MessageCallBack() {
                @Override
                public void onMessageReceived(Object content) {
                    // 将消息还原成标准格式
                    try {
                        Message receiveMsg = JsonUtil.objectMapper.readValue((String)content,Message.class);
                        // 判断是否为core需要处理的消息，否则分发给其他模块
                        // TODO 判断core
                        bs.consensus.onMessageReceived(receiveMsg);
                    } catch (JsonProcessingException e) {
                        // TODO 异常处理
                        e.printStackTrace();
                        log.error("OnMessageReceived(): unknown message data, "+content);
                    }
                }
                @Override
                public void onClusterChanged(Set<String> pre, Set<String> now) {
                    log.warn("OnClusterChanged(): cluster changed pre="+pre+" now="+now);
                    // 轮数归零
                    bs.startNewRound(bs.BLOCKCHAIN_SERVICE_STATE_SUCCESS);
                }
            });
        }catch (Exception e){
            log.error("initMessageService(): Failed init message service "+messageServiceType+" in constructor.");
            e.printStackTrace();
        }finally {
            if(null == this.messageService){
                log.error("initMessageService(): Null message service. Shut down!");
                shutDownManager.shutDown();
                return;
            }
        }
        log.info("initMessageService(): "+messageServiceType+" init complete.");
    }
    /**
     * 初始化共识协议
     * */
    private void initConsensus(){
        BlockchainServiceImpl bs = this;
        try {
            //TODO 暂时这么写 需要修改
            if(this.consensusType.equals(BaseConsensus.SBFT)){
                this.consensus = new SBFTConsensusImpl(bs);
            }else if(this.consensusType.equals(BaseConsensus.PBFT)){
                this.consensus = new PBFTConsensusImpl(bs);
            }else{
                log.error("initConsensus(): "+this.consensusType+" not found! Shut down.");
                shutDownManager.shutDown();
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
     * */
    private boolean isSelfLeader(){
        int height = blockMapper.findMaxHeight() + 1;
        int round = this.round.get();
        if(messageService.getClusterAddressList().size() == 0){
            log.warn("isSelfLeader(): no node detected!");
            return false;
        }
        if(messageService.getClusterAddressList().size() == 1){
            // 当前网络只有一个节点
            // log.info("isSelfLeader(): single node.");
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
     * 检查交易是否合法
     * 检查点如下
     * 1. 是否为延迟交易
     * 2. 是否为已经在MYSQL中持久化的交易
     * */
    private boolean isValidTransaction(Transaction ts){
        // 是否为延迟交易
        if(null == redisTxpool.get(TxPool.TXPOOL_LABEL_DEL_TRANSACTION,ts.getTran_hash())){
            // 该交易是否已经被持久化在数据库中
            if(null == transactionMapper.findTransByHash(ts.getTran_hash())){
                // 交易可用
                return true;
            }else{
                // 交易不在无效交易池中，并且已被持久化，不受理此交易
                // TODO 但是应该基于反馈
                redisTxpool.delete(TxPool.TXPOOL_LABEL_TRANSACTION,ts.getTran_hash());
            }
        }else{
            // 此处出现的原因是本地节点在之前执行投票通过的区块交易时，执行了本地交易池中不存在的交易；可以认为是本地交易池的延时导致的
            redisTxpool.delete(TxPool.TXPOOL_LABEL_TRANSACTION,ts.getTran_hash());
        }
        return false;
    }

    /**
     * 存储交易
     * */
    public void insertTransactionList(Block block){
        transactionMapper.insertAllTrans(block.getTrans());
    }
}
