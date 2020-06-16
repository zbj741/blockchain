package com.buaa.blockchain.trie.datasource;


public interface DataSource {

    void setName(String name);

    String getName();

    void init();

    boolean isAlive();
    
    void close();
}
