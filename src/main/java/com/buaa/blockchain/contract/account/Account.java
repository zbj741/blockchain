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
    // 序列化工具
    public static ObjectMapper objectMapper = new ObjectMapper();
    static{
        // 转化为格式化的json
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        // 如果json中有新增的字段并且在实体类中不存在，不报错
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }

    public Account(){

    }
    public Account(String key){
        this(key,key);
    }
    public Account(String key, String id){
        this.key = key;
        this.id = id;
    }
    public Account(String key, String id,String name){
        this.key = key;
        this.id = id;
        this.name = name;
        this.balance = 0.0f;
    }

    /**
     * 将自身打包为Json，并且以byte数组的形式插入state中
     * 交由子类自身实现，仅仅提供Jackson支持
     * */
    public abstract void writeStateAsBytes(State state);

    /**
     * 查看是否有重复key，没有则返回true
     * */
    public boolean hasConflict(State state){
        return (state.get(this.key).length() != 0 || null != state.get(key));
    }

}
