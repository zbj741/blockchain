package com.buaa.blockchain.contract.trie;

import com.buaa.blockchain.contract.trie.datasource.KeyValueDataSource;
import com.buaa.blockchain.contract.trie.datasource.LevelDbDataSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 测试Trie和levelDb
 *
 * 测试结论：trie模块用于k-v形式的存储，不要直接操作leveldb，因为不知道leveldb里面key是什么样子的，只知道trie里面key是什么样子的
 *
 * @author hitty
 *
 * */

public class TrieTest {
    private static final Logger logger = LoggerFactory.getLogger("transaction");

    //public static HashMapDB mockDb = new HashMapDB();
    private static KeyValueDataSource levelDb = new LevelDbDataSource("D:\\data","triedb");

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        //BasicConfigurator.configure();

        //KeyValueDataSource levelDb = new LevelDbDataSource("triedb");
        levelDb.init();
        ObjectMapper objectMapper = new ObjectMapper();


        // 当存入的数据不足时，就算执行sync也不会flush到leveldb里面，具体参考源码
        TrieImpl trie = new TrieImpl(levelDb);
        System.out.println("trie.getRootHash()"+trie.getRootHash());
        for(byte b : trie.getRootHash()){
            System.out.print(b);
        }
        System.out.println("\nlevelDb:"+levelDb.get(trie.getRootHash()));

        // 新加入一个数据，这个trie变动，root变动
        trie.update("cat", "dog");
        System.out.println("trie.getRootHash()"+trie.getRootHash());
        for(byte b : trie.getRootHash()){
            System.out.print(b);
        }
        System.out.println("\nlevelDb:"+levelDb.get(trie.getRootHash()));
        // undo
        trie.undo();
        System.out.println("trie.getRootHash()"+trie.getRootHash());
        for(byte b : trie.getRootHash()){
            System.out.print(b);
        }
        System.out.println("\nlevelDb:"+levelDb.get(trie.getRootHash()));


        // 尝试一个长一点的数据
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0;i < 100;i++){
            stringBuilder.append("233666test");
        }
        trie.update("longdata",stringBuilder.toString());
        System.out.println("cat:"+new String(trie.get("cat")));
        System.out.println("longdata:"+new String(trie.get("longdata")));

        // 尝试获取
        System.out.println("before sync");
        System.out.println("trie.getRootHash:"+trie.getRootHash());
        for(byte b : trie.getRootHash()){
            System.out.print(b);
        }
        System.out.println("\nlevelDb:"+levelDb.get(trie.getRootHash()));


        trie.sync();

        System.out.println("after sync");
        System.out.println("trie.getRootHash:"+trie.getRootHash());
        for(byte b : trie.getRootHash()){
            System.out.print(b);
        }
        System.out.println("\nlevelDb:"+levelDb.get(trie.getRootHash()));


        // 根节点的val存的东西是用来指向其他地方的数据
        Values val1 = Values.fromRlpEncoded(levelDb.get(trie.getRootHash()));
        Values val2 = Values.fromRlpEncoded(levelDb.get(trie.getRootHash()));
        System.out.println(val1.toString());
        System.out.println(val2.toString());

        levelDb.close();

        levelDb.init();
        // 重开
        TrieImpl trie2 = new TrieImpl(levelDb,val1.asObj());
        System.out.println("cat:"+new String(trie2.get("cat")));
        System.out.println("longdata:"+new String(trie2.get("longdata")));

        levelDb.close();
        levelDb.init();
        TrieImpl trie3 = new TrieImpl(levelDb);
        System.out.println("cat:"+new String(trie2.get("cat")));

    }

}
