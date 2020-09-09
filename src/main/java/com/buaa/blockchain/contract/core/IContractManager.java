package com.buaa.blockchain.contract.core;

import com.buaa.blockchain.contract.State;

import java.util.Map;

/**
 * 智能合约管理器
 * 用于向区块链上层的core.TxExecuter来服务
 * 区块链执行交易的功能模块不需要关注交易的具体执行情况，此类用来封装交易对应智能合约的具体执行情况
 *
 * @author hitty
 * */
public interface IContractManager {
    // 指定的合约前缀，用于生成载入时的全限定类名
    String classPrefix = "com.buaa.blockchain.contract.develop.";
    /**
     * 智能合约调用
     * */
    boolean invokeContract(State state, String contractName, Map<String, DataUnit> args);
    /**
     * 智能合约添加
     * */
    boolean addContract(State state, String key, String contractName, byte[] classData);

}
