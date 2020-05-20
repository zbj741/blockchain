package com.buaa.blockchain;

import com.buaa.blockchain.core.BlockchainService;
import com.buaa.blockchain.core.BlockchainServiceImpl;
import com.buaa.blockchain.message.MessageCallBack;
import com.buaa.blockchain.message.MessageService;
import com.buaa.blockchain.test.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.util.Set;

@SpringBootApplication
@ComponentScan(basePackages = "com.buaa.blockchain.*")
public class BlockchainApplication implements CommandLineRunner {
    private static final Logger Log = LoggerFactory.getLogger(BlockchainApplication.class);

    @Autowired
    private BlockchainService blockchainService;

    @Override
    public void run(String... args){
        try {
            blockchainService.firstTimeSetup();
        } catch (Exception e) {
            Log.error("run(): fatal error!");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(BlockchainApplication.class, args);
    }

}
