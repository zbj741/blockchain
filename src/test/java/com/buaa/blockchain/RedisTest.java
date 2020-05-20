package com.buaa.blockchain;

import com.buaa.blockchain.entity.Transaction;
import com.buaa.blockchain.txpool.RedisTxPool;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class RedisTest {

    @Autowired
    RedisTxPool redisTxpool;

    @Test
    void contextLoads() {
    }


    @Test
    public void testRedis(){

        for(int i = 1;i < 1000;i++){
            Transaction tran = Transaction.createDefaultTransaction();
            redisTxpool.put("hash",i+"",tran);
        }

        for(Transaction transaction : redisTxpool.getList("hash",100)){
            System.out.println(transaction.toString());
        }
//        transactionHashOperations.put("hash","1",tran);
//        System.out.println(transactionHashOperations.get("hash","1"));
    }
}
