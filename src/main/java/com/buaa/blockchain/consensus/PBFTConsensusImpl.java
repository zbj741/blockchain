package com.buaa.blockchain.consensus;
import com.buaa.blockchain.core.BlockchainService;
import com.buaa.blockchain.entity.Block;
import com.buaa.blockchain.entity.Message;
import com.buaa.blockchain.message.MessageCallBack;
import com.buaa.blockchain.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

/**
 * PBFTConsensus的实现类
 *
 * @author hitty
 * */

@Slf4j
public class PBFTConsensusImpl implements PBFTConsensus<Message> {
    private BlockchainService blockchainService = null;

    public PBFTConsensusImpl(BlockchainService blockchainService){
        this.blockchainService = blockchainService;
        this.blockchainService.setMessageCallBack(new MessageCallBack() {
            @Override
            public void OnMessageReceived(Object msg) {
                try{
                    // 将消息还原成标准格式
                    Message receiveMsg = JsonUtil.objectMapper.readValue((String)msg,Message.class);
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
            public void OnClusterChanged(Set<String> pre, Set<String> now) {

            }
        });
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
        String jsonStr = JsonUtil.message2JsonString(message);
        log.info("prePrepareBroadcast(): broadcast block, message size=" + jsonStr.length() * 2 / 1024.0 + "KB.");
        blockchainService.broadcasting(jsonStr);
    }

    /**
     * 接收到了pre-prepare的广播
     * 检查广播中的区块所携带信息是否正确，正确则广播prepare
     * */
    @Override
    public void prePrepareReceived(Message message) {
        log.info("prePrepareReceived(): received message="+message.toString());
        if(blockchainService.verifyBlock(message.getBlock(),message.getHeight(),message.getRound())){

        }
    }

    @Override
    public void prepareBroadcast(Message message) {

    }

    @Override
    public void prepareReceived(Message message) {

    }

    @Override
    public void commitBroadcast(Message message) {

    }

    @Override
    public void commitReceived(Message message) {

    }

    @Override
    public void viewChanged() {

    }

    @Override
    public void setup(Message message) {
        prePrepareBroadcast(message);
    }
}
