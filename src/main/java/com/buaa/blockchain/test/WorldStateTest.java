package com.buaa.blockchain.test;

import com.buaa.blockchain.contract.WorldState;
import com.buaa.blockchain.entity.Transaction;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WorldStateTest {

    public static void main(String[] args) throws Exception{
        ObjectMapper objectMapper = new ObjectMapper();
        WorldState worldState = new WorldState("D:\\data","triedb",null);
        String str = worldState.get("ts");
        System.out.println(str);


        System.out.println(worldState.getRootHash());
        worldState.update("233","666");
        worldState.update("key","value");
        System.out.println(worldState.getRootHash());
        Transaction ts = Transaction.createDefaultTransaction();
        String tsstr = objectMapper.writeValueAsString(ts);
        worldState.update("ts",tsstr);
        System.out.println(worldState.getRootHash());
        worldState.sync();
        System.out.println(worldState.get("233"));
        System.out.println(worldState.get("ts"));
        System.out.println(worldState.getRootHash());
        for(int i = 0;i < 1000;i++){
            worldState.update(System.currentTimeMillis()+"","time");
        }
        worldState.sync();
        System.out.println(worldState.getRootHash());

        System.out.println(worldState.getRootHash());
        System.out.println(worldState.get("ts"));


    }
}
