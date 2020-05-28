package com.buaa.blockchain.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Block数据结构
 *
 * @author hitty
 * */

@Data
public class Block implements Serializable, Comparable<Block> {

    private static final long serialVersionUID = 4695627546411078831L;
    /* 上一个区块的hash */
    private String pre_hash;
    /* 区块的状态树根 */
    private String state_root;
    /* 上一个区块的状态树根 */
    private String pre_state_root;
    /* 区块所包含交易数据的merkle树根 */
    private String merkle_root;
    /* 交易数量 */
    private int tx_length;
    /* 区块高度，作为区块持久化的主键，在数据库中是按照升序排列的 */
    private int height;
    /* 区块签名 */
    private String sign;
    /* 版本号 */
    private String version;
    /* 其他， 暂时用来记录从createBlock结束到storeBlock结束的时间差（毫秒）*/
    private String extra;
    /* 区块hash，作为区块持久化的主键，由区块头部数据哈希得到 */
    private String hash;
    /* 时间戳 */
    private String timestamp;
    /* 交易列表 */
    private ArrayList<Transaction> trans;
    /* 计算区块的耗时相关记录 */
    private Times times;

    public Block(){}

	@Override
	public String toString() {
		return "Block " +
                "[hash=" + hash +
                ", pre_hash=" + pre_hash +
                ", merkle_root=" + merkle_root +
                ", tx_length=" + tx_length +
                ", height=" + height +
                ", pre_state_root" + pre_state_root +
                ", timestamp=" + timestamp +
                "]";
	}

    @Override
    public int compareTo(Block b) {
        if(
                this.merkle_root.equals(b.merkle_root) &&
                        this.tx_length == b.tx_length &&
                        this.tx_length == b.tx_length &&
                        this.hash == b.hash
        ){
            Map<Integer, Transaction> map = new HashMap<Integer, Transaction>();
            for (Transaction tran : this.trans) {
                map.put(tran.hashCode(), tran);
            }
            for (Transaction tran : b.trans) {
                if (map.get(tran.hashCode()) == null || map.get(tran.hashCode()).compareTo(tran) != 0) {
                    return -1;
                }
            }
        }
        return 0;
    }

    public void setArgs(String preHash, String hash, String merkleRoot, String PreStateRoot, int height, String sign, String timestamp, String version, ArrayList<Transaction> transList, int txLength){
        this.setPre_hash(preHash);
        this.setHash(hash);
        this.setMerkle_root(merkleRoot);
        this.setPre_state_root(PreStateRoot);
        this.setHeight(height);
        this.setSign(sign);
        this.setTimestamp(timestamp);
        this.setVersion(version);
        this.setExtra("");
        this.setTrans(transList);
        this.setTx_length(txLength);
    }

}
