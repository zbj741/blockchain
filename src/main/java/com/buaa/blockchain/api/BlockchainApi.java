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
    /*************  持久化数据查询相关  *************/
    /**
     * 获取区块数据
     * */
    Block findBlockByHash(String hash);
    Block findBlockByHeight(int height);
    // Bk意为不完整的block，省略了交易数据
    Block findBkByHash(String hash);
    Block findBkByHeight(int height);
    List<Block> listBlock();
    List<Block> listBlockByHeight(int min,int max);
    List<Block> listBk();
    List<Block> listBkByHeight(int min,int max);

    /**
     * 获取交易数据
     * */
    Transaction findTxByHash(String hash);
    Transaction findTxByBlockHeight(int height);
    Transaction findTxByBlockHash(String bhash);
    List<Transaction> listTx();
    List<Transaction> listTxByStartTime();
}
