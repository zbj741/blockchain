package com.buaa.blockchain.consensus;

import com.buaa.blockchain.core.BlockchainService;
import com.buaa.blockchain.entity.Block;
import com.buaa.blockchain.entity.Message;
import com.buaa.blockchain.message.MessageCallBack;
import com.buaa.blockchain.utils.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;

import java.util.ArrayList;
import java.util.Set;
/**
 * SBFT共识协议的实现类
 * */
@Slf4j
public class SBFTConsensusImpl implements SBFTConsensus<Message>{
    private BlockchainService blockchainService = null;
    // TODO 超时检查器，即当前轮数中某一个阶段到达超时条件

    public SBFTConsensusImpl(BlockchainService blockchainService){
        this.blockchainService = blockchainService;
        // 定义消息系统和共识流程的结合点
        this.blockchainService.setMessageCallBack(new MessageCallBack() {
            @Override
            public void OnMessageReceived(Object msg) {
                try {
                    // 将消息还原成标准格式
                    Message receiveMsg = JsonUtil.objectMapper.readValue((String)msg,Message.class);
                    String topic = receiveMsg.getTopic();
                    // 对应处理逻辑
                    if (topic.equals(SBFT_MESSAGE_TOPIC_DIGEST)) {
                        sbftDigestBroadcastReceived(receiveMsg);
                    } else if (topic.equals(SBFT_MESSAGE_TOPIC_VOTE)) {
                        sbftVoteBroadcastReceived(receiveMsg);
                    } else if (topic.equals(SBFT_MESSAGE_TOPIC_SYNC)) {

                    } else if (topic.equals(SBFT_MESSAGE_TOPIC_SYNC_REPLY)){

                    } else if(topic.equals(SBFT_MESSAGE_TOPIC_TEST)){
                        log.info("OnMessageReceived(): testMsg:"+receiveMsg.toString());
                    }
                } catch (JsonProcessingException e) {
                    // TODO 异常处理
                    e.printStackTrace();
                    log.error("OnMessageReceived(): unknown message data, "+msg);
                }
            }
            // 收到集群变动后的处理逻辑
            @Override
            public void OnClusterChanged(Set<String> pre, Set<String> now) {
                log.warn("OnClusterChanged(): cluster changed pre="+pre+" now="+now);
                // 轮数归零
                blockchainService.startNewRound(blockchainService.BLOCKCHAIN_SERVICE_STATE_SUCCESS);
                // TODO 其他逻辑
            }
        });
    }

    @Override
    public void setup(Message message) {
        sbftDigestBroadcast(message);
    }

    /**
     * 主节点发出第一阶段的广播
     * */
    @Override
    public void sbftDigestBroadcast(Message stage1_send) {
        stage1_send.setTopic(SBFT_MESSAGE_TOPIC_DIGEST);
        String jsonStr =  JsonUtil.message2JsonString(stage1_send);
        log.info("sbftDigestBroadcast(): broadcast block, message size=" + jsonStr.length() * 2 / 1024.0 + "KB.");
        blockchainService.broadcasting(jsonStr);
    }

    /**
     * 收到第一阶段的主节点做块进行检验，并且投票
     * */
    @Override
    public void sbftDigestBroadcastReceived(Message stage1_received) {
        log.info("sbftDigestBroadcastReceived(): received message="+stage1_received.toString());
        boolean vote = blockchainService.verifyBlock(stage1_received.getBlock(),stage1_received.getHeight(),stage1_received.getRound());
        // 计时
        stage1_received.getBlock().getTimes().setBlockReceived(System.currentTimeMillis());
        // 生成投票消息
        Message message = new Message(SBFT_MESSAGE_TOPIC_VOTE,blockchainService.getName(),
                stage1_received.getHeight(),stage1_received.getRound(),vote,stage1_received.getBlock());
        sbftVoteBroadcast(message);
        // 尝试提前做块，是异步执行
        blockchainService.createNewCacheBlock(stage1_received.getHeight()+1,stage1_received.getRound(),
                stage1_received.getBlock());
    }

