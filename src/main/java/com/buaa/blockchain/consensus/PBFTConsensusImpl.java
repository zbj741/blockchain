package com.buaa.blockchain.consensus;
import com.buaa.blockchain.core.BlockchainService;
import com.buaa.blockchain.entity.Block;
import com.buaa.blockchain.message.Message;
import lombok.extern.slf4j.Slf4j;


import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PBFTConsensus的实现类
 * TODO 流程之间同步性还没有完成
 *
 * @author hitty
 * */

@Slf4j
public class PBFTConsensusImpl implements PBFTConsensus<Message> {
    // 对区块链服务的引用
    private BlockchainService blockchainService = null;
    // 投票通过率
    private float agreeGate;
    // 在commit阶段暂时用来存储正在参与做块的区块，减少传输中的带宽
    private ConcurrentHashMap<String,Block> blockList = new ConcurrentHashMap<>();

    public PBFTConsensusImpl(BlockchainService blockchainService){
        this.blockchainService = blockchainService;
        this.agreeGate = 0.67f;
        log.info("PBFTConsensusImpl(): init, agreeGate="+this.agreeGate);
    }
    public PBFTConsensusImpl(BlockchainService blockchainService, float agreeGate){
        this.blockchainService = blockchainService;
        this.agreeGate = agreeGate;
        log.info("PBFTConsensusImpl(): init, agreeGate="+this.agreeGate);
    }
    /**
     * 入口，将Message进行共识
     * */
    @Override
    public void setup(Message message) {
        prePrepareBroadcast(message);
    }

