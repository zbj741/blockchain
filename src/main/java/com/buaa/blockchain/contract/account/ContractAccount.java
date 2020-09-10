package com.buaa.blockchain.contract.account;

import com.buaa.blockchain.contract.State;
import com.buaa.blockchain.contract.core.Contract;
import com.buaa.blockchain.contract.core.DataUnit;
import com.buaa.blockchain.contract.core.IContractManager;
import com.buaa.blockchain.contract.util.classloader.ByteClassLoader;
import com.buaa.blockchain.utils.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.util.Contracts;

import java.io.IOException;
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
public class ContractAccount extends Account{
    // 合约Class
    Class clazz;
    // 类class文件字节码
    byte[] classData;
    // 全限定类名
    String fullName;

    public ContractAccount(){}
    /**
     * 参数中存在classData，则直接生成Class
     * */
    public ContractAccount(String key,String id,String name,byte[] classData){
        this.key = key;
        this.id = id;
        this.name = name;
        this.classData = classData;
    }

    public ContractAccount(State state, String key){
        this.key = key;
        loadFromState(state);
    }

    /**
     * 将byte[]生成Class
     * */
    public void load(){
        if(this.clazz == null){
            this.fullName = IContractManager.classPrefix + this.name;
            try {
                this.clazz = ByteClassLoader.getClass(this.classData, fullName);
            } catch (ClassNotFoundException e) {
                // TODO 处理读入异常
                e.printStackTrace();
                clazz = null;
            }
        }

    }

    @Override
    public void loadFromState(State state) {
        byte[] res = state.getAsBytes(this.key);
        try {
            ContractAccount contractAccount = (ContractAccount) JsonUtil.objectMapper.readValue(res,ContractAccount.class);
            this.copy(contractAccount);
        } catch (IOException e) {
            // TODO 处理读入异常
            e.printStackTrace();
        }
    }

    @Override
    void copy(Object o) {
        ContractAccount c = (ContractAccount) o;
        this.key = c.key;
        this.id = c.id;
        this.name = c.name;
        this.balance = c.balance;
        this.data = c.data;
        // 不需要深拷贝，将引用传递即可
        this.classData = c.classData;

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
