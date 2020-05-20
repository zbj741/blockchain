package com.buaa.blockchain;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;


@SpringBootTest
class BlockchainApplicationTests {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    public void testRedis(){
        ValueOperations<String,String> stringOps = redisTemplate.opsForValue();
        stringOps.set("1","first");
        System.out.println(stringOps.get("1"));
    }

}
