package com.buaa.blockchain;


import com.buaa.blockchain.datasource.KeyValueDataSource;
import com.buaa.blockchain.entity.Transaction;
import com.buaa.blockchain.mapper.TransactionMapper;

import com.buaa.blockchain.trie.TrieImpl;
import com.buaa.blockchain.trie.Values;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest(classes = BlockchainApplication.class)
public class TrieTest {


    @Autowired
    KeyValueDataSource levelDb;

    @Test
    void contextLoads() {
    }


    @Test
    public void testDB(){
        levelDb.init();
        TrieImpl trie = new TrieImpl(levelDb);

        trie.update("cat", "dog");
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0;i < 10;i++){
            stringBuilder.append("233666test");
        }
        trie.update("longdata",stringBuilder.toString());
        System.out.println("cat:"+new String(trie.get("cat")));
        System.out.println("longdata:"+new String(trie.get("longdata")));
        System.out.println("trie.getRootHash:"+trie.getRootHash());
        System.out.println("1.levelDb.get(rootHash):"+levelDb.get(trie.getRootHash()));
        //System.out.println(new String(levelDb.get("cat".getBytes())));
        trie.sync();
        System.out.println("trie.getRootHash:"+trie.getRootHash());
        System.out.println("2.levelDb.get(rootHash):"+levelDb.get(trie.getRootHash()));
        System.out.println("trie.getRootHash:"+trie.getRootHash());
        System.out.println("3.levelDb.get(rootHash):"+levelDb.get(trie.getRootHash()));

        Values val1 = Values.fromRlpEncoded(levelDb.get(trie.getRootHash()));
        Values val2 = Values.fromRlpEncoded(levelDb.get(trie.getRootHash()));
        System.out.println(val1.toString());
        System.out.println(val2.toString());
        levelDb.close();

        levelDb.init();

        TrieImpl trie2 = new TrieImpl(levelDb,val1.asObj());
        System.out.println("cat:"+new String(trie2.get("cat")));
        System.out.println("longdata:"+new String(trie2.get("longdata")));

        levelDb.close();
    }
}
