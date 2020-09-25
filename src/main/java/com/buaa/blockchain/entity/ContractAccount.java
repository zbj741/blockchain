package com.buaa.blockchain.entity;

import com.buaa.blockchain.contract.State;
import com.buaa.blockchain.contract.core.Contract;
import com.buaa.blockchain.contract.core.DataUnit;
import com.buaa.blockchain.contract.core.IContractManager;
import com.buaa.blockchain.contract.util.classloader.ByteClassLoader;
import com.buaa.blockchain.contract.util.classloader.FileClassLoader;
import com.buaa.blockchain.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
 * 智能合约的合约账户实体类
 *
 * 智能合约由用户完成后，以交易的形式发送到区块链中
 * 区块链使用OriginalContract中的dev方法来部署智能合约
 * 被部署的智能合约以账户的形式存在，通过ContractEntrance账户中的记录，区块链可以从state中获取智能合约的class文件
 * 通过ByteClassLoader将class文件载入ContractManager中，调用
 *
 * @author hitty
 * */
@Slf4j
public class ContractAccount{
    static String contractDir = System.getProperty("user.dir")+ File.separator + "contract" + File.separator;
    // state中的key
    String cKey;
    // 合约名
    String cName;
    // 余额
    int balance;
    // 数据
    String data;
    // 合约Class
    Class clazz;
    // 类class文件字节码
    byte[] classData;
    // 全限定类名
    String fullName;
    // 介绍
    String intro = "";

    public ContractAccount(){}
    /**
     * 参数中存在classData，则直接生成Class
     * */
    public ContractAccount(String key,String name,byte[] classData){
        this.cKey = key;
        this.cName = name;
        this.fullName = IContractManager.classPrefix + this.cName;
        this.classData = classData;
    }

    public ContractAccount(State state, String key){
        this.cKey = key;
        loadFromState(state);
    }

    public void loadJar(){
        System.out.println("load():  -----------------start--------------");
        log.warn("load():  -----------------start--------------");
        if(this.clazz == null){
            String contractName = IContractManager.classPrefix + cName;
            String softPath = "file:"+contractDir+"ChangeBalance.jar";
            try {
                URLClassLoader classLoader = new URLClassLoader(new URL[]{new URL(softPath)},Thread.currentThread().getContextClassLoader());
                Class demo = classLoader.loadClass(contractName);
                Contract object = (Contract) demo.newInstance();
                System.out.println(object.getName());
                System.out.println("EXECUTE JAR");
                this.clazz = demo;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }
        System.out.println("load(): "+clazz.toString());
        log.warn("load(): "+clazz.toString());
        if(clazz == null){
            log.error("load(): load class failed, fullname="+fullName);
        }
    }

    /**
     * 将byte[]生成Class
     * */
    public void load(){
        System.out.println("load():  -----------------start--------------");
        log.warn("load():  -----------------start--------------");
        if(this.clazz == null){
            this.fullName = IContractManager.classPrefix + this.cName;
            // this.clazz = ByteClassLoader.getClass(this.classData, fullName);
            // String abPath = System.getProperty("user.dir")+ File.separator + "contract" + File.separator + cName + ".class";
            //this.clazz = FileClassLoader.getClass(abPath,fullName);
            String softPath = "file:"+System.getProperty("user.dir")+ File.separator + "contract" + File.separator+cName+".class";
            try {
                URLClassLoader classLoader = new URLClassLoader(new URL[]{new URL(softPath)},Thread.currentThread().getContextClassLoader());
                Class demo = classLoader.loadClass(fullName);
                this.clazz = demo;
            }catch (Exception e){
                e.printStackTrace();
            }

        }
        System.out.println("load(): "+clazz.toString());
        log.warn("load(): "+clazz.toString());
        if(clazz == null){
            log.error("load(): load class failed, fullname="+fullName);
        }
    }


    public void loadFromState(State state) {
        byte[] res = state.getAsBytes(this.cKey);
        try {
            ContractAccount contractAccount = (ContractAccount) JsonUtil.objectMapper.readValue(res,ContractAccount.class);
            this.copy(contractAccount);
        } catch (IOException e) {
            // TODO 处理读入异常
            e.printStackTrace();
        }
    }


    void copy(Object o) {
        ContractAccount c = (ContractAccount) o;
        this.cKey = c.cKey;

        this.cName = c.cName;
        this.balance = c.balance;
        this.data = c.data;
        // 不需要深拷贝，将引用传递即可
        this.classData = c.classData;

    }

    public String getcKey() {
        return cKey;
    }

    public void setcKey(String cKey) {
        this.cKey = cKey;
    }

    public String getcName() {
        return cName;
    }

    public void setcName(String cName) {
        this.cName = cName;
    }

    public float getBalance() {
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

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public byte[] getClassData() {
        return classData;
    }

    public void setClassData(byte[] classData) {
        this.classData = classData;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
