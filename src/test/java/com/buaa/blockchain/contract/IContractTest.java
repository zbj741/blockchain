package com.buaa.blockchain.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/** 
* IContract Tester. 
* 
* @author <Authors name> 
* @since <pre>01/06/2021</pre> 
* @version 1.0 
*/ 
public class IContractTest extends TestCase { 
    public IContractTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
    *
    * Method: LOG(String msg)
    *
    */
    public void testLOG() throws Exception {
        TestConctract testConctract = new TestConctract();

        List logList = new ArrayList();
        Field f1 = testConctract.getClass().getSuperclass().getDeclaredField("logs");
        f1.setAccessible(true);
        f1.set(testConctract, logList);

        testConctract.add(1, 2);

        List logs = (List) f1.get(testConctract);

        String str = new ObjectMapper().writeValueAsString(logs);
        System.out.println("field: " + str);
    }



    public static Test suite() {
        return new TestSuite(IContractTest.class);
    }

} 
