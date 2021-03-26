package com.buaa.blockchain.core.db;

import com.buaa.blockchain.contract.trie.Trie;
import com.buaa.blockchain.contract.trie.TrieImpl;
import com.buaa.blockchain.contract.trie.Values;
import com.buaa.blockchain.contract.trie.datasource.KeyValueDataSource;
import com.buaa.blockchain.contract.trie.datasource.LevelDbDataSource;
import com.buaa.blockchain.entity.*;
import com.buaa.blockchain.utils.JsonUtil;
import com.buaa.blockchain.utils.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;

/**
 * StateDB是区块链中的状态数据库
 * 这里的StateDB使用了以太坊的MPT模块
 *
 * 由于StateDB是一个KV数据库，所以需要一个对KeySet进行组织的方式
 * 当前的设计是，将同类型数据（比如区块数据的Key会放到一起）
 *
 * @author hitty
 * */
@Slf4j
public class StateDB implements DB {
    private Trie trie;
    private KeyValueDataSource levelDb;

    // 关键数据的KeySet
    public static final String BLOCK_KEYSET_KEY = "BLOCK_KEYSET_KEY";
    public static final String TRANSACTION_KEYSET_KEY = "TRANSACTION_KEYSET_KEY";
    public static final String USERACCOUNT_KEYSET_KEY = "USERACCOUNT_KEYSET_KEY";
    public static final String CONTRACTACCOUNT_KEYSET_KEY = "CONTRACTACCOUNT_KEYSET_KEY";

