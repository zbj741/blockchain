package com.buaa.blockchain.contract.core;


import com.buaa.blockchain.contract.State;
import com.buaa.blockchain.entity.ContractAccount;
import com.buaa.blockchain.entity.ContractEntrance;
import com.buaa.blockchain.contract.util.classloader.FileClassLoader;

import com.buaa.blockchain.core.BlockchainService;
import com.buaa.blockchain.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * 合约管理器，用于将class文件形式的智能合约动态加载并且维护引用
 * 每一个合约的Class在ContractManager中注册后，就不用再次加载了，仅仅单例存在于系统中
 *
 * 暂时的设计中，合约以账户的形式存在
 * @author hitty
 * */

@Slf4j
public class ContractManager implements IContractManager{

    private static ContractManager instance = null;
    private ContractEntrance contractEntrance = null;
    // BlockchainService 的引用
    BlockchainService bs = null;
    // 指定的合约class文件存放目录
    public static final String dir = System.getProperty("user.dir")+ File.separator + "contract" + File.separator;
    // 合约表 <合约名，合约账户实例>
    private HashMap<String,ContractAccount> contractMap;
    // 原生合约的硬编码表
    public static final String UPDATE = "UPDATE";
    public static final String DELETE = "DELETE";
    public static final String DEV = "DEV";
    // 原生合约方法表
    public OriginContract oriContract;

    /**
     * 初始化方法
     * */
    private ContractManager(BlockchainService bs,State state){
        this.bs = bs;
        // 初始化合约入口，入口已经被初始化完毕
        this.contractEntrance = ContractEntrance.getInstance();
        // 初始化原生合约表
        this.oriContract = new OriginContract(bs);
        this.contractMap = new HashMap<>();
        // 通过ContractEntrance维护的智能合约<name,key>映射表，从statedb中生成寻找智能合约账户实体类
        Map<String,String> map = this.contractEntrance.getContracts();
        try{
            for(Map.Entry<String,String> entry : map.entrySet()){
                ContractAccount contractAccount = loadContractAccountFromState(entry.getKey(), entry.getValue(),state);
                if(contractAccount != null){
                    this.contractMap.put(contractAccount.getcName(),contractAccount);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        // TODO 将contractEntrance作为一个ContractAccount 存入mysql

        log.info("ContractManager(): init done! origin="+oriContract.getAllOriMethods().toString()+" ,contractMap="+contractMap.toString());
    }
    /**
     * 单例
     * */
    public static synchronized ContractManager getInstance(BlockchainService bs,State state){
        if(null == instance){
            instance = new ContractManager(bs,state);
        }
        return instance;
    }


    /**
     * 调用智能合约
     * @param state 状态树
     * @param contractName  智能合约名
     * @param args  智能合约参数
     * @param largeData 字节流
     * @return 是否调用成功
     * */
    @Override
    public boolean invokeContract(State state, String contractName, Map<String, DataUnit> args, byte[] largeData){
        log.warn("invokeContract(): enter.... contractName = "+contractName);
        boolean res = true;
        // 先查看是否为原生合约
        if(this.oriContract.getAllOriMethods().contains(contractName)){
            // 是原生合约，调用原生合约
            this.oriContract.invoke(state,contractName,args,largeData);
            return true;
        }
        // 查看当前是否缓存了对应的ContractAccount
        ContractAccount cac = null;
        log.info("invokeContract(): show "+contractEntrance.getContracts().toString());
        if(!this.contractMap.containsKey(contractName)){
            // 通过ContractEntrance，从statedb中同步合约到contractMap
            if(contractEntrance.getContracts().keySet().contains(contractName)){
                log.warn("invokeContract():"+contractName+" is in contractEntrance.");
                String key = contractEntrance.getContracts().get(contractName);
                cac = new ContractAccount(state,key);
                this.contractMap.put(cac.getcName(), cac);
            }else{
                // 没有该智能合约
                log.warn("invokeContract(): "+contractName+" not found!!");
                return false;
            }
        }else{
            // 找到了缓存
            log.warn("invokeContract(): "+contractName+" in the cache.");
            cac = this.contractMap.get(contractName);
        }
        log.info("invokeContract(): invoke cache "+cac.getcName()+" start load...");
        try {
            //cac.load();
            cac.loadJar();
            log.info("invokeContract(): invoke cache "+cac.getcName()+" load done.");
            // 转换为class实例
            Class clazz = cac.getClazz();
            // 反射调用，执行
            Contract contract = (Contract) clazz.newInstance();
            contract.initParam(args);
            contract.run(state);
            res = true;
        } catch (Exception e) {
            res = false;
            // TODO 处理调用异常
            e.printStackTrace();
        } finally {
            return  res;
        }

    }

    @Override
    public boolean addContract(State state, String key, String contractName, byte[] classData) {
        ContractAccount contractAccount = new ContractAccount(contractName,contractName,classData);
        // 同步ContractEntrance
        contractEntrance.addContract(contractAccount);
        contractMap.put(contractName,contractAccount);
        return true;
    }


    /***************** Private Functions *****************/

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
            if(name.equals(contractAccount.getcName())){
                return contractAccount;
            }
        } catch (IOException e) {
            // TODO 处理读入异常
            e.printStackTrace();
        }
        return null;
    }
}
