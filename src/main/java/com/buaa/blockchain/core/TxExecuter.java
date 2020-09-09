package com.buaa.blockchain.core;

import com.buaa.blockchain.contract.State;
import com.buaa.blockchain.contract.WorldState;
import com.buaa.blockchain.contract.core.ContractManager;
import com.buaa.blockchain.contract.core.DataUnit;
import com.buaa.blockchain.contract.core.IContractManager;
import com.buaa.blockchain.entity.Transaction;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 交易执行器
 * 用于将交易数据解包，并且发放给ContractManager来执行
 *
 * @author hitty
 * */
@Slf4j
public class TxExecuter {
    private static TxExecuter instance = null;
    private IContractManager contractManager;
    /**
     * 全局单例
     * */
    public synchronized static TxExecuter getInstance(State state){
        if(null == instance){
            instance = new TxExecuter(state);
        }
        return instance;
    }
    private TxExecuter(State state){
        // 初始化ContractManager
        this.contractManager = ContractManager.getInstance(state);
    }

    public void baseExecute(List<Transaction> transactionList, WorldState worldState){
        for(Transaction transaction : transactionList){
            baseSingleExecute(transaction,worldState);
        }
    }
    /**
     * 执行单个交易
     * 交易的执行，需要修改worldState，也许需要写数据库
     * */
    private void baseSingleExecute(Transaction transaction,WorldState worldState){
        // 交易拆包
        Map<String,DataUnit> args = new HashMap<>();
        args.put("KEY",new DataUnit("testKey"));
        args.put("VALUE", new DataUnit(transaction.getTran_hash()));
        contractManager.invokeContract(worldState,"update",args);
    }
}
