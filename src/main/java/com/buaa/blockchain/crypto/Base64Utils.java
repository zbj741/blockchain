package com.buaa.blockchain.crypto;

import java.util.Base64;

public class Base64Utils {
    private static Base64Utils instance = null;
    private static Base64.Decoder decoder;
    private static Base64.Encoder encoder;
    private Base64Utils() {
        //System.out.println(Thread.currentThread().getName() + "\t 我是构造方法SingletonDemo");
        decoder = Base64.getDecoder();
        encoder = Base64.getEncoder();
    }

    //DCL  (Double Check Lock 双端捡锁机制）
    //初始化单例
    public static void getInstance() {
        if (instance == null) {
            synchronized (Base64Utils.class) {
                if (instance == null) {
                    instance = new Base64Utils();
                }
            }
        }
    }
    //触发回收机制
    public static void clear() {
        if (instance != null) {
            synchronized (Base64Utils.class) {
                if (instance != null) {
                    decoder = null;
                    encoder = null;
                    instance = null;
                }
            }
        }
    }

    public static byte[] decode(String encodedText){
        Base64Utils.getInstance();
        return decoder.decode(encodedText);
    }

    public static String encode(byte[] text){
        Base64Utils.getInstance();
        return encoder.encodeToString(text);
    }
}
