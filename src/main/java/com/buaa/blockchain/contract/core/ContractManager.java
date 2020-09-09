package com.buaa.blockchain.contract.core;


import com.buaa.blockchain.contract.State;
import com.buaa.blockchain.contract.account.Account;
import com.buaa.blockchain.contract.account.ContractAccount;
import com.buaa.blockchain.contract.account.ContractEntrance;
import com.buaa.blockchain.contract.util.classloader.ByteClassLoader;
import com.buaa.blockchain.contract.util.classloader.FileClassLoader;

import com.buaa.blockchain.utils.JsonUtil;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


/**
 * 合约管理器，用于将class文件形式的智能合约动态加载并且维护引用
 * 每一个合约的Class在ContractManager中注册后，就不用再次加载了，仅仅单例存在于系统中
 * 在生成contract实例时，使用Class，但是生成的contract可以多例存在于Vm中
 *
 * 暂时的设计中，合约不以账户的形式存在，仅仅作为一个可被调用的代码段
 * @author hitty
 * */

@Slf4j
public class ContractManager implements IContractManager{

    private static ContractManager instance = null;
    private ContractEntrance contractEntrance = null;
    // 指定的合约class文件存放目录
    public static final String dir = System.getProperty("user.dir")+ File.separator + "contract" + File.separator;
    // 合约表 <合约名，合约账户实例>
    private HashMap<String,ContractAccount> contractMap;
    // 原生合约的硬编码表
    public static final String UPDATE = "UPDATE";
    public static final String DELETE = "DELETE";
    public static final String DEV = "DEV";
    // 原生合约方法表
    public HashSet<String> oriContracts;

    /**
     * 初始化方法
     * */
    private ContractManager(State state){
        this.contractEntrance = ContractEntrance.getInstance(state);
        // 初始化原生合约表
        this.oriContracts = OriginContract.getAllOriMethods();
        this.contractMap = new HashMap<>();
        // 通过ContractEntrance维护的智能合约<name,key>映射表，从statedb中生成寻找智能合约账户实体类
        Map<String,String> map = this.contractEntrance.getContracts();
        for(Map.Entry<String,String> entry : map.entrySet()){
            ContractAccount contractAccount = loadContractAccountFromState(entry.getKey(), entry.getValue(),state);
            if(contractAccount != null){
                this.contractMap.put(contractAccount.getName(),contractAccount);
            }
        }
        log.info("ContractManager(): init done! origin="+oriContracts.toString()+" ,contractMap="+contractMap.toString());
    }
    /**
     * 单例
     * */
    public static synchronized ContractManager getInstance(State state){
        if(null == instance){
            instance = new ContractManager(state);
        }
        return instance;
    }


    /**
     * 调用智能合约
     * @param state 状态树
     * @param contractName  智能合约名
     * @param args  智能合约参数
     * @return 是否调用成功
     * */
    @Override
    public boolean invokeContract(State state, String contractName, Map<String, DataUnit> args){
        boolean res = true;
        // 先查看是否为原生合约
        if(this.oriContracts.contains(contractName)){
            // 是原生合约，调用原生合约
            OriginContract.invoke(state,contractName,args);
            return true;
        }
        // 尝试添加
        if(!this.contractMap.containsKey(contractName)){
            // 通过ContractEntrance，从statedb中同步合约到contractMap
            if(contractEntrance.getContracts().keySet().contains(contractName)){
                String key = contractEntrance.getContracts().get(contractName);
                ContractAccount account = new ContractAccount(state,key);
                this.contractMap.put(account.getName(),account);
            }else{
                // 没有该智能合约
                return false;
            }
        }
        try {
            Contract contract = this.getContractInstance(contractName);
            contract.initParam(args);
            contract.run(state);
            res = true;
        } catch (Exception e) {
            res = false;
            e.printStackTrace();
        } finally {
            return  res;
        }

    }

    @Override
    public boolean addContract(State state, String key, String contractName, byte[] classData) {
        ContractAccount contractAccount = new ContractAccount(contractName,contractName,contractName,classData);
        // 同步ContractEntrance
        contractEntrance.addContract(contractAccount);
        contractMap.put(contractName,contractAccount);
        return true;
    }


    /***************** Private Functions *****************/

    /**
     * 获取智能合约实例
     * 从contractMap中获取
     * */
     private Contract getContractInstance(String name){
        Contract res = null;
        try {
            res = (Contract) contractMap.get(name).getClazz().newInstance();
        } catch (InstantiationException e) {
            // TODO
            res = null;
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO
            res = null;
            e.printStackTrace();
        }finally {
            return res;
        }
    }


    /**
     * 从文件系统中获取智能合约到contractMap中
     * @param name 智能合约名
     * @param path 智能合约class文件的绝对路径
     * */
    private Class loadContractFromFile(String name,String path){
        Class res = null;
        try {
            res = FileClassLoader.getClass(path,classPrefix + name);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return res;
        }
    }
    /**
     * 从statedb中获取智能合约到contractMap中
     * @param name 智能合约名
     * @param key 智能合约在state中的key
     * @param state
     * @return 找到的智能合约账户
     * */
    private ContractAccount loadContractAccountFromState(String name, String key, State state){
        byte[] res = state.getAsBytes(key);
        try {
            ContractAccount contractAccount = (ContractAccount) JsonUtil.objectMapper.readValue(res,ContractAccount.class);
            if(name.equals(contractAccount.getName())){
                return contractAccount;
            }
        } catch (IOException e) {
            // TODO 处理读入异常
            e.printStackTrace();
        }
        return null;
    }
}
