package com.buaa.blockchain.test;

import com.buaa.blockchain.entity.ContractCaller;
import com.buaa.blockchain.entity.Transaction;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.Random;

public class TransferTest {
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
        String remote1 = "39.105.129.47";

        String localhost = "127.0.0.1";
        Random random = new Random(1);

        Jedis jedis27600remote = new Jedis(remote1,27600);

        Jedis jedis27600 = new Jedis("192.168.2.101",27600);
        Jedis jedis27700 = new Jedis("192.168.2.101",27700);
        Jedis jedis27800 = new Jedis("192.168.2.102",27800);
        Jedis jedis27900 = new Jedis("192.168.2.102",27900);

        Jedis jedisYN1 = new Jedis("182.92.183.51",7601,10000);
        Jedis jedisYN2 = new Jedis("47.96.224.137",7601,10000);
        Jedis jedisYN3 = new Jedis("47.105.85.229",7601,10000);
        Jedis jedisYN4 = new Jedis("39.99.56.129",7601,10000);


        Transaction tadduser1 = Transaction.createAddUser("user1",200,"user1Hash",objectMapper);
        Transaction tadduser2 = Transaction.createAddUser("user2",200,"user2Hash",objectMapper);
        Transaction ttransfer = Transaction.createTransfer("user1","user2",200,"user12Hash",objectMapper);
        String t1 = null;
        String t2 = null;
        String t3 = null;
        try {
            t1 = objectMapper.writeValueAsString(tadduser1);
            t2 = objectMapper.writeValueAsString(tadduser2);
            t3 = objectMapper.writeValueAsString(ttransfer);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        try {
            Transaction dt1 = objectMapper.readValue(t1, Transaction.class);
            ContractCaller c = objectMapper.readValue(dt1.getData(), ContractCaller.class);
            System.out.println(c.getArg());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        jedis27600.hset("TRANSACTION",tadduser1.getTran_hash(),t1);
        jedis27600.hset("TRANSACTION",tadduser1.getTran_hash(),t2);
        jedis27600.hset("TRANSACTION",tadduser1.getTran_hash(),t3);

    }


}
