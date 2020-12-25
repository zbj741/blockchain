package com.buaa.blockchain.core.db;

import com.buaa.blockchain.entity.*;

import java.math.BigInteger;
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
     * 更新账户余额
     * 使用账户的名字作为key
     * */
    void addBalance(String userName, BigInteger value);

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
    Block findBlockByHeight(long height);
    /**
     * 查询当前区块的数量
     * */
    long findBlockNum(String hash);
    /**
     * 以高度查询区块的hash
     * */
    String findHashByHeight(long height);
    /**
     * 查询当前最高块的高度
     * */
    long findMaxHeight();
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
