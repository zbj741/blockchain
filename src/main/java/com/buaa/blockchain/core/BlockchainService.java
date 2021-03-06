package com.buaa.blockchain.core;


import com.buaa.blockchain.config.ChainConfig;
import com.buaa.blockchain.entity.Block;
import com.buaa.blockchain.message.Message;
import com.buaa.blockchain.message.MessageCallBack;

import java.util.List;

/**
 * blockchain实现类需要完成的若干功能
 * 状态转移图如下：
 * firstTimeSetup --> (startNewRound ~~> verifyBlock ~~> storeBlock --> startNewRound ...)
 * startNewRound之后，若当前本地为主节点则做块，否则等待；等到verifyBlock投票通过则执行storeBlock，完成后startNewRound
 * verifyBlock是被动执行的，其执行取决于是否收到其他的节点投票，与其他过程无关
 * storeBlock是被动执行的，但是storeBlock状态完成后才可以转入startNewRound
 *
 * @author hitty
 * */
public interface BlockchainService {

    ChainConfig getChainConfig();

    /*************  区块生成相关  *************/

    /**
     * 新的一轮做块。
     * @param height
     * @param round
     * */
    void startNewRound(long height,long round);

    /**
     * 状态码
     * 0：做块投票未通过，轮数+1
     * 1：做块投票通过/集群变动，轮数归零
     * */
    int BLOCKCHAIN_SERVICE_STATE_FAIL = 0;
    int BLOCKCHAIN_SERVICE_STATE_SUCCESS = 1;
    void startNewRound(int state);

    /**
     * 运行入口
     * */
    void firstTimeSetup();

    /**
     * 区块持久化，包括交易的执行、区块和交易的持久化
     * @param block
     * */
    void storeBlock(Block block);

    /**
     * 区块确认
     * @param block
     * @param height
     * @param round
     * @return
     * */
    boolean verifyBlock(Block block, long height, long round);

    /**
     * 生成创世区块
     * */
    Block generateFirstBlock();

    /**
     * 出块
     * @param height
     * @param round
     * */
    Block createNewBlock(long height, long round);

    /**
     * 提前做块
     * @param height
     * @param round
     * @param block
     * */
    void createNewCacheBlock(long height, long round, Block block);

    /**
     * 删除提前做块
     * */
    void flushCacheBlock();

    String CORE_MESSAGE_TOPIC_SYNC = "CORE_MESSAGE_TOPIC_SYNC";
    String CORE_MESSAGE_TOPIC_SYNCREPLY = "CORE_MESSAGE_TOPIC_SYNCREPLY";
    String CORE_MESSAGE_TOPIC_SYNC_END = "CORE_MESSAGE_TOPIC_SYNC_END";

    /**
     * 同步区块阶段开启
     * */
    void startSyncBlocks(String IpPort);
    /**
     * 向其他节点广播请求同步区块
     * */
    void requestSyncBlocks(String IpPort);
    /**
     * 回复syncBlocks
     * */
    void replySyncBlocks(long requireHeight,String address);
    /**
     * 本地同步区块
     * */
    void syncBlocks(List<Block> blockList);
    /**
     * 节点同步结束后，广播给所有节点同时开启新的一轮
     * */
    void syncBlocksEnd();
    /**
     * 模拟交易执行并返回rootHash
     * @param stateRoot
     * @param block
     * @return
     * */
    String transactionExec(String stateRoot,Block block);

    /**
     * 撤回交易执行，将worldState还原为上一次的形式
     * 在worldState已经sync之后则无效
     * */
    void undoTransactionExec();

    /**
     * 默认数据摘要生成
     * @param data
     * @return
     * */
    String getDigest(String data);


    /*************  对某一轮的做块投票相关   *************/
    /**
     * 为某一轮做块的区块投票
     * */
    Boolean voteForBlock(String tag,long height,long round,String blockHash,String nodeName,Boolean voteValue);

    /**
     * 获取同意票数
     * */
    int getAgreeVoteCount(String tag,long height,long round,String blockHash);

    /**
     * 获取不同意票数
     * */
    int getAgainstVoteCount(String tag,long height,long round,String blockHash);

    /**
     * 删除投票记录
     * */
    void removeVote(String tag,long height,long round,String blockHash);

    /*************  节点通信相关   *************/
    /**
     * 广播
     */
    void broadcasting(Message message);

    /**
     * 单播
     */
    void singleSend(Message message, String address);

    /**
     * 获取集群大小
     */
    int getClusterNodeSize();

    /**
     * 设置回调函数
     */
    void setMessageCallBack(MessageCallBack messageCallBack);


    /*************  自身属性相关  *************/
    /**
     * 获取节点名称
     */
    String getName();

    /**
     * 获取版本号
     */
    String getVersion();

    /**
     * 获取节点的数字签名
     */
    String getSign();
}
