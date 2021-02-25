package com.buaa.blockchain.entity;


import com.buaa.blockchain.contract.core.DataUnit;
import com.buaa.blockchain.contract.core.OriginContract;
import com.buaa.blockchain.vm.DataWord;
import com.buaa.blockchain.vm.utils.HexUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.io.Serializable;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;

/**
 * 交易实体类
 * 交易作为Java类生成后，如果需要放到交易池中，需要转成json字符串，表现形式为<xxx,tran_hash,tran_json>
 *
 * @author hitty
 */
@Data
public class Transaction implements Serializable,Comparable<Transaction> {

    public static final String TYPE_CALL = "CALL";
    public static final String TYPE_TEST = "TEST";

    private static final long serialVersionUID = 4695627546411078836L;

    private String block_hash;      // 区块hash
    private String tran_hash;       // 交易内容生成后算出的字段，在交易池中作为本交易的key
    private String type;            // 交易种类
    private Timestamp timestamp;    // 时间戳
    private Integer sequence;       // 序列号
    private String sign;            // 签名
    private String version;         // 版本号
    private String extra;           // 其他
    private byte[] data;            // 交易内容
    private byte[] largeData;       // 大量数据
    private Integer tranSeq;

    private int depth;
    private int index;
    private byte[] from;            // 交易发起人
    private byte[] to;              // 交易接收人
    private BigInteger value;       // 转帐金额
    private long nonce;             // 帐户交易次数
    private long gas;               // 执行合约调用所消耗的Gas值
    private BigInteger gasPrice;    // Gas单价信息
    private boolean rejected = false;
    private boolean create = false;

    public Transaction(){}

    public Transaction(byte[] from, byte[] to, BigInteger value, byte[] data) {
        this.from = from;
        this.to = to;
        this.value = value;
        this.data = data;
    }

    public Transaction(String _block_hash, String _tran_hash, String _type, Timestamp _timestamp,
                       Integer _sequence, String _sign, String _version, String _extra, byte[] _data) {
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


    public Transaction(int depth, int index, String type, byte[] from, byte[] to, long nonce, BigInteger value, byte[] data, long gas, BigInteger gasPrice){
        this.depth = depth;
        this.index = index;
        this.type = type;

        this.from = from;
        this.to = to;
        this.nonce = nonce;
        this.value = value;
        this.data = data;
        this.gas = gas;
        this.gasPrice = gasPrice;
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
        ts.setData(jsonstr.getBytes());
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
        ts.setData(jsonstr.getBytes());
        return ts;
    }

    public boolean isCreate() {
        return create;
    }
    public void reject() {
        this.rejected = true;
    }

    public static Transaction createDefaultTransaction(){
        return new Transaction(
                HexUtil.toHexString(DataWord.of(System.currentTimeMillis()).getData()),
                HexUtil.toHexString(DataWord.of(System.currentTimeMillis()).getData()),
                "type",
                new Timestamp(System.currentTimeMillis()),
                new Integer(0),
                "sign",
                "version",
                "extra",
                "data".getBytes()
        );
    }

    public Transaction(byte[] to, BigInteger value, byte[] data) {
        this.to = to;
        this.value = value;
        this.data = data;
        this.sequence = 0;
        this.version = "v1.0";
        this.extra = "";
//        if (data != null) {
//            this.data = Numeric.cleanHexPrefix(data);
//        }
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Transaction{");
        sb.append("block_hash='").append(block_hash).append('\'');
        sb.append(", create=").append(create);
        sb.append(", data=").append(Arrays.toString(data));
        sb.append(", depth=").append(depth);
        sb.append(", extra='").append(extra).append('\'');
        sb.append(", from=").append(Arrays.toString(from));
        sb.append(", gas=").append(gas);
        sb.append(", gasPrice=").append(gasPrice);
        sb.append(", index=").append(index);
        sb.append(", largeData=").append(Arrays.toString(largeData));
        sb.append(", nonce=").append(nonce);
        sb.append(", rejected=").append(rejected);
        sb.append(", sequence=").append(sequence);
        sb.append(", sign='").append(sign).append('\'');
        sb.append(", timestamp=").append(timestamp);
        sb.append(", to=").append(Arrays.toString(to));
        sb.append(", tran_hash='").append(tran_hash).append('\'');
        sb.append(", tranSeq=").append(tranSeq);
        sb.append(", type='").append(type).append('\'');
        sb.append(", value=").append(value);
        sb.append(", version='").append(version).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
