package com.buaa.blockchain.core;

import com.buaa.blockchain.datasource.KeyValueDataSource;
import com.buaa.blockchain.trie.Trie;
import com.buaa.blockchain.trie.TrieImpl;
import com.buaa.blockchain.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 区块链中的全局状态树，用于记录交易的执行情况。
 * 对于使用者，存入WorldState的<K,V>形式为<String,String>，默认V是Json字符串
 * 每当执行一次block中的全部交易后，将根节点存入。
 * 所使用的底层为trie模块。levelDb是初始化trie模块需要的参数，但是不直接对levelDb进行任何操作。
 *
 * 为了使得key的冲突程度降低，可以将业务逻辑拆分，对应不同的key类型
 *
 * @author hitty
 * */

@Component
public class WorldState {

    private Trie trie;

    private final
    KeyValueDataSource levelDb;

    @Autowired
    public WorldState(KeyValueDataSource levelDb){
        trie = new TrieImpl(levelDb);
        this.levelDb = levelDb;
        levelDb.init();
    }

    /**
     * 更新状态
     * @param trie  trie对象
     * @param key   键值
     * @param value 
     * */
    public void update32(Trie trie, String key, String value) {
        //TODO 检查输入参数的合法性
        trie.update(key.getBytes(),value.getBytes());
    }

    /**
     * 默认以root为根
     * */
    public void update32(String key, String value) {
        //TODO 检查输入参数的合法性
        trie.update(key.getBytes(),value.getBytes());
    }

    /**
     * 获取状态数中的数据
     * @param trie
     * @param key
     * */
    public String get32(Trie trie, String key){
        byte[] rlp = trie.get(key.getBytes());
        return new String(rlp);
    }

    /**
     * 默认以root为根
     * */
    public String get32(String key){
        byte[] rlp = trie.get(key.getBytes());
        return new String(rlp);
    }

    /**
     * 同步levelDb
     * 当前数据存在java提供的cache中，只有存在脏数据或数据量到达一定大小，同步才会生效
     * */
    public void sync(){
        trie.sync();
    }

    /**
     * 获取状态树的树根值的16进制字符串形式
     * */
    public String getRootHash(){
        return Utils.bytesToHexString(trie.getRootHash());
    }
}
