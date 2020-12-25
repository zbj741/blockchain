package com.buaa.blockchain.vm.spec;

import com.buaa.blockchain.entity.Transaction;
import com.buaa.blockchain.vm.DataWord;
import com.buaa.blockchain.vm.client.Repository;
import com.buaa.blockchain.vm.crypto.ECKey;
import com.buaa.blockchain.vm.program.ProgramResult;
import com.buaa.blockchain.vm.utils.HashUtil;
import com.buaa.blockchain.vm.utils.HexUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.buaa.blockchain.vm.utils.ByteArrayUtil.EMPTY_BYTE_ARRAY;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** 
* DefaultSpec Tester. 
* 
* @author nolan.zhang 
* @since <pre>Dec 17, 2020</pre> 
* @version 1.0 
*/ 
public class DefaultSpecTest { 
    private Spec spec = new DefaultSpec();

    @Before
    public void before() throws Exception { 
    } 

    @After
    public void after() throws Exception { 
    } 

    private PrecompiledContractContext wrapData(byte[] data){
       return new PrecompiledContractContext(){
           private Repository repo = mock(Repository.class);
           private ProgramResult result = mock(ProgramResult.class);
           private Transaction tx = mock(Transaction.class);

           {
               when(tx.getData()).thenReturn(data);
           }
           @Override
           public Repository getTrack() {
               return repo;
           }

           @Override
           public ProgramResult getResult() {
               return result;
           }

           @Override
           public Transaction getTransaction() {
               return tx;
           }
       };
    }

    @Test
    public void testIdentityContract(){
        DataWord addr = DataWord.of("0000000000000000000000000000000000000000000000000000000000000004");
        PrecompiledContract contract = spec.getPrecompiledContracts().getContractForAddress(addr);
        byte[] data = HexUtil.fromHexString("123456");
        byte[] expected = HexUtil.fromHexString("123456");

        byte[] result = contract.execute(wrapData(data)).getRight();

        assertArrayEquals(expected, result);
    }

    @Test
    public void testSHA256Contract1(){
        DataWord addr = DataWord.of("0000000000000000000000000000000000000000000000000000000000000002");
        PrecompiledContract contract = spec.getPrecompiledContracts().getContractForAddress(addr);
        byte[] data = EMPTY_BYTE_ARRAY;
        byte[] result = contract.execute(wrapData(data)).getRight();

        String expected = HexUtil.toHexString(HashUtil.sha256(data));
        assertEquals(expected, HexUtil.toHexString(result));
    }

    @Test
    public void testSHA256Contract2(){
        DataWord addr = DataWord.of("0000000000000000000000000000000000000000000000000000000000000002");
        PrecompiledContract contract = spec.getPrecompiledContracts().getContractForAddress(addr);
        byte[] data = HexUtil.fromHexString("11");
        byte[] result = contract.execute(wrapData(data)).getRight();

        String expected = HexUtil.toHexString(HashUtil.sha256(data));
        assertEquals(expected, HexUtil.toHexString(result));
    }

    @Test
    public void testRecoverTest1() throws Exception {
        byte[] messageHash = HexUtil.fromHexString("14431339128bd25f2c7f93baa611e367472048757f4ad67f6d71a5ca0da550f5");
        byte v = 28;
        byte[] r = HexUtil.fromHexString("51e4dbbbcebade695a3f0fdf10beb8b5f83fda161e1a3105a14c41168bf3dce0");
        byte[] s = HexUtil.fromHexString("46eabf35680328e26ef4579caf8aeb2cf9ece05dbf67a4f3d1f28c7b1d0e3546");
        byte[] address = ECKey.signatureToAddress(messageHash, ECKey.ECDSASignature.fromComponents(r, s, v));

        String expected = "7f8b3b04bf34618f4a1723fba96b5db211279a2b";
        assertEquals(expected, HexUtil.toHexString(address));

        byte[] data = HexUtil.fromHexString("14431339128bd25f2c7f93baa611e367"
                + "472048757f4ad67f6d71a5ca0da550f5"
                + "00000000000000000000000000000000"
                + "0000000000000000000000000000001c"
                + "51e4dbbbcebade695a3f0fdf10beb8b5"
                + "f83fda161e1a3105a14c41168bf3dce0"
                + "46eabf35680328e26ef4579caf8aeb2c"
                + "f9ece05dbf67a4f3d1f28c7b1d0e3546");
        DataWord addr = DataWord.of("0000000000000000000000000000000000000000000000000000000000000001");
        PrecompiledContract contract = spec.getPrecompiledContracts().getContractForAddress(addr);
        String expected2 = "0000000000000000000000007f8b3b04bf34618f4a1723fba96b5db211279a2b";

        byte[] result = contract.execute(wrapData(data)).getRight();
        assertEquals(expected2, HexUtil.toHexString(result));

    }

}