    /**
     * 发出第二阶段的投票广播信息
     * */
    @Override
    public void sbftVoteBroadcast(Message stage2_send) {
        stage2_send.setTopic(SBFT_MESSAGE_TOPIC_VOTE);
        stage2_send.getBlock().getTimes().setSendVote(System.currentTimeMillis());
        // 将消息打包成Json字符串
        String jsonStr = JsonUtil.message2JsonString(stage2_send);
        log.info("sbftVoteBroadcast(): node="+blockchainService.getName()+" vote "+stage2_send.getVote()+" to "+stage2_send.getBlock().toString());
        blockchainService.broadcasting(jsonStr);
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
        log.info("sbftVoteBroadcastReceived(): vote message="+stage2_received.toString());
        // 赋值
        int height = stage2_received.getHeight();
        int round = stage2_received.getRound();
        String msgNodeName = stage2_received.getNodeName();
        Block block = stage2_received.getBlock();
        String blockHash = block.getHash();
        Boolean voteValue = stage2_received.getVote();
        // 接收投票
        blockchainService.voteForBlock(SBFT_VOTETAG_VOTE,height,round,blockHash,msgNodeName,voteValue);
        log.info("sbftVoteBroadcastReceived(): block="+blockHash+", "+blockchainService.getAgreeVoteCount(SBFT_VOTETAG_VOTE,height,round,blockHash)+"/"+blockchainService.getClusterNodeSize());
        // 查看是否收到sbft中大于集群节点个数2/3的同意票
        if(blockchainService.getAgreeVoteCount(SBFT_VOTETAG_VOTE,height,round,blockHash)*1.0f > blockchainService.getClusterNodeSize() * (2/3.0f)){
            log.info("sbftVoteBroadcastReceived(): execute, block="+blockHash+", height="+height+", round="+round+
                    " received vote "+blockchainService.getAgreeVoteCount(SBFT_VOTETAG_VOTE,height,round,blockHash)+
                    "/"+blockchainService.getClusterNodeSize());
            // 删除投票记录
            blockchainService.removeVote(SBFT_VOTETAG_VOTE,height,round,blockHash);
            // 计时
            stage2_received.getBlock().getTimes().setVoteReceived(System.currentTimeMillis());
            // 执行
            stage2_received.setTopic(SBFT_MESSAGE_TOPIC_EXECUTE);
            sbftExecute(stage2_received);
            return ;
        }else if(blockchainService.getAgainstVoteCount(SBFT_VOTETAG_VOTE,height,round,blockHash)*1.0f > blockchainService.getClusterNodeSize() * (1/3.0f)){
            // 反对票超过1/3，直接开始下一轮
            log.info("sbftVoteBroadcastReceived(): start next round. Block="+blockHash+", height="+height+", round="+round+
                    " received vote against "+blockchainService.getAgainstVoteCount(SBFT_VOTETAG_VOTE,height,round,blockHash)+
                    "/"+blockchainService.getClusterNodeSize());
            // 删除投票记录
            blockchainService.removeVote(SBFT_VOTETAG_VOTE,height,round,blockHash);
            // 开始下一轮
            stage2_received.setTopic(SBFT_MESSAGE_TOPIC_DROP);
            sbftExecute(stage2_received);
            return ;
        }

    }

    /**
     * 执行阶段，执行区块中的交易，并且持久化
     * 这里就不通过网络广播了，直接本地执行
     * */
    @Override
    public void sbftExecute(Message exec) {
        // 检查是否为投票通过的
        if(SBFT_MESSAGE_TOPIC_DROP.equals(exec.getTopic())){
            log.info("sbftExecute(): Drop block="+exec.getBlock().getHash());
            blockchainService.startNewRound(BlockchainService.BLOCKCHAIN_SERVICE_STATE_FAIL);
            return ;
        }else if(SBFT_MESSAGE_TOPIC_EXECUTE.equals(exec.getTopic())){
            log.info("sbftExecute(): execute block="+exec.getBlock().getHash());
            blockchainService.transactionExec(null,exec.getBlock());
            blockchainService.storeBlock(exec.getBlock());
            blockchainService.startNewRound(BlockchainService.BLOCKCHAIN_SERVICE_STATE_SUCCESS);
            return ;
        }
    }
}
