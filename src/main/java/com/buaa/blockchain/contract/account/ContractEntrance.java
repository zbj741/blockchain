package com.buaa.blockchain.contract.account;


import com.buaa.blockchain.contract.State;
import com.buaa.blockchain.contract.core.Contract;
import com.buaa.blockchain.utils.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ContractEntrance维护了全部智能合约在statedb中的key值
 * 该数据存储在Account的data字段中，以List<String>的形式存在
 * 只负责记录，不负责载入等工作，载入的工作交给ContractManager
 * 全局单例
 * */

@Slf4j
public class ContractEntrance extends Account{
    // 入口在statedb中的key
    public static final String CONTRACT_ENTRANCE_KEY = "CONTRACT_ENTRANCE_KEY";

    private static ContractEntrance instance = null;
    // 智能合约名字记录 <合约名，合约账户的key>
    private ConcurrentHashMap<String,String> contracts;

    /**
     * 添加合约的<name,key>键值对
     * */
    public void addContract(ContractAccount contractAccount){
        contracts.put(contractAccount.name,contractAccount.key);
    }

    public Map<String,String> getContracts(){
        return contracts;
    }

    /**
     * 单例模式，因为仅仅需要一个入口来访问所有的智能合约
     * */
    public static synchronized ContractEntrance getInstance(State state){
        if(null == instance){
            return new ContractEntrance(state);
        }else{
            return instance;
        }
    }


    private ContractEntrance(State state){
        super(CONTRACT_ENTRANCE_KEY);
        this.contracts = new ConcurrentHashMap<>();
        // 查看当前statedb中是否已经存在
        if(hasConflict(state)){
            // 已存在载入
            this.loadFromState(state);
        }
        log.info("ContractEntrance(): init, this="+this.toString());
    }

    /**
     * 拷贝属性字段
     * */
    @Override
    public void copy(Object o) {
        ContractEntrance c = (ContractEntrance) o;
        this.key = c.key;
        this.id = c.id;
        this.name = c.name;
        this.balance = c.balance;
        this.data = c.data;
        // 不需要深拷贝，将引用传递即可
        this.contracts = c.contracts;
    }



    /**
     * 从state中读取自身
     * */
    @Override
    public void loadFromState(State state) {
        byte[] res = state.getAsBytes(this.key);
        try {
            ContractEntrance contractEntrance = (ContractEntrance) JsonUtil.objectMapper.readValue(res,ContractEntrance.class);
            this.copy(contractEntrance);
        } catch (IOException e) {
            // TODO 处理读入异常
            e.printStackTrace();
        }

    }

    @Override
    public String toString() {
        return "{key="+this.key+" , id="+this.id+" , contracts="+this.contracts.toString()+"}";
    }
}
