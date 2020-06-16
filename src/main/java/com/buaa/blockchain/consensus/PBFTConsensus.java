package com.buaa.blockchain.consensus;

/**
 * PBFT实用拜占庭共识协议
 * 此处的PBFT实现参考了Hyperledger Fabric中的PBFT实现
 * PBFT协议分为4个步骤：
 *     0.request: 接受到新交易的节点将交易广播 【由于是许可链，默认存在转发节点将交易全网广播，省略此处步骤】
 *     1.pre-prepare: 主节点从交易池取出一部分交易，打包成区块并且广播
 *                    收到pre-prepare的节点准备进入prepare阶段
 *     2.prepare: 收到pre-prepare广播后，将区块模拟执行并且将执行结果放入区块，缓存并且广播
 *     3.commit: 如果收到的prepare广播中得到的区块摘要和本地相等的票数超过阈值，则广播commit
 *               收到的commit超过了阈值则持久化
 *
 * */
public interface PBFTConsensus<T> extends BaseConsensus<T> {

    void requestBroadcast(T t);
    void requestReceived(T t);
    void prePrepareBroadcast(T t);
    void prePrepareReceived(T t);
    void prepareBroadcast(T t);
    void prepareReceived(T t);
    void commitBroadcast(T t);
    void commitReceived(T t);

    void viewChanged();

    /**
     * 各个阶段Message的Topic
     * */
    String PBFT_MESSAGE_TOPIC_REQUEST = "PBFT_MESSAGE_TOPIC_REQUEST";
    String PBFT_MESSAGE_TOPIC_PREPREPARE = "PBFT_MESSAGE_TOPIC_PREPREPARE";
    String PBFT_MESSAGE_TOPIC_PREPARE = "PBFT_MESSAGE_TOPIC_PREPARE";
    String PBFT_MESSAGE_TOPIC_COMMIT = "PBFT_MESSAGE_TOPIC_COMMIT";
    /**
     * 投票tag
     * */
    String PBFT_VOTETAG_PREPARE = "PBFT_VOTETAG_PREPARE";
    String PBFT_VOTETAG_COMMIT = "PBFT_VOTETAG_COMMIT";


}
