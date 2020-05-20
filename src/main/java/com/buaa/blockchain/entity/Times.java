package com.buaa.blockchain.entity;


import java.io.Serializable;

import lombok.Data;

@Data
public class Times implements Serializable{
    /**
     * 区块流程中的计时模块
     */
    private static final long serialVersionUID = 1L;

    /*  */
    private String block_hash;
    private int tx_length;
    private long startCompute;	//leader has been chosen and leader start compute block
    private long broadcast;	//computing block is finished and start broadcast the block
    private long blockReceived;	//block is received and start verify
    private long sendVote;	//verification is finished and send vote
    private long voteReceived;	//received quorum vote and start apply transaction
    private long storeBlock;	//start store block
    private long removeTrans;	//start remove transactions that have been executed from redis
    private long storeTrans;	//start store transactions that have been executed
    private long endTime;	//all procedures are finished

}
