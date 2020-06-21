package com.buaa.blockchain.txpool;


import com.buaa.blockchain.entity.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 交易池的Redis实现
 * 包装RedisConfig生成的template和operation，完成对于交易的各种操作
 *
 * @author hitty
 *
 * */
@Component
public class RedisTxPool implements TxPool{

    private final HashOperations<String,String,Transaction> transactionHashOperations;
    @Autowired
    public RedisTxPool(HashOperations<String, String, Transaction> transactionHashOperations) {
        this.transactionHashOperations = transactionHashOperations;
    }

    /**
     * 获取指定hash值集合的交易
     * @param hash 集合的hash值
     * @param tran_hash 交易的hash值
     * @return 交易
     * */
    @Override
    public Transaction get(String hash,String tran_hash){
        return transactionHashOperations.get(hash,tran_hash);
    }

    /**
     * 写入指定hash值集合的交易
     * @param hash 集合的hash值
     * @param tran_hash 交易的hash值
     * */
    @Override
    public void put(String hash,String tran_hash,Transaction transaction){
        transactionHashOperations.put(hash,tran_hash,transaction);
    }

    /**
     * 删除指定hash值集合的交易
     * @param hash 集合的hash值
     * @param tran_hash 交易的hash值
     * */
    @Override
    public void delete(String hash,String tran_hash){
        transactionHashOperations.delete(hash,tran_hash);
    }

    /**
     * 获取指定hash值集合的长度
     * @param hash 集合的hash值
     * @return 该集合长度
     * */
    @Override
    public Long size(String hash){
        return transactionHashOperations.size(hash);
    }

    /**
     * 获取指定hash值集合指定长度元素列表
     * @param hash
     * @param size
     * @return
     * */
    @Override
    public List<Transaction> getList(String hash,int size){
        List<Transaction> result = new ArrayList<>();
        if(size <= 0){
            return result;
        }
        int count = 0;
        Iterator iterator = transactionHashOperations.entries(hash).entrySet().iterator();
        while(iterator.hasNext() && count++ < size){
            Transaction tmp = (Transaction) ((Entry) iterator.next()).getValue();
            result.add(tmp);
        }
        return result;
    }

    /**
     * 获取指定hash值集合的迭代器
     * @param hash 集合的hash值
     * @return 该集合迭代器
     * */
    public Iterator<Map.Entry<String,Transaction>> getIterator(String hash){
        return transactionHashOperations.entries(hash).entrySet().iterator();
    }


}
