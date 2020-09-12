package com.buaa.blockchain.consensus;

import com.buaa.blockchain.core.BlockchainService;
import com.buaa.blockchain.entity.Block;
import com.buaa.blockchain.message.Message;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
/**
 * SBFT共识协议的实现类
 * */
@Slf4j
public class SBFTConsensusImpl implements SBFTConsensus<Message>{
    private BlockchainService blockchainService = null;
    private float agreeGate;
    // TODO 超时检查器，即当前轮数中某一个阶段到达超时条件

    public SBFTConsensusImpl(BlockchainService blockchainService){
        this.blockchainService = blockchainService;
        this.agreeGate = 0.67f;
        log.info("SBFTConsensusImpl(): init, agreeGate="+this.agreeGate);
    }

    public SBFTConsensusImpl(BlockchainService blockchainService ,float agreeGate){
        this.blockchainService = blockchainService;
        this.agreeGate = agreeGate;
        log.info("SBFTConsensusImpl(): init, agreeGate="+this.agreeGate);
    }

    @Override
    public void setup(Message message) {
        sbftDigestBroadcast(message);
    }

    @Override
    public void onMessageReceived(Message receiveMsg) {
        String topic = receiveMsg.getTopic();
        // 对应处理逻辑
        if (topic.equals(SBFT_MESSAGE_TOPIC_DIGEST)) {
            sbftDigestBroadcastReceived(receiveMsg);
        } else if (topic.equals(SBFT_MESSAGE_TOPIC_VOTE)) {
            sbftVoteBroadcastReceived(receiveMsg);
        } else if(topic.equals(SBFT_MESSAGE_TOPIC_TEST)){
            log.info("OnMessageReceived(): testMsg:"+receiveMsg.toString());
        }
    }

    @Override
    public void onClusterChanger(Set<String> pre, Set<String> now) {

    }

    /**
     * 主节点发出第一阶段的广播
     * */
    @Override
    public void sbftDigestBroadcast(Message stage1_send) {
        stage1_send.setTopic(SBFT_MESSAGE_TOPIC_DIGEST);
        stage1_send.getBlock().getTimes().setBroadcast(System.currentTimeMillis());
        blockchainService.broadcasting(stage1_send);
        log.info("sbftDigestBroadcast(): broadcast block="+stage1_send.getBlock().getHash()+", height="+
                stage1_send.getBlock().getHeight());
    }

    /**
     * 收到第一阶段的主节点做块进行检验，并且投票
     * */
    @Override
    public void sbftDigestBroadcastReceived(Message stage1_received) {
        log.info("sbftDigestBroadcastReceived(): received block="+stage1_received.getBlock().getHash()+", height="+
                stage1_received.getBlock().getHeight());
        boolean vote = blockchainService.verifyBlock(stage1_received.getBlock(),stage1_received.getHeight(),stage1_received.getRound());
        // 计时
        stage1_received.getBlock().getTimes().setBlockReceived(System.currentTimeMillis());
        // 生成投票消息
        Message message = new Message(SBFT_MESSAGE_TOPIC_VOTE,blockchainService.getName(),
                stage1_received.getHeight(),stage1_received.getRound(),vote,stage1_received.getBlock());
        sbftVoteBroadcast(message);
        // 尝试提前做块，是异步执行
        if(vote){
            blockchainService.createNewCacheBlock(stage1_received.getHeight()+1,stage1_received.getRound(),
                    stage1_received.getBlock());
        }

    }

    /**
     * 发出第二阶段的投票广播信息
     * */
    @Override
    public void sbftVoteBroadcast(Message stage2_send) {
        stage2_send.setTopic(SBFT_MESSAGE_TOPIC_VOTE);
        stage2_send.getBlock().getTimes().setSendVote(System.currentTimeMillis());
        log.info("sbftVoteBroadcast(): node="+blockchainService.getName()+" vote "+stage2_send.getVote()+" to block="
                +stage2_send.getBlock().getHash()+", height="+ stage2_send.getBlock().getHeight());
        blockchainService.broadcasting(stage2_send);
        return ;
    }

    /**
     * 收到第二阶段的投票广播消息
     * 当赞成票超过阈值时，执行sbftExcute阶段
     * 当反对票超过阈值时，开始新的一轮
     *
     * 【注】 投票过程中会产生同步问题，所以每一次接收投票的方法是synchronize的
     * */
    @Override
    public synchronized void sbftVoteBroadcastReceived(Message stage2_received) {
        // 赋值
        int height = stage2_received.getHeight();
        int round = stage2_received.getRound();
        String msgNodeName = stage2_received.getNodeName();
        Block block = stage2_received.getBlock();
        String blockHash = block.getHash();
        Boolean voteValue = stage2_received.getVote();
        // 接收投票
        if(blockchainService.voteForBlock(SBFT_VOTETAG_VOTE,height,round,blockHash,msgNodeName,voteValue)){
            log.info("sbftVoteBroadcastReceived():node="+msgNodeName+", vote="+voteValue+" to block="+blockHash+", height="+height+", round="+round);
            // 查看是否收到sbft中大于集群节点个数2/3的同意票
            if(blockchainService.getAgreeVoteCount(SBFT_VOTETAG_VOTE,height,round,blockHash)*1.0f > blockchainService.getClusterNodeSize() * this.agreeGate){
                log.info("sbftVoteBroadcastReceived(): execute, block="+blockHash+", height="+height+", round="+round+
                        " received vote "+blockchainService.getAgreeVoteCount(SBFT_VOTETAG_VOTE,height,round,blockHash)+
                        "/"+blockchainService.getClusterNodeSize());
                // 删除投票记录
                blockchainService.removeVote(SBFT_VOTETAG_VOTE,height,round,blockHash);
                // 计时
                stage2_received.getBlock().getTimes().setVoteReceived(System.currentTimeMillis());
                // 执行
                blockchainService.transactionExec(null,stage2_received.getBlock());
                blockchainService.storeBlock(stage2_received.getBlock());
                blockchainService.startNewRound(BlockchainService.BLOCKCHAIN_SERVICE_STATE_SUCCESS);
                return ;
            }else if(blockchainService.getAgainstVoteCount(SBFT_VOTETAG_VOTE,height,round,blockHash)*1.0f > blockchainService.getClusterNodeSize() * (1.0f - this.agreeGate)){
                // 反对票超过1/3，直接开始下一轮
                log.info("sbftVoteBroadcastReceived(): start next round. Block="+blockHash+", height="+height+", round="+round+
                        " received vote against "+blockchainService.getAgainstVoteCount(SBFT_VOTETAG_VOTE,height,round,blockHash)+
                        "/"+blockchainService.getClusterNodeSize());
                // 删除投票记录
                blockchainService.removeVote(SBFT_VOTETAG_VOTE,height,round,blockHash);
                // 清空提前做块缓存，因为是基于当前区块进行的提前做块
                blockchainService.flushCacheBlock();
                // 开始下一轮
                blockchainService.startNewRound(BlockchainService.BLOCKCHAIN_SERVICE_STATE_FAIL);
                return ;
            }else{
                // TODO 本轮投票没有操作？
            }
        }else{
            log.info("sbftVoteBroadcastReceived(): removed item tag="+SBFT_VOTETAG_VOTE+", height="+height+", round="+round+", blockhash="+blockHash+"!");
            return ;
        }
    }
}
