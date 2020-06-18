package com.buaa.blockchain.contract;

/**
 * 基于Java的智能合约
 * 智能合约使用Java语言编写，并且提供一定的计算功能和对于Trie的修改功能
 *
 * @author hitty
 * */
public interface BaseContract<T> {
    void update(T t,String key,String value);
    String get(T t,String key);
    void delete(T t,String key);
}
