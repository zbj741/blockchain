package com.buaa.blockchain.contract.core;

import com.buaa.blockchain.contract.State;
import com.buaa.blockchain.contract.account.Account;
import com.buaa.blockchain.contract.account.ContractAccount;
import com.buaa.blockchain.contract.account.ContractEntrance;
import com.buaa.blockchain.contract.util.classloader.ByteClassLoader;
import com.buaa.blockchain.utils.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import static com.buaa.blockchain.contract.account.ContractEntrance.CONTRACT_ENTRANCE_KEY;

/**
 * 原生合约，每一个节点在启动时初始化的合约
 * 原生合约直接存在于源码中，不使用反射来调用，用于提高执行速度
 *
 * @author hitty
 * */

@Slf4j
public class OriginContract {
    // contract文件夹路径
    private static final String contractDir = System.getProperty("user.dir")+ File.separator + "contract" + File.separator;

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
        log.info("invoke(): "+function+", args="+args.toString());
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
        String value = args.get("VALUE").getValue();
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
        String contractName = args.get("CONTRACT_NAME").getString();
        byte[] classData = args.get("CONTRACT_BYTES").getByteArray();
        // 建立ContractAccount实例
        // TODO 暂时将智能合约用户的 key，id，name设为一样的，过后修改
        ContractAccount contractAccount = new ContractAccount(contractName,contractName,contractName,classData);
        boolean done = false;
        // 将自己写入state
        try {
            state.update(contractAccount.getKey(), JsonUtil.objectMapper.writeValueAsBytes(contractAccount));
            // 更新ContractEntrance
            ContractEntrance.getInstance().addContract(contractAccount);
            state.update(CONTRACT_ENTRANCE_KEY,JsonUtil.objectMapper.writeValueAsBytes(ContractEntrance.getInstance()));
            done = true;
            // 写入contract文件夹
            FileOutputStream fileRes = new FileOutputStream (new File(contractDir+contractName+".class"));
            fileRes.write(classData);
            fileRes.close();
        } catch (JsonProcessingException e) {
            // 写入错误则删除
            e.printStackTrace();
            done = false;
        }catch (IOException e){
            // 写入文件错误 非主要问题 暂时不处理
            log.warn("dev(): write class file failed!");
            e.printStackTrace();
        } finally {
            if(done){
                log.info("dev(): develop contract "+contractName+" successfully.");
                // 写入contract文件夹

            }else{
                state.delete(contractAccount.getKey());
                log.info("dev(): develop contract "+contractName+" failed, remove data from state at key="+contractAccount.getKey());
            }
        }

    }

}
