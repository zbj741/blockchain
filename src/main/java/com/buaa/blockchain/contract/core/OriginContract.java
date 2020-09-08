package com.buaa.blockchain.contract.core;

import com.buaa.blockchain.contract.State;
import com.buaa.blockchain.contract.util.classloader.ByteClassLoader;

import java.util.*;

/**
 * 原生合约，每一个节点在启动时初始化的合约
 * 原生合约直接存在于源码中，不使用反射来调用，用于提高执行速度
 *
 * @author hitty
 * */
public class OriginContract {
    // 原生合约提供的方法名
    public static final String UPDATE = "update";
    public static final String DELETE = "delete";
    public static final String DEV = "dev";

    private static HashSet<String> functions;

    static {
        functions = new HashSet<>();
        functions.add(UPDATE);
        functions.add(DELETE);
        functions.add(DEV);
    }
    /**
     * 获取全部的原生合约方法名
     * */
    public static HashSet<String> getAllOriMethods(){
        return functions;
    }

    /**
     * 调用入口
     * @param state statedb实例
     * @param function 方法名
     * @param args 参数
     * */
    public static void invoke(State state,String function,Map<String, DataUnit> args){
        // 分发函数的调用
        switch (function){
            case UPDATE:{
                update(state,args);
                break;
            }
            case DELETE:{
                delete(state,args);
                break;
            }
            case DEV:{
                dev(state,args);
                break;
            }
        }
        return ;
    }
    /**
     * 简单插入
     * */
    private static void update(State state, Map<String, DataUnit> args){
        String key = args.get("KEY").toString();
        String value = args.get("VALUE").value.toString();
        state.update(key,value);
    }
    /**
     * 简单删除
     * */
    private static void delete(State state, Map<String, DataUnit> args){
        String key = args.get("KEY").toString();
        state.delete(key);
    }
    /**
     * 智能合约部署
     * */
    private static void dev(State state, Map<String, DataUnit> args){
        String contractName = args.get("CONTRACT_NAME").value.toString();
        byte[] classData = (byte[]) args.get("CONTRACT_BYTES").value;
        // 写入
        state.update(contractName,classData);
        // 向ContractEntrance注册

    }
}
