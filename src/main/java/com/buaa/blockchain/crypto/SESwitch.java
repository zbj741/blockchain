package com.buaa.blockchain.crypto;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SESwitch {
    /*
    * 对称加密选择器
    * */
    private static final int AES_KEY_LENGTH_LEVEL_2 = 128;
    private static final int AES_KEY_LENGTH_LEVEL_3 = 192;
    private static final int AES_KEY_LENGTH_LEVEL_4 = 256;
    /**
     * 对称加密操作
     *
     * @param content 加密内容
     * @param key 密钥字符串
     * @param secretLevel 加密等级 0=原文 1=DES 2=AES_128 3=192 4=256
     * @return 密文
     */
    public static String SymmetricEncrypt(String content, String key,final int secretLevel){
        if (key==null){
            Logger.getLogger(SESwitch.class.getName()).log(Level.SEVERE, "密钥为null", new Exception());
            return null;
        }
        if (secretLevel<0||secretLevel>4){
            Logger.getLogger(SESwitch.class.getName()).log(Level.SEVERE, "不存在安全等级:"+secretLevel, new Exception());
            return null;
        }
        if (0==secretLevel) {
            return content;
        }
        else if (1==secretLevel) {
            return DESUtils.encrypt(content, key);
        }
        else if (2==secretLevel) {
            return AESUtils.encrypt(content, key, AES_KEY_LENGTH_LEVEL_2);
        }
        else if (3==secretLevel) {
            return AESUtils.encrypt(content, key, AES_KEY_LENGTH_LEVEL_3);
        }
        else{
            //4==secretLevel
            return AESUtils.encrypt(content, key, AES_KEY_LENGTH_LEVEL_4);
        }
    }
    /**
     * 对称解密操作
     *
     * @param content 解密内容
     * @param key 密钥字符串
     * @param secretLevel 解密等级 0=原文 1=DES 2=AES_128 3=192 4=256
     * @return 明文
     */
    public static String SymmetricDecrypt(String content, String key,int secretLevel){
        if (0==secretLevel) {
            return content;
        }
        else if (1==secretLevel) {
            return DESUtils.decrypt(content, key);
        }
        else if (2==secretLevel) {
            return AESUtils.decrypt(content, key, AES_KEY_LENGTH_LEVEL_2);
        }
        else if (3==secretLevel) {
            return AESUtils.decrypt(content, key, AES_KEY_LENGTH_LEVEL_3);
        }
        else if (4==secretLevel) {
            return AESUtils.decrypt(content, key, AES_KEY_LENGTH_LEVEL_4);
        }
        else return "";
    }
}
