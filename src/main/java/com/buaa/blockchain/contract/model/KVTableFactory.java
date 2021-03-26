package com.buaa.blockchain.contract.model;

import java.util.HashMap;

public class KVTableFactory {

    public KVTableFactory() {

    }

    public void createTable(String table_name, String id, String attr){

    }

    public KVTable openTable(String table_name){
        return new KVTable(new HashMap<>());
    }
}
