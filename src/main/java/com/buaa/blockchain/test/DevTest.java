package com.buaa.blockchain.test;

import com.buaa.blockchain.contract.core.DataUnit;
import com.buaa.blockchain.entity.ContractCaller;
import com.buaa.blockchain.entity.Transaction;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import redis.clients.jedis.Jedis;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Random;

public class DevTest {
    static String contractDir = System.getProperty("user.dir")+ File.separator + "contract" + File.separator;
    static String contractName = "com.buaa.blockchain.contract.develop.Add";
    static ObjectMapper objectMapper = new ObjectMapper();
    static{
        // 转化为格式化的json
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        // 如果json中有新增的字段并且在实体类中不存在，不报错
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    }
    public static void main(String[] args) {
        // 39.105.129.47
        String localhost = "127.0.0.1";
        Random random = new Random(1);
        ObjectMapper objectMapper = new ObjectMapper();
        Jedis jedis27600 = new Jedis("192.168.2.101",27600);
        Jedis jedis27700 = new Jedis("192.168.2.101",27700);
        Jedis jedis27800 = new Jedis("192.168.2.102",27800);
        Jedis jedis27900 = new Jedis("192.168.2.102",27900);

        //Jedis jedis1 = new Jedis("39.105.129.47",27600);
        //Jedis jedis = new Jedis("39.105.129.47",27601);

        Transaction dev = createDevTx();
        String tsStr = "";
        try {
            tsStr = objectMapper.writeValueAsString(dev);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        jedis27600.hset("TRANSACTION", dev.getTran_hash(), tsStr);
        //jedis27700.hset("TRANSACTION", ts.getTran_hash(), tsStr);
        //jedis27800.hset("TRANSACTION", ts.getTran_hash(), tsStr);
        //jedis27900.hset("TRANSACTION", ts.getTran_hash(), tsStr);

        Transaction add = createAddTx();
        String tsStr1 = "";
        try {
            tsStr1 = objectMapper.writeValueAsString(add);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        jedis27600.hset("TRANSACTION", add.getTran_hash(), tsStr1);

        System.out.println(add);
    }

    public static Transaction createDevTx() {
        Transaction ts = new Transaction();
        ts.setSequence(100);
        ts.setSign("0");
        ts.setTran_hash("DEV");
        Long now = System.currentTimeMillis();
        ts.setBlock_hash("");
        ts.setTran_hash(Long.toString(now));
        ts.setType("CALL");
        ts.setTimestamp(new Timestamp(now));
        ts.setSequence(1);
        ts.setSign("test_sign");
        ts.setVersion("1.0");
        ts.setExtra("");
        // ts.setData("test data");
        File file = new File(contractDir+"Add.class");
        byte[] origindata = null;
        try{
            FileInputStream stream = new FileInputStream(file);
            origindata = new byte[stream.available()];
            stream.read(origindata);

            String name = "dev";

            HashMap<String, DataUnit> map = new HashMap<String, DataUnit>();
            map.put("CONTRACT_NAME",new DataUnit("Add"));
            map.put("CONTRACT_BYTES",new DataUnit(origindata));

            byte[] a = map.get("CONTRACT_BYTES").getByteArray();
            ContractCaller contractCaller = new ContractCaller(name,map);
            String jsonstr = objectMapper.writeValueAsString(contractCaller);
            ts.setData(jsonstr);


        }catch (Exception e){
            e.printStackTrace();
        }

        return ts;
    }
    public static Transaction createAddTx() {
        Transaction ts = new Transaction();
        ts.setSequence(2);
        ts.setSign("0");
        ts.setTran_hash("ADD2");
        Long now = System.currentTimeMillis();
        ts.setBlock_hash("");
        ts.setTran_hash(Long.toString(now));
        ts.setType("CALL");
        ts.setTimestamp(new Timestamp(now));
        ts.setSequence(0);
        ts.setSign("test_sign");
        ts.setVersion("1.0");
        ts.setExtra("");

        String name = "Add";

        HashMap<String, DataUnit> map = new HashMap<String, DataUnit>();
        map.put("KEY",new DataUnit("testAdd"));
        map.put("VALUE_1",new DataUnit(233));
        map.put("VALUE_2",new DataUnit(666));

        ContractCaller contractCaller = new ContractCaller(name,map);
        String jsonstr = null;
        try {
            jsonstr = objectMapper.writeValueAsString(contractCaller);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        ts.setData(jsonstr);

        return ts;
    }
}
