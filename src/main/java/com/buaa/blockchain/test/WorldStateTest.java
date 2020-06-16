package com.buaa.blockchain.test;

import com.buaa.blockchain.core.WorldState;
import com.buaa.blockchain.trie.datasource.KeyValueDataSource;
import com.buaa.blockchain.trie.datasource.LevelDbDataSource;
import com.buaa.blockchain.entity.Transaction;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WorldStateTest {
    private static KeyValueDataSource levelDb = new LevelDbDataSource("D:\\Develop\\PRJ\\buaa-blockchain\\levelDb","triedb");
    public static void main(String[] args) throws Exception{
        ObjectMapper objectMapper = new ObjectMapper();
        WorldState worldState = new WorldState(levelDb);
        System.out.println(worldState.get32("ts"));


        System.out.println(worldState.getRootHash());
        worldState.update32("233","666");
        worldState.update32("key","value");
        System.out.println(worldState.getRootHash());
        Transaction ts = Transaction.createDefaultTransaction();
        String tsstr = objectMapper.writeValueAsString(ts);
        worldState.update32("ts",tsstr);
        System.out.println(worldState.getRootHash());
        worldState.sync();
        System.out.println(worldState.get32("233"));
        System.out.println(worldState.get32("ts"));
        System.out.println(worldState.getRootHash());
        for(int i = 0;i < 1000;i++){
            worldState.update32(System.currentTimeMillis()+"","time");
        }
        worldState.sync();
        System.out.println(worldState.getRootHash());

        System.out.println(worldState.getRootHash());
        System.out.println(worldState.get32("ts"));


    }
}
