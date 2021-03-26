package com.buaa.blockchain.api;

import com.buaa.blockchain.entity.Block;
import com.buaa.blockchain.entity.Transaction;
import com.buaa.blockchain.entity.TransactionReceipt;

import java.util.List;
import java.util.Map;

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

    Block findBlockByPreHash(String prehash);

    List<Block> findBlocks(int start,int end);

    Long  findMaxHeight();

    List<Block> getBlocklist();

    String getNowHash();

    int getBlockNumByTxRange(int low, int top);

    int getBlockNumBySign(String sign);

    List<Map<String, Object>> countBlockNumGroupBySign();

    List<Block> findPageBlocks(int page_index, int page_size);

    /**
     *
      * @param height
     * @return
     */
    Block findBlockByHeight(long height);

    /**
     *
     * @return
     */
    Block findLastBlock();

    /**
     * 获取交易数据
     * */
    Transaction findTxByTxHash(String tx_hash);

    List<Transaction> findTxByBlockHash(String blockHash);

    /**
     * 查询交易Receipts
     *
     * @param height
     * @return
     */
    List<TransactionReceipt> findReceiptsByHeight(long height);
}
