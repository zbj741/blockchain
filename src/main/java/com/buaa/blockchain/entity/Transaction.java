package com.buaa.blockchain.entity;


import java.io.Serializable;
import java.sql.Timestamp;

import lombok.Data;

/**
 * 交易实体类
 * 交易作为Java类生成后，如果需要放到交易池中，需要转成json字符串，表现形式为<xxx,tran_hash,tran_json>
 *
 * @author hitty
 *
 * */

@Data
public class Transaction implements Serializable,Comparable<Transaction> {

    private static final long serialVersionUID = 4695627546411078836L;


    private String block_hash;

    /* 交易内容生成后算出的字段，在交易池中作为本交易的key */

    private String tran_hash;

    /* 交易种类 */

    private String type;

    /* 时间戳 */

    private Timestamp timestamp;

    /* 序列号 */

    private Integer sequence;


    private String sign;

    /* 版本号 */

    private String version;


    private String extra;

    /* 交易内容 */

    private String data;

    private Integer tranSeq;

    public Transaction(String _block_hash, String _tran_hash, String _type, Timestamp _timestamp,
                       Integer _sequence, String _sign, String _version, String _extra, String _data) {
        block_hash = _block_hash;
        tran_hash = _tran_hash;
        type = _type;
        timestamp = _timestamp;
        sequence = _sequence;
        sign = _sign;
        version = _version;
        extra = _extra;
        data = _data;
    }

    public Transaction() {

    }

    public static Transaction createDefaultTransaction(){
        return new Transaction(
                "block_hash",
                Long.toString(System.currentTimeMillis()),
                "type",
                new Timestamp(System.currentTimeMillis()),
                new Integer(0),
                "sign",
                "version",
                "extra",
                "data"
        );

    }

    @Override
    public int compareTo(Transaction tran) {
        return this.getTranSeq().compareTo(tran.getTranSeq());
    }

    public boolean equals(Transaction transaction) {
        return tran_hash.equals(transaction.tran_hash);
    }

}
