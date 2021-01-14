package com.buaa.blockchain.utils;

import com.buaa.blockchain.crypto.HashUtil;
import com.buaa.blockchain.vm.DataWord;
import com.buaa.blockchain.vm.utils.ByteArrayUtil;
import com.buaa.blockchain.vm.utils.HexUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * xxxx
 *
 * @author <a href="http://github.com/hackdapp">hackdapp</a>
 * @date 2020/12/23
 * @since JDK1.8
 */
public class HexTest {
    @Test
    public void testHex(){
        System.out.println(DataWord.of("1".getBytes()).getData().length);
        System.out.println(DataWord.of("2221".getBytes()).getData().length);
        System.out.println(DataWord.of("1333".getBytes()).getData().length);
        System.out.println(DataWord.of("1444".getBytes()).getData().length);

        System.out.println("c.buaachain.contract.DemoContract".getBytes().length);
        System.out.println(new String(ByteArrayUtil.stripLeadingZeroes(DataWord.of("123456".getBytes()).getData())));
        System.out.println(new String(ByteArrayUtil.stripLeadingZeroes(DataWord.of("333112".getBytes()).getData())));
        System.out.println(new String(ByteArrayUtil.stripLeadingZeroes(DataWord.of("12345690".getBytes()).getData())));
        System.out.println(new String(ByteArrayUtil.stripLeadingZeroes(DataWord.of("com.buaachain.contract.DemoContract".getBytes()).getData())));
    }

    @Test
    public void testMethodHash(){
        String contractName = "DemoUserContract";
        String code = "public class A {}";

        byte[] all = ByteArrayUtil.merge(DataWord.of(contractName.getBytes()).getData(), code.getBytes());
        byte[] mbyte = ByteUtil.parseBytes(all, 0, 32);
        byte[] cbyte = ByteUtil.parseBytes(all, 32, all.length-32);
        System.out.println(mbyte.length);
        System.out.println(cbyte.length);

        System.out.println(new String(ByteArrayUtil.stripLeadingZeroes(mbyte)));
        System.out.println(new String(cbyte));
    }

    @Test
    public void hexHash(){
        final byte[] x = HashUtil.randomHash();
        System.out.println(x);
        System.out.println(new String(x));
        System.out.println(HexUtil.toHexString(x));
    }

    @Test
    public void testCast() throws JsonProcessingException {
        Map<String, Long> test = new HashMap();
        test.put("abc", 1l);


        Long a = test.get("abc");

        String str = new ObjectMapper().writeValueAsString(test);
        Map newB = new ObjectMapper().readValue(str, new TypeReference<Map<String, Long>>() {});
        Long newC = (Long) newB.get("abc");
        System.out.println(newC);
    }
}
