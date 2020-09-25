package com.buaa.blockchain.entity;


import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;

import com.buaa.blockchain.contract.core.DataUnit;
import com.buaa.blockchain.contract.core.OriginContract;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.checkerframework.checker.units.qual.C;

/**
 * 交易实体类
 * 交易作为Java类生成后，如果需要放到交易池中，需要转成json字符串，表现形式为<xxx,tran_hash,tran_json>
 *
 * @author hitty
 *
 * */

@Data
public class Transaction implements Serializable,Comparable<Transaction> {

    public static final String TYPE_CALL = "CALL";
    public static final String TYPE_TEST = "TEST";

    private static final long serialVersionUID = 4695627546411078836L;
    // 区块hash
    private String block_hash;
    //交易内容生成后算出的字段，在交易池中作为本交易的key
    private String tran_hash;
    // 交易种类
    private String type;
    // 时间戳
    private Timestamp timestamp;
    // 序列号
    private Integer sequence;
    // 签名
    private String sign;
    // 版本号
    private String version;
    // 其他
    private String extra;
    // 交易内容
    private String data;
    // 大量数据
    private byte[] largeData;

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
        largeData = new byte[]{};
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
        if(tran.getTranSeq() < this.getTranSeq()){
            return 1;
        }else if(tran.getTranSeq() > this.getTranSeq()){
            return -1;
        }else{
            return 0;
        }

    }

    public boolean equals(Transaction transaction) {
        return tran_hash.equals(transaction.tran_hash);
    }


    /*************************** 测试使用 ***************************/
    /**
     * 新增用户的交易模板
     * */
    public static Transaction createAddUser(String userName, int balance, String hash, ObjectMapper objectMapper){
        Transaction ts = new Transaction();
        ts.setBlock_hash("");
        ts.setTran_hash(hash+System.currentTimeMillis());
        ts.setType("CALL");
        ts.setTimestamp( new Timestamp(System.currentTimeMillis()));
        ts.setSequence(0);
        ts.setSign("test_sign");
        ts.setVersion("1.0");
        ts.setExtra("");
        String contractName = OriginContract.ADDUSER;
        HashMap<String, DataUnit> map = new HashMap<String, DataUnit>();
        /**
         * // 获取参数
         *         String key = args.get("KEY").getString();
         *         String name = args.get("NAME").getString();
         *         String password = args.get("PASSWORD").getString();
         *         String intro = args.get("INTRO").getString();
         *         String data = args.get("DATA").getString();
         *         int balance = args.get("BALANCE").getInteger();
         * */
        map.put("KEY",new DataUnit(userName));
        map.put("NAME",new DataUnit(userName));
        map.put("PASSWORD",new DataUnit(userName+".password"));
        map.put("INTRO",new DataUnit("test for adduser with "+userName));
        map.put("DATA",new DataUnit(""));
        map.put("BALANCE",new DataUnit(100));
        ContractCaller c = new ContractCaller(contractName,map);
        String jsonstr = null;
        try {
            jsonstr = objectMapper.writeValueAsString(c);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        ts.setData(jsonstr);
        return ts;
    }

    /**
     * 双用户转账的交易模板
     * */
    public static Transaction createTransfer(String u1,String u2, int tran, String hash, ObjectMapper objectMapper){
        Transaction ts = new Transaction();
        ts.setBlock_hash("");
        ts.setTran_hash(hash+System.currentTimeMillis());
        ts.setType("CALL");
        ts.setTimestamp( new Timestamp(System.currentTimeMillis()));
        ts.setSequence(0);
        ts.setSign("test_sign");
        ts.setVersion("1.0");
        ts.setExtra("");
        String contractName = OriginContract.TRANSFER;
        HashMap<String, DataUnit> map = new HashMap<String, DataUnit>();
        /**
         * String ua1 = args.get("USERACCOUNT1").getString();
         *         String ua2 = args.get("USERACCOUNT2").getString();
         *         int t = args.get("AMOUNT").getInteger();
         * */
        map.put("USERACCOUNT1",new DataUnit(u1));
        map.put("USERACCOUNT2",new DataUnit(u2));
        map.put("AMOUNT",new DataUnit(tran));
        ContractCaller c = new ContractCaller(contractName,map);
        String jsonstr = null;
        try {
            jsonstr = objectMapper.writeValueAsString(c);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        ts.setData(jsonstr);
        return ts;
    }
}
