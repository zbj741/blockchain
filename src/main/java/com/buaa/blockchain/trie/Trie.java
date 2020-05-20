package com.buaa.blockchain.trie;

/**
 * 字符树的接口，用于规范MPT树的实现类
 * 这里的value为byte数组形式，默认使用RLP编码
 *
 * @author hitty
 *
 * */
public interface Trie {

    /**
     * 按键取值
     *
     * @param key
     * @return 被存储对象的RLP编码
     */
    byte[] get(byte[] key);

    /**
     * 插入新的K,V数据
     *
     * @param key
     * @param value 被RLP编码后的对象
     */
    void update(byte[] key, byte[] value);

    /**
     * 删除一个K,V数据
     *
     * @param key
     */
    void delete(byte[] key);

    /**
     * 返回一个被SHA-3编码的trie树根节点的hash值（长度为32-byte）
     *
     * @return
     */
    byte[] getRootHash();

    /**
     * 设置根节点的SHA-3的hash值
     *
     * @param root -
     */
    void setRoot(byte[] root);

    /**
     * 提交所有的更改
     */
    void sync();

    /**
     * 撤销所有的更改
     */
    void undo();

}