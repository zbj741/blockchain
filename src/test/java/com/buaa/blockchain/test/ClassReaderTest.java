package com.buaa.blockchain.test;

import com.buaa.blockchain.contract.util.classreader.ClassReaderApiImpl;


public class ClassReaderTest {
    public static void main(String[] args) throws Exception {
        ClassReaderApiImpl api = new ClassReaderApiImpl();
        // 测试对某一个class文件的读取
        api.read("D:/Develop/PRJ/buaa-blockchain/target/classes/com/buaa/blockchain/contract/WorldState.class");
    }
}
