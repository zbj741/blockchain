package com.buaa.blockchain.vm; 

import com.buaa.blockchain.vm.utils.HexUtil;
import org.junit.Test;
import org.junit.Before; 
import org.junit.After;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 
* DataWord Tester. 
* 
* @author nolan.zhang 
* @since <pre>Dec 15, 2020</pre> 
* @version 1.0 
*/ 
public class DataWordTest { 

    @Before
    public void before() throws Exception { 
    } 

    @After
    public void after() throws Exception { 
    } 


    /**
     * 
     * Method: mul(DataWord word) 
     * 
     */ 
    @Test
    public void testAdd() throws Exception {
        byte[] three = new byte[32];
        for (int i = 0; i < three.length; i++) {
            three[i] = (byte) 0xff;
        }

        DataWord x = DataWord.of(three);
        x.add(DataWord.of(three));
        assertEquals(32, x.getData().length);
    } 

    @Test
    public void testMod() throws Exception {
        String expected = "000000000000000000000000000000000000000000000000000000000000001a";

        byte[] one = new byte[32];
        one[31] = 0x1e; // 0x000000000000000000000000000000000000000000000000000000000000001e

        byte[] two = new byte[32];
        for (int i = 0; i < two.length; i++) {
            two[i] = (byte) 0xff;
        }
        two[31] = 0x56; // 0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff56

        DataWord x = DataWord.of(one);System.out.println(x.value());
        DataWord y = DataWord.of(two); System.out.println(y.value());
        DataWord z = y.mod(x);
        assertEquals(32, z.getData().length);
        assertEquals(expected, HexUtil.toHexString(z.getData()));
    }

    @Test
    public void testMul() {
        byte[] one = new byte[32];
        one[31] = 0x1; // 0x0000000000000000000000000000000000000000000000000000000000000001

        byte[] two = new byte[32];
        two[11] = 0x1; // 0x0000000000000000000000010000000000000000000000000000000000000000

        DataWord x = DataWord.of(one);// System.out.println(x.value());
        DataWord y = DataWord.of(two);// System.out.println(y.value());
        DataWord z = x.mul(y);
        assertEquals(32, z.getData().length);
        assertEquals("0000000000000000000000010000000000000000000000000000000000000000",
                HexUtil.toHexString(z.getData()));
    }

    @Test
    public void testMulOverflow() {

        byte[] one = new byte[32];
        one[30] = 0x1;

        byte[] two = new byte[32];
        two[0] = 0x1;

        DataWord x = DataWord.of(one);
        System.out.println(x);
        DataWord y = DataWord.of(two);
        System.out.println(y);
        DataWord z = x.mul(y);
        System.out.println(z);

        assertEquals(32, z.getData().length);
        assertEquals("0000000000000000000000000000000000000000000000000000000000000000",
                HexUtil.toHexString(z.getData()));
    }

    @Test
    public void testDiv() {
        byte[] one = new byte[32];
        one[30] = 0x01;
        one[31] = 0x2c; // 0x000000000000000000000000000000000000000000000000000000000000012c

        byte[] two = new byte[32];
        two[31] = 0x0f; // 0x000000000000000000000000000000000000000000000000000000000000000f

        DataWord x = DataWord.of(one);
        DataWord y = DataWord.of(two);
        DataWord z = x.div(y);

        assertEquals(32, z.getData().length);
        assertEquals("0000000000000000000000000000000000000000000000000000000000000014",
                HexUtil.toHexString(z.getData()));
    }

    @Test
    public void testDivZero() {
        byte[] one = new byte[32];
        one[30] = 0x05; // 0x0000000000000000000000000000000000000000000000000000000000000500

        byte[] two = new byte[32];

        DataWord x = DataWord.of(one);
        DataWord y = DataWord.of(two);
        DataWord z = x.div(y);

        assertEquals(32, z.getData().length);
        assertTrue(z.isZero());
    }

