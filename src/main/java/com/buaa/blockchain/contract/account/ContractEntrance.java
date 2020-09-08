package com.buaa.blockchain.contract.account;


import com.buaa.blockchain.contract.State;

import java.util.HashSet;

/**
 * ContractEntrance维护了全部智能合约在statedb中的key值
 * 该数据存储在Account的data字段中，以List<String>的形式存在
 * 全局单例
 * */
public class ContractEntrance extends Account{
    // 入口在statedb中的key
    public static final String CONTRACT_ENTRANCE_ID = "CONTRACT_ENTRANCE_ID";
    // id
    private static ContractEntrance instance = null;

    HashSet<String> contracts;

    public static synchronized ContractEntrance getInstance(State state){
        if(null == instance){
            return new ContractEntrance(state);
        }else{
            return instance;
        }
    }
    private ContractEntrance(State state){
        super(CONTRACT_ENTRANCE_ID);
        contracts = new HashSet<>();
        // 查看当前statedb中是否已经存在
        if(hasConflict(state)){
            // 载入

        }
        // 插入state
        this.writeStateAsBytes(state);

    }


    @Override
    public void writeStateAsBytes(State state) {

    }
}
