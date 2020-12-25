package com.buaa.blockchain.config;

import com.buaa.blockchain.consensus.BaseConsensus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChainConfig {
    /* 是否单节点运行 */
    @Value("${buaa.blockchain.single}")
    public Boolean singleMode;
    /* 节点名 */
    @Value("${buaa.blockchain.nodename}")
    public String nodeName;
    /* 节点公钥 */
    @Value("${buaa.blockchain.sign}")
    private String nodeSign;
    /* 主节点轮询交易池的间隔时间（毫秒） */
    @Value("${buaa.blockchain.round-sleeptime}")
    private int sleepTime;
    /* 数据摘要生成算法名 */
    @Value("${buaa.blockchain.hash-algorithm}")
    private String hashAlgorithm;
    /* 区块链版本 */
    @Value("${buaa.blockchain.version}")
    private String version;
    /* 共识协议名称 */
    @Value("${buaa.blockchain.consensus}")
    private String consensusType;
    /* 共识协议通过占比 */
    @Value("${buaa.blockchain.consensus.agree-gate}")
    private float agreeGate;
    /* 共识协议 */
    private BaseConsensus consensus;
    /* leveldb数据库路径 */
    @Value("${buaa.blockchain.leveldb.dir}")
    String statedbDir;
    /* leveldb数据库名称 */
    @Value("${buaa.blockchain.leveldb.dbname}")
    String statedbName;
    /* 消息服务名称 */
    @Value("${buaa.blockchain.network}")
    private String messageServiceType;
    /* 消息服务器ip */
    @Value("${buaa.blockchain.msg.ipv4}")
    public String msgIp;
    /* 消息服务器端口 */
    @Value("${buaa.blockchain.msg.port}")
    public int msgPort;
    /* 最小连接数 */
    @Value("${buaa.blockchain.msg.minconnect}")
    public int minConnect;
    /* 节点消息服务器地址 */
    @Value("${buaa.blockchain.msg.address}")
    public String msgAddressList;
    /* 是否提前做块并缓存 */
    @Value("${buaa.blockchain.cache-blocks}")
    public Boolean cacheEnable;
    /* 单个区块内期望交易量 */
    @Value("${buaa.blockchain.tx-max-amount}")
    public int txMaxAmount;
    /* 做块阈值，交易量超过则开始做块 */
    @Value("${buaa.blockchain.txgate}")
    private int txGate;
    /* 运行模式 */
    @Value("${buaa.blockchain.debug}")
    private Boolean debug;

    public void setCryptoType(Integer cryptoType) {
        this.cryptoType = cryptoType;
    }

    @Value(("${buaa.blockchain.crypto_type}"))
    private Integer cryptoType;

    public Integer getCryptoType() {
        return cryptoType;
    }

    public Boolean getSingleMode() {
        return singleMode;
    }

    public String getNodeName() {
        return nodeName;
    }

    public String getNodeSign() {
        return nodeSign;
    }

    public int getSleepTime() {
        return sleepTime;
    }

    public String getHashAlgorithm() {
        return hashAlgorithm;
    }

    public String getVersion() {
        return version;
    }

    public String getConsensusType() {
        return consensusType;
    }

    public float getAgreeGate() {
        return agreeGate;
    }

    public BaseConsensus getConsensus() {
        return consensus;
    }

    public void setStatedbDir(String statedbDir) {
        this.statedbDir = statedbDir;
    }

    public void setStatedbName(String statedbName) {
        this.statedbName = statedbName;
    }

    public String getStatedbDir() {
        return statedbDir;
    }

    public String getStatedbName() {
        return statedbName;
    }

    public String getMessageServiceType() {
        return messageServiceType;
    }

    public String getMsgIp() {
        return msgIp;
    }

    public int getMsgPort() {
        return msgPort;
    }

    public int getMinConnect() {
        return minConnect;
    }

    public String getMsgAddressList() {
        return msgAddressList;
    }

    public Boolean getCacheEnable() {
        return cacheEnable;
    }

    public int getTxMaxAmount() {
        return txMaxAmount;
    }

    public int getTxGate() {
        return txGate;
    }

    public Boolean getDebug() {
        return debug;
    }
}
