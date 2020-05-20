package com.buaa.blockchain;

import com.buaa.blockchain.message.JGroupsMessageImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest(classes = BlockchainApplication.class)
class MessageTest {

    @Autowired
    JGroupsMessageImpl messageService;

    @Test
    void contextLoads() {
    }

    @Test
    void testMessage(){
        messageService.showInfo();
    }

}
