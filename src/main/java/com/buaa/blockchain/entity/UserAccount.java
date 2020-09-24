package com.buaa.blockchain.entity;


public class UserAccount {

    String userKey;
    String userName;
    String password;
    String intro;
    int balance;
    String data;
    public UserAccount(){}
    public UserAccount( String key, String name, String password, String intro, int balance, String data){

        this.userKey = key;
        this.userName = name;
        this.password = password;
        this.intro = intro;
        this.balance = balance;
        this.data = data;
    }


    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
