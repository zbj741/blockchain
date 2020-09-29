package com.buaa.blockchain.test;

import com.buaa.blockchain.entity.Transaction;
import redis.clients.jedis.Jedis;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Timestamp;
import java.util.Random;

public class BaseTest {
    public static void main(String[] args) {
        // 39.105.129.47
        String remote1 = "39.105.129.47";

        String localhost = "127.0.0.1";
        Random random = new Random(1);
        ObjectMapper objectMapper = new ObjectMapper();
        Jedis jedis27600remote = new Jedis(remote1,27600);

        Jedis jedis27600 = new Jedis("192.168.2.101",27600);
        Jedis jedis27700 = new Jedis("192.168.2.101",27700);
        Jedis jedis27800 = new Jedis("192.168.2.102",27800);
        Jedis jedis27900 = new Jedis("192.168.2.102",27900);

        Jedis jedisYN1 = new Jedis("182.92.183.51",7601,10000);
        Jedis jedisYN2 = new Jedis("47.96.224.137",7601,10000);
        Jedis jedisYN3 = new Jedis("47.105.85.229",7601,10000);
        Jedis jedisYN4 = new Jedis("39.99.56.129",7601,10000);


        //Jedis jedis = new Jedis("39.105.129.47",27601);
        int count = 0;
        while(count < 200){
            Transaction ts = createRandom();
            ts.setSequence(count);
            ts.setSign("0");
            ts.setTran_hash("hashYNVersion2"+count);
            String tsStr = "";
            try {
                tsStr = objectMapper.writeValueAsString(ts);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            jedisYN1.hset("TRANSACTION", ts.getTran_hash(), tsStr);
            jedisYN2.hset("TRANSACTION", ts.getTran_hash(), tsStr);
            jedisYN3.hset("TRANSACTION", ts.getTran_hash(), tsStr);
            jedisYN4.hset("TRANSACTION", ts.getTran_hash(), tsStr);
            //jedis27600remote.hset("TRANSACTION", ts.getTran_hash(), tsStr);
            //jedis27600.hset("TRANSACTION", ts.getTran_hash(), tsStr);
            //jedis27700.hset("TRANSACTION", ts.getTran_hash(), tsStr);
            //jedis27800.hset("TRANSACTION", ts.getTran_hash(), tsStr);
            //jedis27900.hset("TRANSACTION", ts.getTran_hash(), tsStr);

            System.out.println(ts);
            count++;
        }

    }
    public static Transaction createRandom(){
        Transaction ts = new Transaction();
        Long now = System.currentTimeMillis();
        ts.setBlock_hash("");
        ts.setTran_hash(Long.toString(now));
        ts.setType(Transaction.TYPE_TEST);
        ts.setTimestamp(new Timestamp(now));
        ts.setSequence(0);
        ts.setSign("test_sign");
        ts.setVersion("1.0");
        ts.setExtra("");
        ts.setData("test data");
        ts.setLargeData(new byte[0]);
        return ts;
    }
}
