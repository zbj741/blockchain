package com.buaa.blockchain.contract.core;

import com.buaa.blockchain.contract.State;
import com.buaa.blockchain.entity.ContractAccount;
import com.buaa.blockchain.entity.ContractEntrance;
import com.buaa.blockchain.entity.UserAccount;
import com.buaa.blockchain.core.BlockchainService;
import com.buaa.blockchain.utils.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import static com.buaa.blockchain.entity.ContractEntrance.CONTRACT_ENTRANCE_KEY;

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
    // BlockchainService的引用
    BlockchainService bs = null;
    // 原生合约提供的方法名
    public static final String UPDATE = "update";
    public static final String DELETE = "delete";
    public static final String DEV = "dev";
    public static final String ADDUSER = "addUser";
    public static final String TRANSFER = "transfer";
    private HashSet<String> functions;

    public OriginContract(BlockchainService bs){
        this.bs = bs;
        this.functions = new HashSet<>();
        functions = new HashSet<>();
        functions.add(UPDATE);
        functions.add(DELETE);
        functions.add(DEV);
        functions.add(ADDUSER);
        functions.add(TRANSFER);
    }

    /**
     * 获取全部的原生合约方法名
     * */
    public HashSet<String> getAllOriMethods(){
        return functions;
    }

    /**
     * 调用入口
     * @param state statedb实例
     * @param function 方法名
     * @param args 参数
     * */
    public void invoke(State state,String function,Map<String, DataUnit> args){
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
            case ADDUSER:{
                addUser(state,args);
                break;
            }
            case TRANSFER:{
                transfer(state,args);
                break;
            }
            default:{
                log.warn("invoke(): contract "+function+" not found in OriginContract!");
                break;
            }
        }
        return ;
    }
    /**
     * 简单插入
     * */
    private void update(State state, Map<String, DataUnit> args){
        String key = args.get("KEY").toString();
        String value = args.get("VALUE").getValue();
        state.update(key,value);
    }
    /**
     * 简单删除
     * */
    private void delete(State state, Map<String, DataUnit> args){
        String key = args.get("KEY").toString();
        state.delete(key);
    }
    /**
     * 建立普通用户账户
     * */
    private void addUser(State state, Map<String, DataUnit> args){
        // 获取参数
        String key = args.get("KEY").getString();
        String name = args.get("NAME").getString();
        String password = args.get("PASSWORD").getString();
        String intro = args.get("INTRO").getString();
        String data = args.get("DATA").getString();
        int balance = args.get("BALANCE").getInteger();
        // 生成实体类
        UserAccount userAccount = new UserAccount(key,name,password,intro,balance,data);
        // 存入state
        try {
            state.update(userAccount.getUserName(), JsonUtil.objectMapper.writeValueAsBytes(userAccount));
        } catch (JsonProcessingException e) {
            // TODO 生成userAccount错误
            e.printStackTrace();
        }
        // 存入mysql
        bs.insertUserAccount(userAccount);
    }
    /**
     * 简单转账（用户）
     * */
    private void transfer(State state, Map<String, DataUnit> args){
        String ua1 = args.get("USERACCOUNT1").getString();
        String ua2 = args.get("USERACCOUNT2").getString();
        int t = args.get("AMOUNT").getInteger();
        UserAccount userAccount1 = null;
        UserAccount userAccount2 = null;
        try {
            userAccount1 = JsonUtil.objectMapper.readValue(state.get(ua1),UserAccount.class);
            userAccount2 = JsonUtil.objectMapper.readValue(state.get(ua2),UserAccount.class);
        } catch (JsonProcessingException e) {
            // TODO 读取失败,转账停止
            e.printStackTrace();
        }
        int pre1 = userAccount1.getBalance();
        int pre2 = userAccount2.getBalance();
        userAccount1.setBalance(pre1 - t);
        userAccount2.setBalance(pre2 + t);
        log.info("transfer(): "+userAccount1.getUserName()+" transfer t="+t+" to "+userAccount2.getUserName()+"; "
                +" [balance]:"+userAccount1.getUserName()+":"+pre1+"->"+userAccount1.getBalance()+", "+
                userAccount2.getUserName()+":"+pre2+"->"+userAccount2.getBalance());
        // 写回state
        try{
            state.update(userAccount1.getUserName(), JsonUtil.objectMapper.writeValueAsBytes(userAccount1));
            state.update(userAccount2.getUserName(), JsonUtil.objectMapper.writeValueAsBytes(userAccount2));
        }catch (JsonProcessingException e) {
            // TODO 写回失败,转账停止
            e.printStackTrace();
        }
        // 写回mysql
        bs.updateUserAccountBalance(userAccount1.getUserName(),userAccount1.getBalance());
        bs.updateUserAccountBalance(userAccount2.getUserName(),userAccount2.getBalance());

    }
    /**
     * 智能合约部署
     * */
    private void dev(State state, Map<String, DataUnit> args){
        String contractName = args.get("CONTRACT_NAME").getString();
        byte[] classData = args.get("CONTRACT_BYTES").getByteArray();
        // 建立ContractAccount实例
        // TODO 暂时将智能合约用户的 key，id，name设为一样的，过后修改
        ContractAccount contractAccount = new ContractAccount(contractName,contractName,classData);
        boolean done = false;
        // 将自己写入state
        try {
            state.update(contractAccount.getcKey(), JsonUtil.objectMapper.writeValueAsBytes(contractAccount));
            // 更新ContractEntrance
            ContractEntrance.getInstance().addContract(contractAccount);
            state.update(CONTRACT_ENTRANCE_KEY,JsonUtil.objectMapper.writeValueAsBytes(ContractEntrance.getInstance()));
            done = true;
            // 写入contract文件夹
            FileOutputStream fileRes = new FileOutputStream (new File(contractDir+contractName+".class"));
            fileRes.write(classData);
            fileRes.close();
            // 写入mysql
            bs.insertContractAccount(contractAccount);

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
                state.delete(contractAccount.getcKey());
                log.info("dev(): develop contract "+contractName+" failed, remove data from state at key="+contractAccount.getcKey());
            }
        }

    }

}