    /**
     * 从core.BlockchainService中获取消息
     * */
    @Override
    public void onMessageReceived(Message receiveMsg) {
        try{
            String topic = receiveMsg.getTopic();
            if(topic.equals(PBFT_MESSAGE_TOPIC_REQUEST)){
                requestReceived(receiveMsg);
            }else if(topic.equals(PBFT_MESSAGE_TOPIC_PREPREPARE)){
                prePrepareReceived(receiveMsg);
            }else if(topic.equals(PBFT_MESSAGE_TOPIC_PREPARE)){
                prepareReceived(receiveMsg);
            }else if(topic.equals(PBFT_MESSAGE_TOPIC_COMMIT)){
                commitReceived(receiveMsg);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onClusterChanger(Set<String> pre, Set<String> now) {

    }

    @Override
    public void requestBroadcast(Message message) {}

    @Override
    public void requestReceived(Message message) {}

    /**
     * pre-prepare的广播
     * 广播的内容为被计算了merkle树和夹带各种上一区块信息的区块
     * */
    @Override
    public void prePrepareBroadcast(Message message) {
        message.setTopic(PBFT_MESSAGE_TOPIC_PREPREPARE);
        message.getBlock().getTimes().setBroadcast(System.currentTimeMillis());
        log.info("prePrepareBroadcast(): broadcast block="+message.getBlock().getHash());
        blockchainService.broadcasting(message);
    }

    /**
     * 接收到了pre-prepare的广播
     * 检查广播中的区块所携带信息是否正确，正确则广播prepare
     * */
    @Override
    public synchronized void prePrepareReceived(Message message) {
        log.info("prePrepareReceived(): received message="+message.toString());
        boolean vote = blockchainService.verifyBlock(message.getBlock(),message.getHeight(),message.getRound());
        // 生成投票信息
        Message voteMessage = new Message(PBFT_MESSAGE_TOPIC_PREPARE,blockchainService.getName(),
                message.getHeight(),message.getRound(),vote,message.getBlock());
        prepareBroadcast(voteMessage);
        // 尝试提前做块，已经获知当前区块了，可以避开已存在在区块中的交易
        if(vote){
            blockchainService.createNewCacheBlock(message.getHeight() + 1,message.getRound(),message.getBlock());
        }

    }

    /**
     * 发起prepare的投票
     * */
    @Override
    public void prepareBroadcast(Message message) {
        log.info("prepareBroadcast:(): node="+blockchainService.getName()+" vote "+message.getVote()+" to "+message.getBlock().toString());
        blockchainService.broadcasting(message);
        return;
    }

    /**
     * 收到prepareBroadcast的投票结果，记录
     * 当赞成票数量超过了阈值时，模拟执行该区块的交易列表并且将执行结果的worldState的rootHash值带入commit中
     * */
    @Override
    public synchronized void prepareReceived(Message message) {
        // 赋值
        int height = message.getHeight();
        int round = message.getRound();
        String msgNodeName = message.getNodeName();
        Block block = message.getBlock();
        String blockHash = block.getHash();
        Boolean voteValue = message.getVote();
        // 投票
        if(blockchainService.voteForBlock(PBFT_VOTETAG_PREPARE,height,round, blockHash,msgNodeName,voteValue)){
            int agree = blockchainService.getAgreeVoteCount(PBFT_VOTETAG_PREPARE,message.getHeight(),message.getRound(),
                    message.getBlock().getHash());
            int against = blockchainService.getAgainstVoteCount(PBFT_VOTETAG_PREPARE,message.getHeight(),message.getRound(),
                    message.getBlock().getHash());
            // 查看是否收到大于2/3的同意票
            if(agree * 1.0f > blockchainService.getClusterNodeSize() * this.agreeGate){
                log.info("prepareReceived:() block="+message.getBlock().getHash()+", agree="+ agree + ", waiting to exec...");
                // 删除投票信息
                blockchainService.removeVote(PBFT_VOTETAG_PREPARE,message.getHeight(),message.getRound(),
                        message.getBlock().getHash());
                // 模拟执行交易
                String fakeStateRoot = blockchainService.transactionExec(null,message.getBlock());
                // 在消息的区块中存入自己本地模拟执行交易后的worldState的rootHash
                message.getBlock().setState_root(fakeStateRoot);
                // 广播commit消息
                commitBroadcast(message);
                log.info("prepareReceived(): block="+message.getBlock().getHash()+" confirm at prepare, exec tx stateRoot="+fakeStateRoot+", and wait to commit.");
            }else if(against*1.0f > blockchainService.getClusterNodeSize() * (1.0f - this.agreeGate)){
                // 反对票大于1/3，开始下一轮
                log.info("prepareReceived(): block="+message.getBlock().getHash()+", against="+against+", start new round.");
                // 删除投票信息
                blockchainService.removeVote(PBFT_VOTETAG_PREPARE,message.getHeight(),message.getRound(),
                        message.getBlock().getHash());
                // 清空做块缓存
                blockchainService.flushCacheBlock();
                // 开启新的一轮
                blockchainService.startNewRound(BlockchainService.BLOCKCHAIN_SERVICE_STATE_FAIL);
            }else{

            }
        }else{
            log.info("prepareReceived(): removed item tag="+PBFT_VOTETAG_PREPARE+", height="+height+", round="+round+", blockhash="+blockHash+"!");
            return;
        }
    }

    /**
     * 将模拟执行的stateRoot广播
     * */
    @Override
    public void commitBroadcast(Message message) {
        message.setTopic(PBFT_MESSAGE_TOPIC_COMMIT);
        message.setNodeName(blockchainService.getName());
        // 将该区块缓存在本地
        blockList.put(message.getBlock().getHash(),message.getBlock());
        // block中其他的字段已经不需要了，所以可以去掉。暂时去掉占用空间最大的交易列表数据，此处深拷贝
        message.setBlock(blockList.get(message.getBlock().getHash()).copyWithoutTrans());
        blockchainService.broadcasting(message);
        log.info("commitBroadcast(): commit stateRoot="+message.getBlock().getState_root()+
                ", block="+message.getBlock().getHash());
        return;
    }

    /**
     * 接收commit的广播，对比stateRoot，相同的达到阈值则存储区块
     * */
    @Override
    public synchronized void commitReceived(Message message) {
        log.info("commitReceived(): received stateRoot="+message.getBlock().getState_root()+
                " at block="+message.getBlock().getHash()+", node="+message.getNodeName());
        String hash = message.getBlock().getHash();
        // 计算投票意见
        boolean vote = false;
        if(blockList.containsKey(hash)){
            // 当前处理的区块本地存在，并且本地模拟执行的stateRoot和接收到的message中的stateRoot相同
            if(blockList.get(hash).getState_root().equals(message.getBlock().getState_root())){
                vote = true;
            }
        }
        // 投票
        if(blockchainService.voteForBlock(PBFT_VOTETAG_COMMIT,message.getHeight(),message.getRound(),
                message.getBlock().getHash(),message.getNodeName(),vote)){
            blockchainService.voteForBlock(PBFT_VOTETAG_COMMIT,message.getHeight(),message.getRound(),
                    message.getBlock().getHash(),message.getNodeName(),vote);
            int agree = blockchainService.getAgreeVoteCount(PBFT_VOTETAG_COMMIT,message.getHeight(),
                    message.getRound(),message.getBlock().getHash());
            int against = blockchainService.getAgainstVoteCount(PBFT_VOTETAG_COMMIT,message.getHeight(),
                    message.getRound(),message.getBlock().getHash());
            // 查看是否收到大于2/3的同意票
            if(agree * 1.0f > blockchainService.getClusterNodeSize() * this.agreeGate){
                log.info("commitReceived:() block="+message.getBlock().getHash()+" confirm through stateRoot consistency, "+agree+"/"+blockchainService.getClusterNodeSize());
                Block block = blockList.get(message.getBlock().getHash());
                // 删除投票信息
                blockchainService.removeVote(PBFT_VOTETAG_COMMIT,message.getHeight(),message.getRound(),
                        message.getBlock().getHash());
                // 删除blockList中的存储
                blockList.remove(message.getBlock().getHash());
                // 存储区块
                blockchainService.storeBlock(block);
                // 开启下一轮
                blockchainService.startNewRound(BlockchainService.BLOCKCHAIN_SERVICE_STATE_SUCCESS);
                return;
            }else if(against * 1.0f > blockchainService.getClusterNodeSize() * (1.0 - this.agreeGate)){
                // 投票未通过，无法commit
                log.info("commitReceived:() block="+message.getBlock().getHash()+" drop due to against, "+against+"/"+blockchainService.getClusterNodeSize());
                // 删除投票信息
                blockchainService.removeVote(PBFT_VOTETAG_COMMIT,message.getHeight(),message.getRound(),
                        message.getBlock().getHash());
                // 删除blockList中的存储
                blockList.remove(message.getBlock().getHash());
                // 撤回交易执行
                blockchainService.undoTransactionExec();
                // 开启新的一轮
                blockchainService.startNewRound(BlockchainService.BLOCKCHAIN_SERVICE_STATE_FAIL);
                return;
            }else{

            }
        }else{
            log.info("commitReceived(): no key found in vote.");
        }


    }

    @Override
    public void viewChanged() {

    }

}
