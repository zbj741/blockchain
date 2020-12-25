package com.buaa.blockchain.contract;

import com.buaa.blockchain.contract.util.classloader.ByteClassLoader;
import com.buaa.blockchain.vm.utils.HexUtil;
import com.google.common.io.ByteStreams;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

/**
 * xxxx
 *
 * @author <a href="http://github.com/hackdapp">hackdapp</a>
 * @date 2020/12/23
 * @since JDK1.8
 */
public class ClassLoaderTest {

    @Test
    public void testClassLoad() throws ClassNotFoundException, IOException {
        InputStream in = ClassLoaderTest.class.getResourceAsStream("/DemoUserContract.class");

        byte[] byteData = ByteStreams.toByteArray(in);
        System.out.println(HexUtil.toHexString(byteData));
        Assert.assertEquals(1045, byteData.length);
    }

    @Test
    public void testMethodLength() throws IOException, ClassNotFoundException {
        InputStream in = ClassLoaderTest.class.getResourceAsStream("/DemoUserContract.class");
        byte[] byteData = ByteStreams.toByteArray(in);

        String contract_name = "com.buaa.blockchain.contract.DemoUserContract";

        ByteClassLoader byteClassLoader = new ByteClassLoader(ClassLoaderTest.class.getClassLoader());
        byteClassLoader.loadDataInBytes(byteData, contract_name);
        Class<?> classZ = byteClassLoader.loadClass(contract_name);
        Assert.assertEquals(3, classZ.getDeclaredMethods().length);
    }
}
