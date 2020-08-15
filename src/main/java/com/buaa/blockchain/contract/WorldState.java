package com.buaa.blockchain.contract;

import com.buaa.blockchain.contract.State;
import com.buaa.blockchain.trie.datasource.KeyValueDataSource;
import com.buaa.blockchain.trie.Trie;
import com.buaa.blockchain.trie.TrieImpl;
import com.buaa.blockchain.trie.Values;
import com.buaa.blockchain.trie.datasource.LevelDbDataSource;
import com.buaa.blockchain.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 区块链中的全局状态树，用于记录交易的执行情况。
 * 对于使用者，存入WorldState的<K,V>形式为<String,String>，默认V是Json字符串
 * 每当执行一次block中的全部交易后，将根节点存入。
 * 所使用的底层为trie模块。levelDb是初始化trie模块需要的参数，但是不直接对levelDb进行任何操作。
 *
 * WorldState的唯一构造方法中，默认创建新的trie。
 * 当区块链系统第一次运行时，WorldState状态为空，由于trie中写入数据的RLP码小于32位则不同步到levelDB
 *
 * 为了使得key的冲突程度降低，可以将业务逻辑拆分，对应不同的key类型
 *
 * @author hitty
 * */
@Slf4j
public class WorldState implements State {
    /* 字符树 */
    private Trie trie;
    /* levelDb存储接口 */
    private KeyValueDataSource levelDb = null;

    private static String init = "This is the description for worldState in buaa-blockchain, " +
            "and the aim for this action is to initial specific value in leveldb." +
            "This data needs to longer than 32 bytes in order to write into disk.";

    public WorldState(String dir,String name) {
        levelDb = new LevelDbDataSource(dir,name);
        levelDb.init();
        trie = new TrieImpl(levelDb);
        // 向初始化的worldState中插入既定数据，硬编码统一
        this.update("init",init);
        this.sync();
        log.info("WorldState(): init, getRootHash="+getRootHash());
    }

    /**
     * 更新当前状态树的根
     * @param root 上一次状态的根节点Hash值的Hex字符串，一般来自mysql中的上一个block存储
     * */
    public boolean switchRoot(String root){
        byte[] rootBytes = null;
        // 如果root和trie的初始化root一样，证明节点是第一次运行
        if(root.equals(trie.getRootHash())){
            return true;
        }
        try {
            // 按照当前root寻找对应的trie
            rootBytes = levelDb.get(Utils.hexStringToBytes(root));
        } finally {
            if(null == rootBytes){
                log.info("switchRoot(): root="+root+" no found in levelDb, still getRootHash="+getRootHash());
                return false;
            }else{
                Values val = Values.fromRlpEncoded(rootBytes);
                trie = new TrieImpl(levelDb,val.asObj());
                log.info("switchRoot(): root="+root+" found in levelDb, change root, getRootHash="+getRootHash());
                return true;
            }
        }

    }

    /**
     * 更新状态
     * @param trie  trie对象
     * @param key   键值
     * @param value 
     * */
    public void update(Trie trie, String key, String value) {
        log.debug("update32(): key="+key+", value="+value);
        //TODO 检查输入参数的合法性
        trie.update(key.getBytes(),value.getBytes());
    }

    /**
     * 默认以root为根
     * */
    public void update(String key, String value) {
        log.debug("update32(): key="+key+", value="+value);
        //TODO 检查输入参数的合法性
        trie.update(key.getBytes(),value.getBytes());
    }

    /**
     * 获取状态数中的数据
     * @param trie
     * @param key
     * */
    public String get(Trie trie, String key){
        byte[] rlp = trie.get(key.getBytes());
        String value = new String(rlp);
        log.info("get32(): key="+key+", value="+value);
        return value;
    }

    /**
     * 默认以root为根
     * */
    public String get(String key){
        byte[] rlp = trie.get(key.getBytes());
        String value = new String(rlp);
        log.debug("get32(): key="+key+", value="+value);
        return value;
    }

    @Override
    public void delete(String key) {
        trie.delete(key.getBytes());
    }

    /**
     * 同步到levelDb
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

    /**
     * 撤销
     * */
    public void undo(){
        trie.undo();
    }
    /**
     * 关闭
     * */
    public void close(){
        this.levelDb.close();
    }
}
