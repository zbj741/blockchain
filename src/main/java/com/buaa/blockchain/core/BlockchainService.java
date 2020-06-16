package com.buaa.blockchain.core;


import com.buaa.blockchain.entity.Block;
import com.buaa.blockchain.entity.Transaction;
import com.buaa.blockchain.message.MessageCallBack;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    /**
     * 新的一轮做块。
     * @param height
     * @param round
     * */
    void startNewRound(int height,int round);
    /**
     * 状态码
     * -1：做块投票未通过/集群变动，高度不变，轮数+1
     * 0：本地做块失败，高度不变，轮数不变
     * 1：做块投票通过，高度+1，轮数归零
     * */
    int BLOCKCHAIN_SERVICE_STATE_FAIL = -1;
    int BLOCKCHAIN_SERVICE_STATE_CONTINUE = 0;
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
    boolean verifyBlock(Block block, int height, int round);
    /**
     * 生成创世区块
     * */
    Block generateFirstBlock();
    /**
     * 出块
     * @param height
     * @param round
     * */
    Block createNewBlock(int height, int round);
    /**
     * 提前做块
     * @param height
     * @param round
     * @param block
     * */
    void createNewCacheBlock(int height, int round, Block block);
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
    void voteForBlock(String tag,int height,int round,String blockHash,String nodeName,Boolean voteValue);
    /**
     * 获取同意票数
     * */
    int getAgreeVoteCount(String tag,int height,int round,String blockHash);
    /**
     * 获取不同意票数
     * */
    int getAgainstVoteCount(String tag,int height,int round,String blockHash);
    /**
     * 删除投票记录
     * */
    void removeVote(String tag,int height,int round,String blockHash);


    /*************  节点通信相关   *************/
    /**
     * 广播
     * */
    void broadcasting(Object message);
    /**
     * 获取集群大小
     * */
    int getClusterNodeSize();
    /**
     * 设置回调函数
     * */
    void setMessageCallBack(MessageCallBack messageCallBack);

    /*************  自身属性相关  *************/
    /**
     * 获取节点名称
     * */
    String getName();
    /**
     * 获取版本号
     * */
    String getVersion();
    /**
     * 获取节点的数字签名
     * */
    String getSign();

}
