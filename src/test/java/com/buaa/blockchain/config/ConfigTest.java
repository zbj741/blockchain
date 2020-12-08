package com.buaa.blockchain.config;

import com.buaa.blockchain.config.model.AccountConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class ConfigTest { 

    @Before
    public void before() throws Exception { 
    } 

    @After
    public void after() throws Exception { 
    } 

    @Test
    public void testAccountConfig() throws Exception {
        AccountConfig act = Config.load("application.properties").getAccountConfig();
        Assert.assertEquals("accounts", act.getKeyStoreDir());
    }

}
