package com.buaa.blockchain.core;

import com.buaa.blockchain.contract.WorldState;
import com.buaa.blockchain.contract.core.DataUnit;
import com.buaa.blockchain.entity.Transaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TxExecuter {
    static String KEY = "KEY";
    static String VALUE = "VALUE";
    static Map<String, DataUnit> args = new HashMap<>();


    public static void baseExecute(List<Transaction> transactionList, WorldState worldState){
        for(Transaction transaction : transactionList){
            baseSingleExecute(transaction,worldState);
        }
    }
    /**
     * 执行单个交易
     * 交易的执行，需要修改worldState，也许需要写数据库
     * */
    public static void baseSingleExecute(Transaction transaction,WorldState worldState){
        //TODO 当下为测试交易执行，仅仅写入交易的hash
        String hash = transaction.getTran_hash();
        args.put(KEY,new DataUnit(hash));
        args.put(VALUE,new DataUnit(hash));


    }
}