    private StateData stateData;
    /* 初始插入字段 */
    private static String init = "This is the description for worldState in buaa-blockchain, " +
            "and the aim for this action is to initial specific value in leveldb." +
            "This data needs to longer than 32 bytes in order to write into disk.";
    /**
     * 当前状态暂存
     * 保存所有的keySet和最高块
     * */
    class StateData{
        // 自身结构体在StateDB中的Key
        static final String STATE_DATA_KEY = "STATE_DATA_KEY";
        // 当前最高块
        Block currentBlock;
        HashSet<String> blockKeySet;
        HashSet<String> userAccountKeySet;
//        HashSet<String> contractAccountKeySet;
        public StateData(){
            this.currentBlock = new Block();
            this.blockKeySet = new HashSet<>();
            this.userAccountKeySet = new HashSet<>();
//            this.contractAccountKeySet = new HashSet<>();
        }
        public void writeStateDB(){
            try {
                update(STATE_DATA_KEY, JsonUtil.objectMapper.writeValueAsBytes(this));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        /**
         * 在StateDB中同步
         * */
        public void syncFromStateDB(){
            try {
                StateData stateData = JsonUtil.objectMapper.readValue(get(STATE_DATA_KEY), StateData.class);
                this.currentBlock = stateData.currentBlock;
                this.blockKeySet = stateData.blockKeySet;
                this.userAccountKeySet = stateData.userAccountKeySet;
//                this.contractAccountKeySet = stateData.contractAccountKeySet;
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        /**
         * 更新最新区块
         * */
        public void updateCurrentBlock(Block block){
            currentBlock = block;
            blockKeySet.add(block.getHash());
            try {
                update(STATE_DATA_KEY, JsonUtil.objectMapper.writeValueAsBytes(this));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        /**
         * 加入用户
         * */
        public void addUserAccount(UserAccount userAccount){
//            this.userAccountKeySet.add(userAccount.getUserKey());
//            try {
//                update(STATE_DATA_KEY, JsonUtil.objectMapper.writeValueAsBytes(this));
//            } catch (JsonProcessingException e) {
//                e.printStackTrace();
//            }
        }
//        /**
//         * 加入智能合约用户
//         * */
//        public void addContractAccount(ContractAccount contractAccount){
//            this.contractAccountKeySet.add(contractAccount.getcKey());
//            try {
//                update(STATE_DATA_KEY, JsonUtil.objectMapper.writeValueAsBytes(this));
//            } catch (JsonProcessingException e) {
//                e.printStackTrace();
//            }
//        }
    }


    /**
     * 初始化方法
     * TODO 改成单例模式
     * */
    public StateDB(String dir, String name, String root){
        levelDb = new LevelDbDataSource(dir, name);
        levelDb.init();
        if(null == root){
            // 链节点是第一次运行，重新建立trie
            trie = new TrieImpl(levelDb);
            update("init", init);
            initStateData();
            sync();
            log.info("WorldState(): init, getRootHash="+getRootHash());
        }else{
            // root不为空，尝试用root找到trie
            byte[] rootBytes = levelDb.get(Utils.hexStringToBytes(root));
            if(null == rootBytes){
                // 在leveldb中无法找到当前root对应的值，只能新建trie
                trie = new TrieImpl(levelDb);
                update("init", init);
                initStateData();
                sync();
                log.info("switchRoot(): root="+root+" no found in levelDb, still getRootHash="+getRootHash());
            }else{
                // 找到之前的旧数据，重新装载
                Values val = Values.fromRlpEncoded(rootBytes);
                trie = new TrieImpl(levelDb,val.asObj());
                try {
                    this.stateData = JsonUtil.objectMapper.readValue(get(StateData.STATE_DATA_KEY), StateData.class);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                log.info("switchRoot(): root="+root+" found in levelDb, change root, getRootHash="+getRootHash());
            }
        }
    }
    /**
     * 初始化keySet集合
     * */
    private void initStateData(){
        this.stateData = new StateData();
        this.stateData.writeStateDB();
    }

    /**
     * 更新状态
     * @param trie  trie对象
     * @param key   键值
     * @param value
     * */
    private void update(Trie trie, String key, String value) {
        log.debug("update32(): key="+key+", value="+value);
        //TODO 检查输入参数的合法性
        trie.update(key.getBytes(),value.getBytes());
    }

    /**
     * 默认以root为根
     * */
    private void update(String key, String value) {
        log.debug("update32(): key="+key+", value="+value);
        //TODO 检查输入参数的合法性
        trie.update(key.getBytes(),value.getBytes());

    }
    /**
     * 将byte数组放入
     * */
    private void update(String key, byte[] data){
        log.debug("update32(): key="+key+", byte[]");
        trie.update(key.getBytes(),data);
    }

    /**
     * 获取状态数中的数据
     * @param trie
     * @param key
     * */
    private String get(Trie trie, String key){
        byte[] rlp = new byte[0];
        rlp = trie.get(key.getBytes());
        String value = new String(rlp);
        log.info("get32(): key="+key+", value="+value);
        return value;
    }

    /**
     * 默认以root为根
     * */
    private String get(String key){
        byte[] rlp = new byte[0];
        rlp = trie.get(key.getBytes());
        String value = new String(rlp);
        log.debug("get32(): key="+key+", value="+value);
        return value;
    }

    private byte[] getAsBytes(String key){
        return trie.get(key.getBytes());
    }

    private void delete(String key) {
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


    /************************* DB interface ******************************/
    @Override
    public void insertUserAccount(UserAccount userAccount) {
//        try {
//            // 插入账户
//            update(userAccount.getUserKey(), JsonUtil.objectMapper.writeValueAsBytes(userAccount));
//            this.stateData.addUserAccount(userAccount);
//        } catch (JsonProcessingException e) {
//            // TODO insertUserAccount failed
//            e.printStackTrace();
//        }
    }

    @Override
    public UserAccount findUserAccountByUserName(String userName) {
        String userAccountStr = get(userName);
        UserAccount userAccount = null;
        try {
            userAccount = JsonUtil.objectMapper.readValue(userAccountStr, UserAccount.class);
        } catch (JsonProcessingException e) {
            // TODO findUserAccountByUserName failed
            e.printStackTrace();
        }
        return userAccount;
    }

    @Override
    public void addBalance(String userName, BigInteger val) {
        UserAccount userAccount = findUserAccountByUserName(userName);
        userAccount.addBalance(val);
//        // 写回
//        try {
//            update(userAccount.getUserKey(), JsonUtil.objectMapper.writeValueAsBytes(userAccount));
//            this.stateData.addUserAccount(userAccount);
//        } catch (JsonProcessingException e) {
//            // TODO updateUserAccountBalance failed
//            e.printStackTrace();
//        }
    }

//    @Override
//    public void insertContractAccount(ContractAccount contractAccount) {
//        try {
//            // 插入账户
//            update(contractAccount.getcKey(), JsonUtil.objectMapper.writeValueAsBytes(contractAccount));
//            this.stateData.addContractAccount(contractAccount);
//        } catch (JsonProcessingException e) {
//            // TODO insertContractAccount failed
//            e.printStackTrace();
//        }
//    }

    @Override
    public void insertBlock(Block block) {
        try {
            // 插入最新区块
            update(block.getHash(), JsonUtil.objectMapper.writeValueAsBytes(block));
            this.stateData.updateCurrentBlock(block);
        } catch (JsonProcessingException e) {
            // TODO insertblock failed
            e.printStackTrace();
        }
    }

    @Override
    public Block findBlockByHash(String hash) {
        String blockStr = get(hash);
        Block block = null;
        try {
            block = JsonUtil.objectMapper.readValue(blockStr, Block.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return block;
    }

    @Override
    public Block findBlockByHeight(long height) {
        // 此处使用常见的索引模式
        String nowHash = this.stateData.currentBlock.getHash();
        long max = this.stateData.currentBlock.getHeight();
        while(max > 0){
            try {
                Block block = JsonUtil.objectMapper.readValue(get(nowHash), Block.class);
                if(block.getHeight() == height){
                    return block;
                }else{
                    nowHash = block.getPre_hash();
                    max--;
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        // 未找到
        return null;
    }

    @Override
    public long findBlockNum(String hash) {
        if(this.stateData.blockKeySet.size() == 0){
            // 没有区块
            return 0;
        }else{
            return this.stateData.currentBlock.getHeight();
        }
    }

    @Override
    public String findHashByHeight(long height) {
        Block b = findBlockByHeight(height);
        if(null == b){
            return null;
        }else{
            return b.getHash();
        }
    }

    @Override
    public long findMaxHeight() {
        return this.stateData.blockKeySet.size();
    }

    @Override
    public String findStateRootByHeight(int height) {
        Block b = findBlockByHeight(height);
        if(null == b){
            return null;
        }else{
            return b.getState_root();
        }
    }

    @Override
    public void insertTimes(Times times) {

    }

    @Override
    public int insertTransaction(Transaction transaction) {
        int flag = 0;
        try {
            update(transaction.getTran_hash(), JsonUtil.objectMapper.writeValueAsBytes(transaction));
            flag = 1;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return flag;
    }

    @Override
    public List<Transaction> findTransByBlockHash(String hash) {
        Block block = findBlockByHash(hash);
        if(null == block){
            return null;
        }else{
            return block.getTrans();
        }
    }

    @Override
    public Transaction findTransByHash(String tranHash) {
        String nowHash = this.stateData.currentBlock.getHash();
        long max = this.stateData.currentBlock.getHeight();
        while(max > 0){
            Block b = findBlockByHash(nowHash);
            for(Transaction ts : b.getTrans()){
                if(ts.getTran_hash().equals(tranHash)){
                    return ts;
                }
            }
            nowHash = b.getPre_hash();
            max--;
        }
        // 未找到
        return null;
    }


}

