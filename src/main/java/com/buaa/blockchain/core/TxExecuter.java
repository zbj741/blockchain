package com.buaa.blockchain.core;

import com.buaa.blockchain.entity.Transaction;

import java.util.List;

public class TxExecuter {
    public static void baseExecute(List<Transaction> transactionList,WorldState worldState){
        for(Transaction transaction : transactionList){
            baseSingleExecute(transaction,worldState);
        }
        worldState.sync();
    }
    /**
     * 执行单个交易
     * 交易的执行，需要修改worldState，也许需要写数据库
     * */
    public static void baseSingleExecute(Transaction transaction,WorldState worldState){
        //TODO 当下为测试交易执行，仅仅写入交易的hash
        String hash = transaction.getTran_hash();
        worldState.update32(hash,hash);
    }
}
