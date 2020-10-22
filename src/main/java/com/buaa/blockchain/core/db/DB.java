package com.buaa.blockchain.core.db;

import com.buaa.blockchain.entity.*;

import java.util.List;

/**
 * DB接口
 * */
public interface DB {
    /**
     * 新增一个用户账户
     * */
    void insertUserAccount(UserAccount userAccount);
    /**
     * 以用户名查找用户
     * */
    UserAccount findUserAccountByUserName(String userName);
    /**
     * 更新账户的余额
     * 使用账户的名字作为key
     * */
    void updateUserAccountBalance(String userName, int newBalance);
    /**
     * 新增一个智能合约账户
     * */
    void insertContractAccount(ContractAccount contractAccount);

    /**
     * 新增一个区块
     * */
    void insertBlock(Block block);
    /**
     * 以hash查询区块
     * */
    Block findBlockByHash(String hash);
    /**
     * 以高度查询区块
     * */
    Block findBlockByHeight(int height);
    /**
     * 查询当前区块的数量
     * */
    int findBlockNum(String hash);
    /**
     * 以高度查询区块的hash
     * */
    String findHashByHeight(int height);
    /**
     * 查询当前最高块的高度
     * */
    int findMaxHeight();
    /**
     * 以高度查询区块的stateRoot
     * */
    String findStateRootByHeight(int height);
    /**
     * 插入时间记录
     * */
    void insertTimes(Times times);
    /**
     * 插入一条交易
     * */
    int insertTransaction(Transaction transaction);
    /**
     * 以区块hash查找对应的交易集合
     * */
    List<Transaction> findTransByBlockHash(String hash);
    /**
     * 以交易hash查找交易
     * */
    Transaction findTransByHash(String tranHash);
}