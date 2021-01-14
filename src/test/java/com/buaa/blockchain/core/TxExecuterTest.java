package com.buaa.blockchain.core;

import com.buaa.blockchain.config.ChainConfig;
import com.buaa.blockchain.config.CryptoType;
import com.buaa.blockchain.contract.WorldState;
import com.buaa.blockchain.contract.model.CallMethod;
import com.buaa.blockchain.crypto.CryptoSuite;
import com.buaa.blockchain.crypto.keypair.CryptoKeyPair;
import com.buaa.blockchain.entity.Transaction;
import com.buaa.blockchain.entity.UserAccount;
import com.buaa.blockchain.vm.DataWord;
import com.buaa.blockchain.vm.utils.ByteArrayUtil;
import com.buaa.blockchain.vm.utils.HexUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** 
* TxExecuter Tester. 
* 
* @author nolan.zhang 
* @since <pre>Dec 24, 2020</pre> 
* @version 1.0 
*/
@Slf4j
public class TxExecuterTest { 
    private TxExecuter txExecuter;
    private WorldState worldState;
    private String roothash = "8ee3688358df8cf3dcc7ad8152d9548f57808bd2ee35128cac2ced11ef1fd69b";
    @Before
    public void before() throws Exception {
        ChainConfig chainConfig = new ChainConfig();
        chainConfig.setStatedbDir("./data/leveldb");
        chainConfig.setStatedbName("triedb");
        chainConfig.setCryptoType(CryptoType.ECDSA_TYPE);
        worldState = new WorldState(chainConfig.getStatedbDir(), chainConfig.getStatedbName());
        if(roothash!=""){
            worldState.switchRoot(roothash);
        }
        txExecuter = new TxExecuter(chainConfig, worldState);

        CryptoSuite cryptoSuite = new CryptoSuite(CryptoType.ECDSA_TYPE);
        List<String> list = new ArrayList();
        for (int i = 0; i < 5; i++) {
            CryptoKeyPair keyPair = cryptoSuite.createKeyPair();
            String address = keyPair.getAddress();
            String prikey = keyPair.getHexPrivateKey();
            UserAccount userAccount = new UserAccount();
            final BigInteger val = BigInteger.valueOf(10000);
            userAccount.addBalance(val);
            worldState.createAccount(new String(address), userAccount);
            list.add(address+","+prikey+","+val);
        }
        log.info("====================================");
        for(String item : list){
            String[] val = item.split(",");
            log.info("addr: {}, pkey: {}, value: {}", val[0], val[1], val[2]);
        }
        log.info("====================================");
    }

    @After
    public void after() throws Exception { 
    } 

