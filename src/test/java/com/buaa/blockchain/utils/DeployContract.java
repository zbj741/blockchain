package com.buaa.blockchain.utils;

import com.buaa.blockchain.BlockchainApplication;
import com.buaa.blockchain.contract.util.classloader.ByteClassLoader;
import com.buaa.blockchain.vm.utils.HexUtil;
import com.google.common.io.ByteStreams;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

/**
 * xxxx
 *
 * @author <a href="http://github.com/hackdapp">hackdapp</a>
 * @date 2021/1/9
 * @since JDK1.8
 */
@Slf4j
public class DeployContract {

    @Test
    public void testReadContract() throws IOException, ClassNotFoundException {
        String contractName = "EisContract";
        // 1. read the contract bytes
        InputStream in = BlockchainApplication.class.getResourceAsStream("/"+contractName+".class");

        byte[] byteData = ByteStreams.toByteArray(in);
        System.out.println(byteData.length);
        System.out.println((byte)7362);
        System.out.println(HexUtil.toHexString(byteData));

        ByteClassLoader byteClassLoader = new ByteClassLoader(DeployContract.class.getClassLoader());
        byteClassLoader.loadDataInBytes(byteData, contractName);
        Class<?> classZ = byteClassLoader.loadClass(contractName);
        System.out.println(classZ.getSimpleName());

        byte[] test = ByteUtil.bigIntegerToBytes(BigInteger.valueOf(65536), 2);
        System.out.println(ByteUtil.bytesToBigInteger(test));
    }
}
