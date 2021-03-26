package com.buaa.blockchain.entity;


import java.math.BigInteger;

public class UserAccount {
    private BigInteger balance;
    private BigInteger nonce;
    private byte[] codeHash;
    private String contractName;
    private byte[] storageHash;
    private byte[] stateRoot;

    public UserAccount() {
        this.balance = BigInteger.ZERO;
        this.nonce = BigInteger.ZERO;
    }

    public BigInteger getBalance() {
        return balance;
    }

    public void addBalance(BigInteger val) {
        this.balance = this.balance.add(val);
    }

    public BigInteger getNonce() {
        return nonce;
    }

    public byte[] getCodeHash() {
        return codeHash;
    }

    public byte[] getStateRoot() {
        return stateRoot;
    }

    public void setCodeHash(byte[] codeHash) {
        this.codeHash = codeHash;
    }

    public void setStateRoot(byte[] stateRoot) {
        this.stateRoot = stateRoot;
    }

    public void setStorageHash(byte[] storageHash) {
        this.storageHash = storageHash;
    }

    public byte[] getStorageHash() {
        return storageHash;
    }

    public boolean isContractAccount(){
        return codeHash != null && codeHash.length>0;
    }

    public String getContractName() {
        return contractName;
    }

    public void setContractName(String contractName) {
        this.contractName = contractName;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserAccount{");
        sb.append(", balance=").append(balance);
        sb.append(", codeHash=").append(codeHash);
        sb.append(", contractName='").append(contractName).append('\'');
        sb.append(", nonce=").append(nonce);
        sb.append(", stateRoot=").append(stateRoot);
        sb.append(", storageHash=").append(stateRoot);
        sb.append('}');
        return sb.toString();
    }
}
