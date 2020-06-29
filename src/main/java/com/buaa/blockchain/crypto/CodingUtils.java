package com.buaa.blockchain.crypto;

public class CodingUtils {

    /**
     * byte数组转Base64格式字符串
     * @param text byte数组
     * @return Base64格式字符串
     */
    public static String bytesToBase64String(byte[] text){
        return Base64Utils.encode(text);
    }

    /**
     * Base64格式字符串转byte数组
     * @param text Base64格式字符串
     * @return byte数组
     */
    public static byte[] base64StringToBytes(String text) {
        return Base64Utils.decode(text);
    }

    /**
     * 将字节数组转换成16进制字符串
     * @param bytes 即将转换的数据
     * @return 16进制字符串
     */
    public static String bytesToHexString(byte[] bytes){
        StringBuilder sb = new StringBuilder(bytes.length);
        String temp = null;
        for (byte aByte : bytes) {
            temp = Integer.toHexString(0xFF & aByte);
            if (temp.length() < 2) {
                sb.append(0);
            }
            sb.append(temp);
        }
        return sb.toString();
    }

    /**
     * 将16进制字符串转换成字节数组
     * @param hex 16进制字符串
     * @return byte[]
     */
    public static byte[] hexStringToBytes(String hex){
        int len = (hex.length() / 2);
        hex = hex.toUpperCase();
        byte[] result = new byte[len];
        char[] chars = hex.toCharArray();
        for (int i= 0;i<len;i++){
            int pos = i * 2;
            result[i] = (byte)(chartoByte(chars[pos]) << 4 | chartoByte(chars[pos + 1]));
        }
        return result;
    }

    /**
     * 将char转换为byte
     * @param c char
     * @return byte
     */
    private static byte chartoByte(char c){
        return (byte)"0123456789ABCDEF".indexOf(c);
    }
}
