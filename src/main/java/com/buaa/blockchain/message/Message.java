package com.buaa.blockchain.message;



import com.buaa.blockchain.entity.Block;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 消息实体
 * 用于core的通信，但不是MessageService中的传输实体
 *
 * @author hitty
 * */
@Data
public class Message implements Serializable {

    private String senderAddress;
    private String receiverAddress;
    private String topic;
    private String nodeName;
    private Long height;
    private Long round;
    private Boolean vote;
    private Block block;
    private List<Block> blockList;

    public Message(){}

    /**
     * 无topic，一般用于共识协议的发起
     * */
    public Message(String nodeName,Long height,Long round,Block block){
        this.topic = "";
        this.nodeName = nodeName;
        this.height = height;
        this.round = round;
        this.vote = false;
        this.block = block;
    }

    /**
     * 注明topic
     * */
    public Message(String topic,String nodeName,Long height,Long round,Boolean vote,Block block){
        this.topic = topic;
        this.nodeName = nodeName;
        this.height = height;
        this.round = round;
        this.vote = vote;
        this.block = block;
    }

    /**
     * 用于同步区块
     * */
    public Message(String topic,String senderAddress,Long height, List<Block> blockList){
        this.topic = topic;
        this.senderAddress = senderAddress;
        this.height = height;
        this.blockList = blockList;
    }



    public String toString(){
        return "topic="+topic+", nodeName="+nodeName+", height="+height+", round="+round+", vote="+vote+
                ", block="+block.getHash();
    }
    
}
