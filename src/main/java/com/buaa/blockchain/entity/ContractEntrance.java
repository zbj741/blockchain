package com.buaa.blockchain.entity;


import com.buaa.blockchain.contract.State;
import com.buaa.blockchain.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ContractEntrance维护了全部智能合约在statedb中的key值
 * 该数据存储在Account的data字段中，以List<String>的形式存在
 * 只负责记录，不负责载入等工作，载入的工作交给ContractManager
 * 全局单例
 * */

@Slf4j
public class ContractEntrance{
    // 入口在statedb中的key
    public static final String CONTRACT_ENTRANCE_KEY = "CONTRACT_ENTRANCE_KEY";
    // 描述
    public static final String intro = "This is the entrance of all contract in this system, and will be created or find in the " +
            "initial process of blockchain project.";

    private static ContractEntrance instance = null;
    // 智能合约名字记录 <合约名，合约账户的key>
    private ConcurrentHashMap<String,String> contracts;

    String key;
    // 用户id
    String id;
    // 用户自己定义名字
    String name;
    // 余额
    int balance;
    // 数据
    String data;

    /**
     * 添加合约的<name,key>键值对
     * */
    public synchronized void addContract(ContractAccount contractAccount){
        contracts.put(contractAccount.cName,contractAccount.cKey);
    }

    public Map<String,String> getContracts(){
        return contracts;
    }

    /**
     * 单例模式，因为仅仅需要一个入口来访问所有的智能合约
     * */
    public static synchronized ContractEntrance getInstance(){
        if(null == instance){
            instance = new ContractEntrance();

        }
        return instance;

    }

    public static synchronized ContractEntrance getInstance(ContractEntrance c){
        if(null != c){
            instance = c;
            return instance;
        } else {
            return null;
        }
    }


    private ContractEntrance(){
        this.key = CONTRACT_ENTRANCE_KEY;
        this.contracts = new ConcurrentHashMap<>();
        this.data = intro;
    }


    /**
     * 拷贝属性字段
     * */
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
    void loadFromState(State state) {
        byte[] res = state.getAsBytes(this.key);
        try {
            ContractEntrance contractEntrance = (ContractEntrance) JsonUtil.objectMapper.readValue(res,ContractEntrance.class);
            this.copy(contractEntrance);
        } catch (IOException e) {
            // TODO 处理读入异常
            e.printStackTrace();
        }

    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "{key="+this.key+" , id="+this.id+" , contracts="+this.contracts.toString()+"}";
    }
}
