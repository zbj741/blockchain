package com.buaa.blockchain.contract.core;


import com.buaa.blockchain.contract.State;
import com.buaa.blockchain.contract.util.classloader.FileClassLoader;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
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
public class ContractManager {

    private static ContractManager instance = null;
    // 指定的合约class文件存放目录
    public static final String dir = System.getProperty("user.dir")+ File.separator + "contract" + File.separator;
    // 指定的合约前缀，用于生成载入时的全限定类名
    public static final String classPrefix = "com.buaa.blockchain.contract.develop.";
    // 合约表
    private HashMap<String,Class> contractMap;
    // 原生合约的硬编码表
    public static final String UPDATE = "UPDATE";
    public static final String DELETE = "DELETE";
    public static final String DEV = "DEV";
    // 原生合约方法表
    public HashSet<String> oriContracts;
    /**
     * 初始化方法
     * */
    private ContractManager(){
        // 初始化原生合约表
        this.oriContracts = OriginContract.getAllOriMethods();
        // 从文件系统中获取智能合约
        this.contractMap = new HashMap<>();
        File fd = new File(dir);
        // 遍历存放目录，加载智能合约
        for(File f : fd.listFiles()){
            // 获取合约名
            String contractName = f.getName().substring(0,f.getName().lastIndexOf("."));
            Class contract = loadContractFromFile(contractName,f.getAbsolutePath());
            if(null != contract){
                contractMap.put(contractName,contract);
                log.info("loadContractFromFile(): load contract: " + contractName + " from "+dir);
            }
        }
        // TODO 从statedb中提前寻找合约


    }
    /**
     * 单例
     * */
    public static synchronized ContractManager getInstance(){
        if(null == instance){
            instance = new ContractManager();
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
    public boolean invokeContract(State state, String contractName, Map<String, DataUnit> args){
        boolean res = true;
        // 先查看是否为原生合约
        if(this.oriContracts.contains(contractName)){

            return true;
        }
        // 从contractMap中找合约
        if(this.contractMap.containsKey(contractName)){
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
        else{
            // 从statedb中同步合约
            // TODO
        }
        return res;
    }


    /***************** Private Functions *****************/

    /**
     * 获取智能合约实例
     * 从contractMap中获取
     * */
     private Contract getContractInstance(String name){
        Contract res = null;
        try {
            res = (Contract) contractMap.get(name).newInstance();
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
     * */
    private Class loadContractFromState(String name, State state){

        return null;
    }
}
