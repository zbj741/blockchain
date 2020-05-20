package com.buaa.blockchain.entity;



import lombok.Data;

import java.io.Serializable;

/**
 * 广播的消息实体
 *
 * @author hitty
 * */
@Data
public class Message implements Serializable {

    // 广播消息的主题
    public static final String MESSAGE_TOPIC_TEST = "MESSAGE_TOPIC_TEST";
    public static final String MESSAGE_TOPIC_DIGEST = "MESSAGE_TOPIC_DIGEST";
    public static final String MESSAGE_TOPIC_VOTE = "MESSAGE_TOPIC_VOTE";
    public static final String MESSAGE_TOPIC_SYNC = "MESSAGE_TOPIC_SYNC";
    public static final String MESSAGE_TOPIC_SYNC_REPLY = "MESSAGE_TOPIC_SYNC_REPLY";
    public static final String MESSAGE_TOPIC_EXECUTE = "MESSAGE_TOPIC_EXECUTE";
    public static final String MESSAGE_TOPIC_DROP = "MESSAGE_TOPIC_DROP";

    private String topic;
    private String nodeName;
    private int height;
    private int round;
    private Boolean vote;
    private Block block;

    public Message(){
        this.topic = MESSAGE_TOPIC_TEST;
        this.nodeName = "";
        this.height = -1;
        this.round = -1;
        this.vote = false;
        this.block = new Block();
    }

    public Message(String topic,String nodeName,int height,int round,Block block){
        this.topic = topic;
        this.nodeName = nodeName;
        this.height = height;
        this.round = round;
        this.vote = false;
        this.block = block;
    }

    public Message(String topic,String nodeName,int height,int round,Boolean vote,Block block){
        this.topic = topic;
        this.nodeName = nodeName;
        this.height = height;
        this.round = round;
        this.vote = vote;
        this.block = block;
    }

    public String toString(){
        return "topic="+topic+", nodeName="+nodeName+", height="+height+", round="+round+", vote="+vote+
                "block="+block.toString();
    }
    
}
