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
    
    private String topic;
    private String nodeName;
    private int height;
    private int round;
    private Boolean vote;
    private Block block;

    public Message(){}

    public Message(String nodeName,int height,int round,Block block){
        this.topic = "";
        this.nodeName = nodeName;
        this.height = height;
        this.round = round;
        this.vote = false;
        this.block = block;
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
                "block="+block.getHash();
    }
    
}
