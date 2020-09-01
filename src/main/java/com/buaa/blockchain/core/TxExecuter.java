package com.buaa.blockchain.core;

import com.buaa.blockchain.entity.Transaction;

import java.util.List;
import java.util.Random;

public class TxExecuter {
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
        if(transaction.getSign().equals("0")){
            worldState.update(hash,hash);
        }else{
            try {
                int a = (new Random()).nextInt(200);
                if(a < 10){
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                worldState.update(hash,hash);
            }
        }

    }
}
