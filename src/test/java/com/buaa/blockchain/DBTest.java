package com.buaa.blockchain;

import com.buaa.blockchain.entity.Transaction;
import com.buaa.blockchain.mapper.TransactionMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest(classes = BlockchainApplication.class)
public class DBTest {


    @Autowired
    TransactionMapper transactionMapper;

    @Test
    void contextLoads() {
    }


    @Test
    public void testDB(){
        Transaction ts = Transaction.createDefaultTransaction();
        transactionMapper.insertTransaction(ts);
    }
}