    @Test
    public void testSDivNegative() {

        // one is -300 as 256-bit signed integer:
        byte[] one = HexUtil.fromHexString("fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffed4");

        byte[] two = new byte[32];
        two[31] = 0x0f;

        DataWord x = DataWord.of(one);
        DataWord y = DataWord.of(two);
        DataWord z = x.sDiv(y);

        assertEquals(32, z.getData().length);
        assertEquals("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffec", z.toString());
    }

    @Test
    public void testSignExtend1() {

        DataWord x = DataWord.of(HexUtil.fromHexString("f2"));
        byte k = 0;
        String expected = "fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff2";

        DataWord z = x.signExtend(k);
        System.out.println(z.toString());
        assertEquals(expected, z.toString());
    }

    @Test
    public void testSignExtend2() {
        DataWord x = DataWord.of(HexUtil.fromHexString("f2"));
        byte k = 1;
        String expected = "00000000000000000000000000000000000000000000000000000000000000f2";

        DataWord z = x.signExtend(k);
        System.out.println(z.toString());
        assertEquals(expected, z.toString());
    }

    @Test
    public void testSignExtend3() {

        byte k = 1;
        DataWord x = DataWord.of(HexUtil.fromHexString("0f00ab"));
        String expected = "00000000000000000000000000000000000000000000000000000000000000ab";

        DataWord z = x.signExtend(k);
        System.out.println(z.toString());
        assertEquals(expected, z.toString());
    }

    @Test
    public void testSignExtend4() {

        byte k = 1;
        DataWord x = DataWord.of(HexUtil.fromHexString("ffff"));
        String expected = "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";

        DataWord z = x.signExtend(k);
        System.out.println(z.toString());
        assertEquals(expected, z.toString());
    }

    @Test
    public void testSignExtend5() {

        byte k = 3;
        DataWord x = DataWord.of(HexUtil.fromHexString("ffffffff"));
        String expected = "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";

        DataWord z = x.signExtend(k);
        System.out.println(z.toString());
        assertEquals(expected, z.toString());
    }

    @Test
    public void testSignExtend6() {

        byte k = 3;
        DataWord x = DataWord.of(HexUtil.fromHexString("ab02345678"));
        String expected = "0000000000000000000000000000000000000000000000000000000002345678";

        DataWord z = x.signExtend(k);
        System.out.println(z.toString());
        assertEquals(expected, z.toString());
    }

    @Test
    public void testSignExtend7() {

        byte k = 3;
        DataWord x = DataWord.of(HexUtil.fromHexString("ab82345678"));
        String expected = "ffffffffffffffffffffffffffffffffffffffffffffffffffffffff82345678";

        DataWord z = x.signExtend(k);
        System.out.println(z.toString());
        assertEquals(expected, z.toString());
    }

