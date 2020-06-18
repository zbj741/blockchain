package com.buaa.blockchain.consensus;


import java.util.Set;

/**
 * 所有共识接口的父接口
 * */
public interface BaseConsensus<T> {
    String PBFT = "PBFT";
    String SBFT = "SBFT";
    /**
     * 开启一次共识
     * */
    void setup(T t);
    /**
     * 收到相关数据
     * */
    void onMessageReceived(T t);
    /**
     * 集群变动
     * */
    void onClusterChanger(Set<String> pre, Set<String> now);
}
