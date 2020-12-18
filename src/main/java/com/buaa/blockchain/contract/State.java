package com.buaa.blockchain.contract;

import com.buaa.blockchain.entity.UserAccount;

public interface State {
    String get(String key);
    void update(String key,String value);
    void delete(String key);
    byte[] getAsBytes(String key);
    void update(String key,byte[] data);

    /**
     * 账户相关
     * */
    int getUserAccountBalance(String userKey);
    void updateUserAccountBalance(String userKey, int updateVal);
    String getUserJsonString(String userKey);
    UserAccount getUser(String userKey);
}
