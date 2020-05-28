package com.buaa.blockchain.entity;


import java.io.Serializable;

import lombok.Data;

/**
 * Block中的times属性，用于记录区块在各个环节中的时间
 *
 * @author hitty
 *
 * */
@Data
public class Times implements Serializable{
    /**
     * 区块流程中的计时模块
     */
    // 区块hash
    private String block_hash;
    // 交易个数
    private int tx_length;
    // 开始做块计算
    private long startCompute;
    // 做块完成，开始广播
    private long broadcast;
    // 收到广播
    private long blockReceived;
    // 发起投票
    private long sendVote;
    // 投票结束
    private long voteReceived;
    // 执行区块
    private long storeBlock;
    // 从交易池中删除交易
    private long removeTrans;
    // 存储交易
    private long storeTrans;
    // 结束
    private long endTime;

    public Times(){}

}
