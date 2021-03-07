package com.buaa.blockchain.crypto.hash; 

import org.junit.Test; 
import org.junit.Before; 
import org.junit.After; 

/** 
* Keccak256 Tester. 
* 
* @author nolan.zhang 
* @since <pre>Mar 7, 2021</pre> 
* @version 1.0 
*/ 
public class Keccak256Test { 

    @Before
    public void before() throws Exception { 
    } 

    @After
    public void after() throws Exception { 
    } 

        /** 
     * 
     * Method: hash(final String inputData) 
     * 
     */ 
    @Test
    public void testHashInputData() throws Exception { 
        //TODO: Test goes here...
        Hash hash = new Keccak256();
        System.out.println(hash.hash("123"));
        System.out.println(hash.hash("123").length());
        System.out.println(hash.hash("123").substring(0, 32).length());
    }

        /** 
     * 
     * Method: hash(final byte[] inputBytes) 
     * 
     */ 
    @Test
    public void testHashInputBytes() throws Exception { 
        //TODO: Test goes here... 
    } 

        /** 
     * 
     * Method: hashBytes(byte[] inputBytes) 
     * 
     */ 
    @Test
    public void testHashBytes() throws Exception { 
        //TODO: Test goes here... 
    } 

    
        /** 
     * 
     * Method: calculateHash(final byte[] inputBytes) 
     * 
     */ 
    @Test
    public void testCalculateHash() throws Exception { 
        //TODO: Test goes here... 
                    /* 
                    try { 
                       Method method = Keccak256.getClass().getMethod("calculateHash", final.class); 
                       method.setAccessible(true); 
                       method.invoke(<Object>, <Parameters>); 
                    } catch(NoSuchMethodException e) { 
                    } catch(IllegalAccessException e) { 
                    } catch(InvocationTargetException e) { 
                    } 
                    */ 
            } 

    } 
