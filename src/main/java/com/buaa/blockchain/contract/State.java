package com.buaa.blockchain.contract;

public interface State {
    String get(String key);
    void update(String key,String value);
    void delete(String key);
}
