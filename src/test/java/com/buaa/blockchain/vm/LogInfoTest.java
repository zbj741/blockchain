package com.buaa.blockchain.vm;

import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/** 
* LogInfo Tester. 
* 
* @author nolan.zhang 
* @since <pre>Dec 16, 2020</pre> 
* @version 1.0 
*/ 
public class LogInfoTest { 

    @Before
    public void before() throws Exception { 
    } 

    @After
    public void after() throws Exception { 
    } 

        /** 
     * 
     * Method: getAddress() 
     * 
     */ 
    @Test
    public void testGetAddress() throws Exception {
        List datawords = Lists.newArrayList();
        datawords.add(DataWord.of("0x333333"));

        byte[] addr = "ox3032923932".getBytes();
        byte[] data = "void".getBytes();
        LogInfo logInfo = new LogInfo(addr, datawords, data);
        System.out.println(logInfo.toString());
    } 

}
