package com.buaa.blockchain.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.buaa.blockchain.crypto.CodingUtils.bytesToHexString;

public class SHASwitch {

    /*
     * 摘要选择器
     * */

    private static final String MESSAGE_DIGEST_LEVEL_0 = "MD5";
    private static final String MESSAGE_DIGEST_LEVEL_1 = "SHA-224";
    private static final String MESSAGE_DIGEST_LEVEL_2 = "SHA-256";
    private static final String MESSAGE_DIGEST_LEVEL_3 = "SHA-384";
    private static final String MESSAGE_DIGEST_LEVEL_4 = "SHA-512";
    public static String sign(String signString,final int secretLevel){
        if (secretLevel<0||secretLevel>4){
            Logger.getLogger(SHASwitch.class.getName()).log(Level.SEVERE, "不存在安全等级:"+secretLevel, new Exception());
            return null;
        }
        if (0==secretLevel) {
            //该方法不安全，慎用
            return Encrypt(signString,MESSAGE_DIGEST_LEVEL_0);
        }
        else if (1==secretLevel) {
            return Encrypt(signString,MESSAGE_DIGEST_LEVEL_1);
        }
        else if (2==secretLevel) {
            return Encrypt(signString,MESSAGE_DIGEST_LEVEL_2);
        }
        else if (3==secretLevel) {
            return Encrypt(signString,MESSAGE_DIGEST_LEVEL_3);
        }
        else{
            //4==secretLevel
            return Encrypt(signString,MESSAGE_DIGEST_LEVEL_4);
        }
    }
    public static String Encrypt(String strSrc, String encName) {
        MessageDigest md = null;
        String strDes = null;
        byte[] bt = strSrc.getBytes();
        try {
            md = MessageDigest.getInstance(encName);
            md.update(bt);
            strDes = bytesToHexString(md.digest()); // to HexString
        } catch (NoSuchAlgorithmException e) {
            System.out.println("签名失败！");
            Logger.getLogger(SHASwitch.class.getName()).log(Level.SEVERE, encName+"签名失败!", new Exception());
            return null;
        }
        return strDes;
    }
}