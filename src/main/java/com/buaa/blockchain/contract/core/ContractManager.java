package com.buaa.blockchain.contract.core;


import com.buaa.blockchain.contract.MyClassLoader;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.HashMap;

/**
 * 合约管理器，用于将class文件形式的智能合约动态加载并且维护引用
 * 每一个合约的Class在ContractManager中注册后，就不用再次加载了，仅仅单例存在于系统中
 * 在生成contract实例时，使用Class，但是生成的contract可以多例存在于Vm中
 *
 * @author hitty
 * */

@Slf4j
public class ContractManager {

    private static ContractManager instance = null;
    // 指定的合约class文件存放目录
    private static final String dir = System.getProperty("user.dir")+ File.separator + "contract" + File.separator;
    // 指定的合约前缀，用于生成载入时的全限定类名
    private static final String classPrefix = "com.buaa.blockchain.contract.develop.";
    // 合约表
    private HashMap<String,Class> contractMap = new HashMap<>();

    /**
     * 初始化方法
     * */
    private ContractManager(){
        // 遍历存放目录，加载智能合约
        File fd = new File(dir);
        for(File f : fd.listFiles()){
            // 获取合约名
            String contractName = f.getName().substring(0,f.getName().lastIndexOf("."));
            Class contractClass = MyClassLoader.getClass(f.getAbsolutePath(),
                    classPrefix+contractName);
            if(null != contractClass){
                contractMap.put(contractName,contractClass);
                log.info("ContractManager(): load contract: " +contractName);
            }

        }


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
     * 获取智能合约实例
     * */
    public Contract getContractInstance(String name){
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

    public void addContract(String name, Class clazz){
        contractMap.put(name,clazz);
    }

}
