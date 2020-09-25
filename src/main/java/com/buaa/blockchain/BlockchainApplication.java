package com.buaa.blockchain;

import com.buaa.blockchain.core.BlockchainService;
import com.buaa.blockchain.test.LoadClassTest;
import com.buaa.blockchain.test.LoadJarTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan(basePackages = "com.buaa.blockchain.*")
public class BlockchainApplication implements CommandLineRunner {
    private static final Logger Log = LoggerFactory.getLogger(BlockchainApplication.class);
    private final BlockchainService blockchainService;

    @Autowired
    public BlockchainApplication(BlockchainService bs) { blockchainService = bs;
    }


    @Override
    public void run(String... args){
        LoadJarTest.LoadJar(null);

        try {
            blockchainService.firstTimeSetup();
        } catch (Exception e) {
            Log.error("run(): fatal error!");
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(BlockchainApplication.class, args);
    }

}
