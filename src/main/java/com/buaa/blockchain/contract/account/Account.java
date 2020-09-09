package com.buaa.blockchain.contract.account;


import com.buaa.blockchain.contract.State;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Data;

import java.io.IOException;

/**
 * 区块链中的账户类
 * */
public abstract class Account {
    String key;
    // 用户id
    String id;
    // 用户自己定义名字
    String name;
    // 余额
    float balance;
    // 数据
    String data;


    public Account(){

    }
    public Account(String key){
        this(key,key);
    }
    public Account(String key, String id){
        this.key = key;
        this.id = id;
        this.name = "";
        this.balance = 0.0f;
    }
    public Account(String key, String id,String name){
        this.key = key;
        this.id = id;
        this.name = name;
        this.balance = 0.0f;
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

    public float getBalance() {
        return balance;
    }

    public void setBalance(float balance) {
        this.balance = balance;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    /**
     * 从state中还原自身
     * 交由子类实现
     * */
    public abstract void loadFromState(State state);

    /**
     * 复制属性
     * 参数由调用者去强转类型，转为自己的类型
     * */
    abstract void copy(Object o);

    /**
     * 查看是否有重复key，没有则返回true
     * */
    boolean hasConflict(State state){
        boolean res = (state.get(this.key).length() != 0);
        return res;
    }

}
