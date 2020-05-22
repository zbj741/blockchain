package com.buaa.blockchain.consensus;


/**
 * 所有共识接口的父接口
 * */
public interface BaseConsensus<T> {
    String PBFT = "PBFT";
    String SBFT = "SBFT";
    void setup(T t);
}
