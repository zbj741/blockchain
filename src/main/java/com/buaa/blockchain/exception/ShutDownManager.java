package com.buaa.blockchain.exception;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ShutDownManager {
    public void shutDown(){
        System.exit(0);
    }
}
