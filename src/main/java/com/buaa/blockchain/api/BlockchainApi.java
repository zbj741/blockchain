package com.buaa.blockchain.api;

import com.buaa.blockchain.entity.Block;
import com.buaa.blockchain.entity.Transaction;

import java.util.List;

/**
 * 该接口定义了一些从外部对区块链进行操作的方法
 *
 * @author hitty
 * */
public interface BlockchainApi {
    /*************  运行时相关  *************/
    /**
     * 同步区块
     * */
    Boolean syncBlocks(List<Block> blockList,String address);
    /*************  持久化数据查询相关  *************/
    /**
     * 获取区块数据
     * */
    Block findBlockByHash(String hash);
    Block findBlockByHeight(int height);
    List<Block> listBlock();
    List<Block> listBlockByHeight(int min,int max);

    /**
     * 获取交易数据
     * */
    Transaction findTxByHash(String hash);
    Transaction findTxByBlockHeight(int height);
    Transaction findTxByBlockHash(String bhash);
    List<Transaction> listTx();
    List<Transaction> listTxByStartTime();
    /***/
}
