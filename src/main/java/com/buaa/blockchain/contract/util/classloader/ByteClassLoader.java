package com.buaa.blockchain.contract.util.classloader;


import com.buaa.blockchain.contract.WorldState;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;

@Slf4j
public class ByteClassLoader extends ClassLoader{
    private byte[] byteInput;
    /**
     * 按照参数返回class
     * @param input class文件完整路径
     * @param className class代码的全限定类名
     * @return 对应class文件生成的Class对象
     * */
    public static synchronized Class getClass(byte[] input,String className) throws ClassNotFoundException{
        ByteClassLoader byteClassLoader = new ByteClassLoader(input);
        Class clazz = byteClassLoader.loadClass(className);
        return clazz;
    }

    private ByteClassLoader(byte[] input) {
        this.byteInput = input;
    }

    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        byte[] classBytes = this.byteInput;
        if (classBytes != null) {
            return defineClass(name, classBytes, 0, classBytes.length);
        }
        return super.findClass(name);
    }




}
