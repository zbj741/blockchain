package com.buaa.blockchain.vm.client;

import com.buaa.blockchain.vm.DataWord;

import java.math.BigInteger;

public interface Repository {

    /**
     * 检查帐户是否存在
     *
     * @param address
     * @return
     */
    boolean exists(byte[] address);

    /**
     * 创建帐户
     *
     * @param address
     */
    void createAccount(byte[] address);

    /**
     * 删除帐户
     *
      * @param address
     */
    void delete(byte[] address);

    /**
     * 帐户nonce值加一处理
     *
     * @param address
     * @return
     */
    long increaseNonce(byte[] address);

    /**
     * 设置帐户nonce值
     *
      * @param address
     * @param nonce
     * @return
     */
    long setNonce(byte[] address, long nonce);

    /**
     * 查询当前帐户nonce值
     *
     * @param address
     * @return
     */
    long getNonce(byte[] address);

    /**
     * 存储智能合约代码code
     *
     * @param address
     * @param code
     */
    void saveCode(byte[] address, byte[] code);

    /**
     * 查询智能合约code
     *
     * @param address
     * @return
     */
    byte[] getCode(byte[] address);

    /**
     * 存储智能合约中Storage成员变量
     *
     * @param address
     * @param key
     * @param value
     */
    void putStorageRow(byte[] address, DataWord key, DataWord value);

    /**
     * 查询智能合约中状态变量值
     *
      * @param address
     * @param key
     * @return
     */
    DataWord getStorageRow(byte[] address, DataWord key);

    /**
     * 查询帐户余额
     *
     * @param address
     * @return
     */
    BigInteger getBalance(byte[] address);

    /**
     * 增加帐户金额
     *
     * @param address
     * @param value
     * @return
     */
    BigInteger addBalance(byte[] address, BigInteger value);

    /**
     * 创建snapshot以备rollback
     *
     * @return
     */
    Repository startTracking();

    /**
     * 备份所有变更操作数据
     *
     * @return
     */
    Repository clone();

    /**
     * 提交所有变更操作至数据库
     */
    void commit();

    /**
     * 回滚数据操作至原始状态
     */
    void rollback();

    byte[] getBlockHashByNumber(long index);
}
