package com.buaa.blockchain.utils;

import com.google.common.io.ByteStreams;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ReflectUtilTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    @Test
    public void testLoadClass() throws Exception {
        InputStream in = ReflectUtilTest.class.getResourceAsStream("/DemoContract.class");
        byte[] byteData = ByteStreams.toByteArray(in);

        String contract_name = "DemoContract";

        Class classZ = ReflectUtil.getInstance().loadClass(contract_name, byteData);
        Assert.assertEquals(contract_name, classZ.getCanonicalName());
    }

    @Test
    public void testNewInstance() throws Exception {
        Assert.assertEquals(ReflectUtil.getInstance(), ReflectUtil.getInstance());
    }

    /**
     * Method: invoke(Class classObj, Object objInstance, String methodName, Object... param)
     */
    @Test
    public void testInvoke() throws Exception {
        InputStream in = ReflectUtilTest.class.getResourceAsStream("/DemoContract.class");
        byte[] byteData = ByteStreams.toByteArray(in);

        String contract_name = "DemoContract";

        final Map param = new HashMap();
        Class classZ = ReflectUtil.getInstance().loadClass(contract_name, byteData);
        Object obj = ReflectUtil.getInstance().newInstance(classZ, Map.class,  param);
        ReflectUtil.getInstance().invoke(classZ, obj, "insertUser", new String[]{"1", "111"});

        Assert.assertEquals(1, param.size());
        Assert.assertEquals("111", param.get("1"));

        ReflectUtil.getInstance().invoke(classZ, obj, "updateUser", new String[]{"1", "222"});
        Assert.assertEquals(1, param.size());
        Assert.assertEquals("222", param.get("1"));

        ReflectUtil.getInstance().invoke(classZ, obj, "delUser", new String[]{"1"});
        Assert.assertEquals(0, param.size());
        Assert.assertEquals(null, param.get("1"));
    }
}
