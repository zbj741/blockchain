package com.buaa.blockchain.test;

import com.buaa.blockchain.contract.ClassScreen.ClassReaderApiImpl;


public class ClassReaderTest {
    public static void main(String[] args) throws Exception {
        ClassReaderApiImpl api = new ClassReaderApiImpl();
        api.read("D:/中移_new/buaa-blockchain/target/classes/com/buaa/blockchain/contract/ClassScreen/ClassReaderApiImpl.class");
    }
}
