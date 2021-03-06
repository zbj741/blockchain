package com.buaa.blockchain;

import com.buaa.blockchain.core.BlockchainService;
import com.buaa.blockchain.utils.SpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;


@SpringBootApplication
@ComponentScan(basePackages = "com.buaa.blockchain.*")
public class BlockchainApplication implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(BlockchainApplication.class);
    private final BlockchainService blockchainService;
    @Autowired
    private Environment environment;
    @Autowired
    private ApplicationContext appContext;
    @Autowired
    public BlockchainApplication(BlockchainService bs) {
        blockchainService = bs;
    }

    @Override
    public void run(String... args){
        new SpringContextUtil().setApplicationContext(appContext);
        try {
            blockchainService.firstTimeSetup();
        } catch (Exception e) {
            logger.error("Failed to start blockchain.", e);
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(BlockchainApplication.class, args);
    }
}
