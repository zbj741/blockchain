package com.buaa.blockchain.consensus;

/**
 * SBFT简化加速拜占庭共识协议
 * 在本协议中，一次共识过程内集群需要进行三个阶段：
 *      1.主节点将交易数据从交易池中取出，计算merkle_root，打包并且广播（记为block-1）。
 *      2.所有节点将block-1中的交易在本地进行merkle_root的计算，并且和block-1中的merkle_root对比，
 *        若一致，则投赞同票，否则投反对票。将投票结果添加进block-1中，打包为block-2，广播。
 *      3.所有节点将会收到若干个投票结果，当赞成票超过总数的2/3时，认为该块已被承认，本地进行交易的执行和区块的存储。
 * SBFT简化加速拜占庭共识协议在传统PBFT环节下，省略了pre-pre的环节；因为merkle_root可以代表本次交易的摘要
 *
 * 区块链服务实现SBFT简化加速拜占庭共识协议，需要实现本接口，规范其行为
 *
 * @author hitty
 * */

public interface SBFTConsensus<T> extends BaseConsensus<T>{
    /**
     * 第一阶段广播
     * */
    void sbftDigestBroadcast(T stage1_send);
    /**
     * 收到第一阶段广播
     * */
    void sbftDigestBroadcastReceived(T stage1_received);
    /**
     * 第二阶段广播
     * */
    void sbftVoteBroadcast(T stage2_send);
    /**
     * 收到第二阶段广播
     * */
    void sbftVoteBroadcastReceived(T stage2_received);
    /**
     * 第三阶段执行
     * */
    void sbftExecute(T exec);

    /**
     * 各个阶段的TOPIC
     * */
    String SBFT_MESSAGE_TOPIC_TEST = "SBFT_MESSAGE_TOPIC_TEST";
    String SBFT_MESSAGE_TOPIC_DIGEST = "SBFT_MESSAGE_TOPIC_DIGEST";
    String SBFT_MESSAGE_TOPIC_VOTE = "SBFT_MESSAGE_TOPIC_VOTE";
    String SBFT_MESSAGE_TOPIC_SYNC = "SBFT_MESSAGE_TOPIC_SYNC";
    String SBFT_MESSAGE_TOPIC_SYNC_REPLY = "SBFT_MESSAGE_TOPIC_SYNC_REPLY";
    String SBFT_MESSAGE_TOPIC_EXECUTE = "SBFT_MESSAGE_TOPIC_EXECUTE";
    String SBFT_MESSAGE_TOPIC_DROP = "SBFT_MESSAGE_TOPIC_DROP";
}