        /** 
     * 
     * Method: batchExecute(List<Transaction> transactionList) 
     * 
     */ 
    @Test
    public void testBatchExecute() throws Exception { 
        //TODO: Test goes here... 
    } 

    
        /** 
     * 
     * Method: execute(Transaction transaction) 
     * 
     */ 
    @Test
    public void testDeployContract() throws Exception {
        String contractName = "DemoUserContract";
        String hexCode = "cafebabe0000003400280a0006001b090005001c0b001d001e0b001d001f0700200700210100036d617001000f4c6a6176612f7574696c2f4d61703b0100063c696e69743e010012284c6a6176612f7574696c2f4d61703b2956010004436f646501000f4c696e654e756d6265725461626c650100124c6f63616c5661726961626c655461626c65010004746869730100124c44656d6f55736572436f6e74726163743b0100104d6574686f64506172616d657465727301000a696e7365727455736572010027284c6a6176612f6c616e672f537472696e673b4c6a6176612f6c616e672f537472696e673b295601000269640100124c6a6176612f6c616e672f537472696e673b0100046e616d6501000a7570646174655573657201000764656c55736572010015284c6a6176612f6c616e672f537472696e673b295601000a536f7572636546696c6501001544656d6f55736572436f6e74726163742e6a6176610c000900220c000700080700230c002400250c0026002701001044656d6f55736572436f6e74726163740100106a6176612f6c616e672f4f626a65637401000328295601000d6a6176612f7574696c2f4d6170010003707574010038284c6a6176612f6c616e672f4f626a6563743b4c6a6176612f6c616e672f4f626a6563743b294c6a6176612f6c616e672f4f626a6563743b01000672656d6f7665010026284c6a6176612f6c616e672f4f626a6563743b294c6a6176612f6c616e672f4f626a6563743b002100050006000000010000000700080000000400010009000a0002000b00000046000200020000000a2ab700012a2bb50002b100000002000c0000000e00030000000e0004000f00090010000d0000001600020000000a000e000f00000000000a00070008000100100000000501000700000001001100120002000b0000004f000300030000000d2ab400022b2cb90003030057b100000002000c0000000a000200000013000c0014000d0000002000030000000d000e000f00000000000d0013001400010000000d0015001400020010000000090200130000001500000001001600120002000b0000004f000300030000000d2ab400022b2cb90003030057b100000002000c0000000a000200000017000c0018000d0000002000030000000d000e000f00000000000d0013001400010000000d0015001400020010000000090200130000001500000001001700180002000b00000044000200020000000c2ab400022bb90004020057b100000002000c0000000a00020000001b000b001c000d0000001600020000000c000e000f00000000000c00130014000100100000000501001300000001001900000002001a";

        byte[] data = ByteArrayUtil.merge(DataWord.of(contractName.getBytes()).getData(), HexUtil.fromHexString(hexCode));

        String from = "0x9e217168f3b10cb9287b76be7ca943c1fb6cf5b0";
        Transaction transaction = new Transaction();
        transaction.setFrom(from.getBytes());
        transaction.setTo(null);
        transaction.setData(data);
        txExecuter.execute(transaction);

        System.out.println(worldState.getRootHash());
    }


    @Test
    public void testCallMethod() throws JsonProcessingException {
        String contractAddr = "0xd11e9e614a62f538cbc750535a324ee2dfae0849";
        String from = "0x9e217168f3b10cb9287b76be7ca943c1fb6cf5b0";

        CallMethod callMethod = new CallMethod("insertUser", new String[]{"1", "22"});
        String data = new ObjectMapper().writeValueAsString(callMethod);
        Transaction transaction = new Transaction();
        transaction.setFrom(from.getBytes());
        transaction.setTo(contractAddr.getBytes());
        transaction.setData(data.getBytes());
        txExecuter.execute(transaction);


        UserAccount userAccount = worldState.getUser(contractAddr);
        String storage_data = worldState.get(HexUtil.toHexString(userAccount.getStorageHash()));
        Map storage = new ObjectMapper().readValue(storage_data, Map.class);
        Assert.assertEquals(true, storage.containsKey("1"));
        Assert.assertEquals("22", storage.get("1"));
    }

    @Test
    public void testTransfer(){
        CryptoSuite cryptoSuite = new CryptoSuite(CryptoType.ECDSA_TYPE);
        CryptoKeyPair keyPair = cryptoSuite.createKeyPair();
        String from = keyPair.getAddress();
        worldState.addBalance(from, BigInteger.valueOf(10000));
        String to = cryptoSuite.createKeyPair().getAddress();

        Transaction transaction = new Transaction();
        transaction.setFrom(from.getBytes());
        transaction.setTo(to.getBytes());
        transaction.setValue(BigInteger.valueOf(100));
        txExecuter.execute(transaction);

        Assert.assertEquals(100l, worldState.getBalance(to).longValue());
        Assert.assertEquals(9900l, worldState.getBalance(from).longValue());
    }

    @Test
    public void testContractAddr(){
//        UserAccount userAccount = worldState.getUser("0xb4b4df3d8375516a3c9df242567bbea51ae486cd");
//        System.out.println(userAccount.toString());
//        worldState.update("1", "111");
//        worldState.sync();
        System.out.println( worldState.getRootHash());
        System.out.println(worldState.get("1"));
    }
}
