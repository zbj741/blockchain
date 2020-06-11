package com.buaa.blockchain.consensus;


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
}
