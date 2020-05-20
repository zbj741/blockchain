package com.buaa.blockchain.core;


import com.buaa.blockchain.entity.Block;
import com.fasterxml.jackson.databind.ObjectMapper;

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
     * 新的一轮做块
     * */
    void startNewRound(int height,int round);
    /**
     * 运行入口
     * */
    void firstTimeSetup();
    /**
     * 区块持久化，包括交易的执行、区块和交易的持久化
     * */
    void storeBlock(Block block);
    /**
     * 区块确认
     * */
    boolean verifyBlock(Block block, int height, int round);
    /**
     * 生成创世区块
     * */
    Block generateFirstBlock();
    /**
     * 默认数据摘要生成
     * */
    String getDigest(String data);

}
