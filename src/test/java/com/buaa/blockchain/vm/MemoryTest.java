package com.buaa.blockchain.vm; 

import org.junit.Test; 
import org.junit.Before; 
import org.junit.After; 

/** 
* Memory Tester. 
* 
* @author nolan.zhang 
* @since <pre>Dec 17, 2020</pre> 
* @version 1.0 
*/ 
public class MemoryTest {
    private static final int WORD_SIZE = 32;
    private static final int CHUNK_SIZE = 1024;

    @Before
    public void before() throws Exception { 
    } 

    @After
    public void after() throws Exception { 
    } 

        /** 
     * 
     * Method: read(int address, int size) 
     * 
     */ 
    @Test
    public void testRead() throws Exception { 
        //TODO: Test goes here... 
    } 

        /** 
     * 
     * Method: write(int address, byte[] data, int dataSize, boolean limited) 
     * 
     */ 
    @Test
    public void testWrite() throws Exception { 
        //TODO: Test goes here... 
    } 

        /** 
     * 
     * Method: extendAndWrite(int address, int allocSize, byte[] data) 
     * 
     */ 
    @Test
    public void testExtendAndWrite() throws Exception { 
        //TODO: Test goes here... 
    } 

        /** 
     * 
     * Method: extend(int address, int size) 
     * 
     */ 
    @Test
    public void testExtend() throws Exception { 
        //TODO: Test goes here... 
    } 

        /** 
     * 
     * Method: readWord(int address) 
     * 
     */ 
    @Test
    public void testReadWord() throws Exception { 
        //TODO: Test goes here... 
    } 

        /** 
     * 
     * Method: readByte(int address) 
     * 
     */ 
    @Test
    public void testReadByte() throws Exception { 
        //TODO: Test goes here... 
    } 

        /** 
     * 
     * Method: size() 
     * 
     */ 
    @Test
    public void testSize() throws Exception { 
        //TODO: Test goes here... 
    } 

        /** 
     * 
     * Method: internalSize() 
     * 
     */ 
    @Test
    public void testInternalSize() throws Exception { 
        //TODO: Test goes here... 
    } 

        /** 
     * 
     * Method: getChunks() 
     * 
     */ 
    @Test
    public void testGetChunks() throws Exception { 
        //TODO: Test goes here... 
    } 

    
        /** 
     * 
     * Method: captureMax(int chunkIndex, int chunkOffset, int size, byte[] src, int srcPos) 
     * 
     */ 
    @Test
    public void testCaptureMax() throws Exception { 
        //TODO: Test goes here... 
                    /* 
                    try { 
                       Method method = Memory.getClass().getMethod("captureMax", int.class, int.class, int.class, byte[].class, int.class); 
                       method.setAccessible(true); 
                       method.invoke(<Object>, <Parameters>); 
                    } catch(NoSuchMethodException e) { 
                    } catch(IllegalAccessException e) { 
                    } catch(InvocationTargetException e) { 
                    } 
                    */ 
            } 

        /** 
     * 
     * Method: grabMax(int chunkIndex, int chunkOffset, int size, byte[] dest, int destPos) 
     * 
     */ 
    @Test
    public void testGrabMax() throws Exception { 
        //TODO: Test goes here... 
                    /* 
                    try { 
                       Method method = Memory.getClass().getMethod("grabMax", int.class, int.class, int.class, byte[].class, int.class); 
                       method.setAccessible(true); 
                       method.invoke(<Object>, <Parameters>); 
                    } catch(NoSuchMethodException e) { 
                    } catch(IllegalAccessException e) { 
                    } catch(InvocationTargetException e) { 
                    } 
                    */ 
            } 

        /** 
     * 
     * Method: addChunks(int num) 
     * 
     */ 
    @Test
    public void testAddChunks() throws Exception { 
        //TODO: Test goes here... 
                    /* 
                    try { 
                       Method method = Memory.getClass().getMethod("addChunks", int.class); 
                       method.setAccessible(true); 
                       method.invoke(<Object>, <Parameters>); 
                    } catch(NoSuchMethodException e) { 
                    } catch(IllegalAccessException e) { 
                    } catch(InvocationTargetException e) { 
                    } 
                    */ 
            } 

    } 