    @Test
    public void testSignExtend8() {

        byte k = 30;
        DataWord x = DataWord.of(
                HexUtil.fromHexString("ff34567882345678823456788234567882345678823456788234567882345678"));
        String expected = "0034567882345678823456788234567882345678823456788234567882345678";

        DataWord z = x.signExtend(k);
        System.out.println(z.toString());
        assertEquals(expected, z.toString());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSignExtendException1() {

        byte k = -1;
        DataWord x = DataWord.of(0);

        x.signExtend(k); // should throw an exception
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSignExtendException2() {

        byte k = 32;
        DataWord x = DataWord.of(0);

        x.signExtend(k); // should throw an exception
    }

    @Test
    public void testAddModOverflow() {
        testAddMod("9999999999999999999999999999999999999999999999999999999999999999",
                "8888888888888888888888888888888888888888888888888888888888888888",
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff");
        testAddMod("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff");
    }

    void testAddMod(String v1, String v2, String v3) {
        DataWord dv1 = DataWord.of(HexUtil.fromHexString(v1));
        DataWord dv2 = DataWord.of(HexUtil.fromHexString(v2));
        DataWord dv3 = DataWord.of(HexUtil.fromHexString(v3));
        BigInteger bv1 = new BigInteger(v1, 16);
        BigInteger bv2 = new BigInteger(v2, 16);
        BigInteger bv3 = new BigInteger(v3, 16);

        DataWord z = dv1.addmod(dv2, dv3);
        BigInteger br = bv1.add(bv2).mod(bv3);
        assertEquals(z.value(), br);
    }

    @Test
    public void testMulMod1() {
        DataWord wr = DataWord.of(
                HexUtil.fromHexString("9999999999999999999999999999999999999999999999999999999999999999"));
        DataWord w1 = DataWord.of(HexUtil.fromHexString("01"));
        DataWord w2 = DataWord.of(
                HexUtil.fromHexString("9999999999999999999999999999999999999999999999999999999999999998"));

        DataWord z = wr.mulmod(w1, w2);

        assertEquals(32, z.getData().length);
        assertEquals("0000000000000000000000000000000000000000000000000000000000000001",
                HexUtil.toHexString(z.getData()));
    }

    @Test
    public void testMulMod2() {
        DataWord wr = DataWord.of(
                HexUtil.fromHexString("9999999999999999999999999999999999999999999999999999999999999999"));
        DataWord w1 = DataWord.of(HexUtil.fromHexString("01"));
        DataWord w2 = DataWord.of(
                HexUtil.fromHexString("9999999999999999999999999999999999999999999999999999999999999999"));

        DataWord z = wr.mulmod(w1, w2);

        assertEquals(32, z.getData().length);
        assertTrue(z.isZero());
    }


    @Test
    public void testMulModZeroWord1() {
        DataWord wr = DataWord.of(
                HexUtil.fromHexString("9999999999999999999999999999999999999999999999999999999999999999"));
        DataWord w1 = DataWord.of(HexUtil.fromHexString("00"));
        DataWord w2 = DataWord.of(
                HexUtil.fromHexString("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"));

        DataWord z = wr.mulmod(w1, w2);

        assertEquals(32, z.getData().length);
        assertTrue(z.isZero());
    }

    @Test
    public void testMulModZeroWord2() {
        DataWord wr = DataWord.of(
                HexUtil.fromHexString("9999999999999999999999999999999999999999999999999999999999999999"));
        DataWord w1 = DataWord.of(
                HexUtil.fromHexString("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"));
        DataWord w2 = DataWord.of(HexUtil.fromHexString("00"));

        DataWord z = wr.mulmod(w1, w2);

        assertEquals(32, z.getData().length);
        assertTrue(z.isZero());
    }

    @Test
    public void testMulModOverflow() {
        DataWord wr = DataWord.of(
                HexUtil.fromHexString("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"));
        DataWord w1 = DataWord.of(
                HexUtil.fromHexString("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"));
        DataWord w2 = DataWord.of(
                HexUtil.fromHexString("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"));

        DataWord z = wr.mulmod(w1, w2);

        assertEquals(32, z.getData().length);
        assertTrue(z.isZero());
    }

    @Test
    public void testSHL() {
        assertEquals(DataWord.of("0x0000000000000000000000000000000000000000000000000000000000000001"),
                DataWord.of("0x0000000000000000000000000000000000000000000000000000000000000001")
                        .shiftLeft(DataWord.of("0x00")));
        assertEquals(DataWord.of("0x0000000000000000000000000000000000000000000000000000000000000002"),
                DataWord.of("0x0000000000000000000000000000000000000000000000000000000000000001")
                        .shiftLeft(DataWord.of("0x01")));
        assertEquals(DataWord.of("0x8000000000000000000000000000000000000000000000000000000000000000"),
                DataWord.of("0x0000000000000000000000000000000000000000000000000000000000000001")
                        .shiftLeft(DataWord.of("0xff")));
        assertEquals(DataWord.of("0x0000000000000000000000000000000000000000000000000000000000000000"),
                DataWord.of("0x0000000000000000000000000000000000000000000000000000000000000001")
                        .shiftLeft(DataWord.of("0x0100")));
        assertEquals(DataWord.of("0x0000000000000000000000000000000000000000000000000000000000000000"),
                DataWord.of("0x0000000000000000000000000000000000000000000000000000000000000001")
                        .shiftLeft(DataWord.of("0x0101")));
        assertEquals(DataWord.of("0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"),
                DataWord.of("0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")
                        .shiftLeft(DataWord.of("0x00")));
        assertEquals(DataWord.of("0xfffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe"),
                DataWord.of("0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")
                        .shiftLeft(DataWord.of("0x01")));
        assertEquals(DataWord.of("0x8000000000000000000000000000000000000000000000000000000000000000"),
                DataWord.of("0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")
                        .shiftLeft(DataWord.of("0xff")));
        assertEquals(DataWord.of("0x0000000000000000000000000000000000000000000000000000000000000000"),
                DataWord.of("0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")
                        .shiftLeft(DataWord.of("0x0100")));
        assertEquals(DataWord.of("0x0000000000000000000000000000000000000000000000000000000000000000"),
                DataWord.of("0x0000000000000000000000000000000000000000000000000000000000000000")
                        .shiftLeft(DataWord.of("0x01")));
        assertEquals(DataWord.of("0xfffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe"),
                DataWord.of("0x7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")
                        .shiftLeft(DataWord.of("0x01")));
        assertEquals(DataWord.of(""), DataWord.of("").shiftLeft(DataWord.of("")));
    }

    @Test
    public void testSHR() {
        assertEquals(DataWord.of("0x0000000000000000000000000000000000000000000000000000000000000001"),
                DataWord.of("0x0000000000000000000000000000000000000000000000000000000000000001")
                        .shiftRight(DataWord.of("0x00")));
        assertEquals(DataWord.of("0x0000000000000000000000000000000000000000000000000000000000000000"),
                DataWord.of("0x0000000000000000000000000000000000000000000000000000000000000001")
                        .shiftRight(DataWord.of("0x01")));
        assertEquals(DataWord.of("0x4000000000000000000000000000000000000000000000000000000000000000"),
                DataWord.of("0x8000000000000000000000000000000000000000000000000000000000000000")
                        .shiftRight(DataWord.of("0x01")));
        assertEquals(DataWord.of("0x0000000000000000000000000000000000000000000000000000000000000001"),
                DataWord.of("0x8000000000000000000000000000000000000000000000000000000000000000")
                        .shiftRight(DataWord.of("0xff")));
        assertEquals(DataWord.of("0x0000000000000000000000000000000000000000000000000000000000000000"),
                DataWord.of("0x8000000000000000000000000000000000000000000000000000000000000000")
                        .shiftRight(DataWord.of("0x0100")));
        assertEquals(DataWord.of("0x0000000000000000000000000000000000000000000000000000000000000000"),
                DataWord.of("0x8000000000000000000000000000000000000000000000000000000000000000")
                        .shiftRight(DataWord.of("0x0101")));
        assertEquals(DataWord.of("0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"),
                DataWord.of("0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")
                        .shiftRight(DataWord.of("0x00")));
        assertEquals(DataWord.of("0x7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"),
                DataWord.of("0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")
                        .shiftRight(DataWord.of("0x01")));
        assertEquals(DataWord.of("0x0000000000000000000000000000000000000000000000000000000000000001"),
                DataWord.of("0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")
                        .shiftRight(DataWord.of("0xff")));
        assertEquals(DataWord.of("0x0000000000000000000000000000000000000000000000000000000000000000"),
                DataWord.of("0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")
                        .shiftRight(DataWord.of("0x0100")));
        assertEquals(DataWord.of("0x0000000000000000000000000000000000000000000000000000000000000000"),
                DataWord.of("0x0000000000000000000000000000000000000000000000000000000000000000")
                        .shiftRight(DataWord.of("0x01")));
    }

    @Test
    public void testSAR() {
//        assertEquals(DataWord.of("0x0000000000000000000000000000000000000000000000000000000000000001"),
//                DataWord.of("0x0000000000000000000000000000000000000000000000000000000000000001")
//                        .shiftRightSigned(DataWord.of("0x00")));
        assertEquals(DataWord.of("0x0000000000000000000000000000000000000000000000000000000000000000"),
                DataWord.of("0x0000000000000000000000000000000000000000000000000000000000000001")
                        .shiftRightSigned(DataWord.of("0x01")));
        assertEquals(DataWord.of("0xc000000000000000000000000000000000000000000000000000000000000000"),
                DataWord.of("0x8000000000000000000000000000000000000000000000000000000000000000")
                        .shiftRightSigned(DataWord.of("0x01")));
        assertEquals(DataWord.of("0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"),
                DataWord.of("0x8000000000000000000000000000000000000000000000000000000000000000")
                        .shiftRightSigned(DataWord.of("0xff")));
        assertEquals(DataWord.of("0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"),
                DataWord.of("0x8000000000000000000000000000000000000000000000000000000000000000")
                        .shiftRightSigned(DataWord.of("0x0100")));
        assertEquals(DataWord.of("0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"),
                DataWord.of("0x8000000000000000000000000000000000000000000000000000000000000000")
                        .shiftRightSigned(DataWord.of("0x0101")));
        assertEquals(DataWord.of("0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"),
                DataWord.of("0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")
                        .shiftRightSigned(DataWord.of("0x00")));
        assertEquals(DataWord.of("0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"),
                DataWord.of("0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")
                        .shiftRightSigned(DataWord.of("0x01")));
        assertEquals(DataWord.of("0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"),
                DataWord.of("0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")
                        .shiftRightSigned(DataWord.of("0xff")));
        assertEquals(DataWord.of("0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"),
                DataWord.of("0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")
                        .shiftRightSigned(DataWord.of("0x0100")));
        assertEquals(DataWord.of("0x0000000000000000000000000000000000000000000000000000000000000000"),
                DataWord.of("0x0000000000000000000000000000000000000000000000000000000000000000")
                        .shiftRightSigned(DataWord.of("0x01")));
        assertEquals(DataWord.of("0x0000000000000000000000000000000000000000000000000000000000000001"),
                DataWord.of("0x4000000000000000000000000000000000000000000000000000000000000000")
                        .shiftRightSigned(DataWord.of("0xfe")));
        assertEquals(DataWord.of("0x000000000000000000000000000000000000000000000000000000000000007f"),
                DataWord.of("0x7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")
                        .shiftRightSigned(DataWord.of("0xf8")));
        assertEquals(DataWord.of("0x0000000000000000000000000000000000000000000000000000000000000001"),
                DataWord.of("0x7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")
                        .shiftRightSigned(DataWord.of("0xfe")));
        assertEquals(DataWord.of("0x0000000000000000000000000000000000000000000000000000000000000000"),
                DataWord.of("0x7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")
                        .shiftRightSigned(DataWord.of("0xff")));
        assertEquals(DataWord.of("0x0000000000000000000000000000000000000000000000000000000000000000"),
                DataWord.of("0x7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")
                        .shiftRightSigned(DataWord.of("0x0100")));
    }

    public static BigInteger pow(BigInteger x, BigInteger y) {
        if (y.compareTo(BigInteger.ZERO) < 0)
            throw new IllegalArgumentException();
        BigInteger z = x; // z will successively become x^2, x^4, x^8, x^16, x^32...
        BigInteger result = BigInteger.ONE;
        byte[] bytes = y.toByteArray();
        for (int i = bytes.length - 1; i >= 0; i--) {
            byte bits = bytes[i];
            for (int j = 0; j < 8; j++) {
                if ((bits & 1) != 0)
                    result = result.multiply(z);
                // short cut out if there are no more bits to handle:
                if ((bits >>= 1) == 0 && i == 0)
                    return result;
                z = z.multiply(z);
            }
        }
        return result;
    }
}

