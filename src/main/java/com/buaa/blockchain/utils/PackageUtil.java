package com.buaa.blockchain.utils;

import com.buaa.blockchain.crypto.utils.ByteUtils;
import com.buaa.blockchain.vm.DataWord;
import com.buaa.blockchain.vm.utils.ByteArrayUtil;
import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * xxxx
 *
 * @author <a href="http://github.com/hackdapp">hackdapp</a>
 * @date 2021/1/14
 * @since JDK1.8
 */
public class PackageUtil {

    public static byte[] pack(Class[] classes) throws IOException, ClassNotFoundException {
        byte[] lenBytes = new byte[]{(byte) classes.length};
        byte[] classBytes = new byte[]{};
        for (Class item : classes) {
            InputStream itemIn = PackageUtil.class.getResourceAsStream("/" + item.getName() + ".class");
            try {
                byte[] itemBytes = ByteStreams.toByteArray(itemIn);
                byte[] itemByteLen = ByteUtils.bigIntegerToBytes(BigInteger.valueOf(itemBytes.length+32), 2);
                classBytes = ByteArrayUtil.merge(classBytes, DataWord.of(item.getName().getBytes()).getData());
                classBytes = ByteArrayUtil.merge(classBytes, itemBytes);
                lenBytes = ByteArrayUtil.merge(lenBytes, itemByteLen);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (itemIn != null) {
                    itemIn.close();
                }
            }
        }
        return ByteArrayUtil.merge(lenBytes, classBytes);
    }

    public static List<byte[]> unPack(byte[] bytes) {
        int classNum = bytes[0];
        int beginIndex = classNum * 2 + 1;

        List<byte[]> rtnList = new ArrayList<>();
        for (int i = 0; i < classNum; i++) {
            int classLen = ByteUtils.byteArrayToInt(ByteUtils.parseBytes(bytes, i * 2 + 1, 2));
            byte[] classBytes = ByteUtils.parseBytes(bytes, beginIndex, classLen);

            rtnList.add(classBytes);
            beginIndex += classLen;
        }
        return rtnList;
    }
}
