package com.buaa.blockchain.vm.utils;

import org.bouncycastle.util.encoders.Hex;

public class HexUtil {

    public static byte[] fromHexString(String hex) {
        return Hex.decode(hex.startsWith("0x") ? hex.substring(2) : hex);
    }

    public static String toHexString(byte[] data) {
                                                return Hex.toHexString(data);
                                                                             }

    public static String toHexStringWith0x(byte[] data) {
                                                      return "0x" + toHexString(data);
                                                                                      }

    public static String toHexString(byte b) {
                                           return toHexString(new byte[] { b });
                                                                                }
}
