package com.buaa.blockchain.contract;

import com.buaa.blockchain.entity.UserAccount;

import java.math.BigInteger;
import java.util.Map;

public interface State {

    String get(String key);

    byte[] getAsBytes(String key);

    void update(String key,byte[] data);

    void update(String key,String value);

    void delete(String key);

    void createAccount(String address, UserAccount userAccount);

    boolean isExistAccount(String address);

    boolean isContractAccount(String address);

    BigInteger getBalance(String address);

    void addBalance(String address, BigInteger val);

    boolean refreshStorage(String address, byte[] storageHash);

    UserAccount getUser(String address);

    Map getContractStorage(String address);

    byte[] getCode(String address);
}
