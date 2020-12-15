package com.buaa.blockchain.crypto;

import com.buaa.blockchain.config.Config;
import com.buaa.blockchain.config.ConfigOption;
import com.buaa.blockchain.config.exceptions.ConfigException;
import com.buaa.blockchain.config.model.CryptoType;
import com.buaa.blockchain.crypto.hash.Hash;
import com.buaa.blockchain.crypto.hash.Keccak256;
import com.buaa.blockchain.crypto.hash.SM3Hash;
import com.buaa.blockchain.crypto.keypair.CryptoKeyPair;
import com.buaa.blockchain.crypto.keypair.ECDSAKeyPair;
import com.buaa.blockchain.crypto.keypair.SM2KeyPair;
import com.buaa.blockchain.crypto.keystore.KeyTool;
import com.buaa.blockchain.crypto.keystore.P12KeyStore;
import com.buaa.blockchain.crypto.keystore.PEMKeyStore;
import com.buaa.blockchain.crypto.signature.ECDSASignature;
import com.buaa.blockchain.crypto.signature.SM2Signature;
import com.buaa.blockchain.crypto.signature.Signature;
import com.buaa.blockchain.crypto.signature.SignatureResult;
import org.bouncycastle.util.encoders.Hex;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.math.BigInteger;

/**
* CryptoSuite Tester.
*
* @author nolan.zhang
* @since <pre>Dec 7, 2020</pre>
* @version 1.0
*/
public class CryptoSuiteTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }


    @Test
    public void testCryptoSuiteForECDSA() {
        CryptoSuite cryptoSuite = new CryptoSuite(CryptoType.ECDSA_TYPE);
        // generate keyPair
        CryptoKeyPair keyPair = cryptoSuite.createKeyPair();
        // test signature
        testSignature(cryptoSuite, keyPair);

        // load KeyPair from the given privateKey
        String privateKeyStr =
                "47300381232944006945664493109832654111051142806262820216166278362539860431476";
        String publicKeyStr =
                "2179819159336280954262570523402774481036769289289277534998346117714415641803934346338726829054711133487295949018624582253372411779380507548447040213240521";
        String hexedPublicKey = new BigInteger(publicKeyStr).toString(16);
        BigInteger privateKey = new BigInteger(privateKeyStr);
        keyPair = cryptoSuite.getKeyPairFactory().createKeyPair(privateKey);
        // check publicKey
        System.out.println("hexedPublicKey: " + hexedPublicKey);
        System.out.println("keyPair.getHexPublicKey(): " + keyPair.getHexPublicKey());
        Assert.assertEquals(hexedPublicKey, keyPair.getHexPublicKey().substring(2));
        testSignature(cryptoSuite, keyPair);

        String hexedPrivateKeyStr = "bcec428d5205abe0f0cc8a734083908d9eb8563e31f943d760786edf42ad67dd";
        keyPair = cryptoSuite.getKeyPairFactory().createKeyPair(hexedPrivateKeyStr);
        testSignature(cryptoSuite, keyPair);
    }

    @Test
    public void testCryptoSuiteForSM2() {
        CryptoSuite cryptoSuite = new CryptoSuite(CryptoType.SM_TYPE);
        // generate keyPair
        CryptoKeyPair keyPair = cryptoSuite.createKeyPair();
        // test signature
        testSignature(cryptoSuite, keyPair);
    }

    @Test
    public void testECDSASignature() {
        Signature ecdsaSignature = new ECDSASignature();
        CryptoKeyPair keyPair = (new ECDSAKeyPair()).generateKeyPair();
        Hash hasher = new Keccak256();
        testSignature(hasher, ecdsaSignature, keyPair);
    }

    @Test
    public void testSM2Signature() {
        Signature sm2Signature = new SM2Signature();
        CryptoKeyPair keyPair = (new SM2KeyPair()).generateKeyPair();
        Hash hasher = new SM3Hash();
        testSignature(hasher, sm2Signature, keyPair);
    }

    public void testSignature(Hash hasher, Signature signature, CryptoKeyPair keyPair) {
        String message = "abcde";
        byte[] messageBytes = message.getBytes();
        // check valid case
        for (int i = 0; i < 10; i++) {
            String plainText = "abcd----" + Integer.toString(i);
            message = hasher.hash(plainText);
            messageBytes = hasher.hash(plainText.getBytes());
            Assert.assertTrue(message.equals(Hex.toHexString(messageBytes)));
            // sign
            SignatureResult signResult = signature.sign(message, keyPair);
            // verify
            Assert.assertTrue(
                    signature.verify(
                            keyPair.getHexPublicKey(), message, signResult.convertToString()));
            signResult = signature.sign(messageBytes, keyPair);
            Assert.assertTrue(
                    signature.verify(
                            keyPair.getHexPublicKey(), message, signResult.convertToString()));
        }

        // check invalid case
        for (int i = 0; i < 10; i++) {
            message = hasher.hash("abcd----" + Integer.toString(i));
            String plainText = "abcd---" + Integer.toString(i + 1);
            String invalidMessage = hasher.hash(plainText);
            byte[] invalidBytes = hasher.hash(plainText.getBytes());
            Assert.assertTrue(invalidMessage.equals(Hex.toHexString(invalidBytes)));
            // sign
            SignatureResult signResult = signature.sign(message, keyPair);
            // verify
            Assert.assertEquals(
                    false,
                    signature.verify(
                            keyPair.getHexPublicKey(),
                            invalidMessage,
                            signResult.convertToString()));
            signResult = signature.sign(messageBytes, keyPair);
            Assert.assertEquals(
                    false,
                    signature.verify(
                            keyPair.getHexPublicKey(),
                            invalidMessage,
                            signResult.convertToString()));
        }
    }

    public void testSignature(CryptoSuite signature, CryptoKeyPair keyPair) {
        String message = "abcde";
        byte[] messageBytes = message.getBytes();
        // check valid case
        for (int i = 0; i < 10; i++) {
            // Note: the message must be hash
            String plainText = "abcd----" + Integer.toString(i);
            message = signature.hash(plainText);
            messageBytes = signature.hash(plainText.getBytes());
            Assert.assertTrue(message.equals(Hex.toHexString(messageBytes)));
            // sign
            SignatureResult signResult = signature.sign(message, keyPair);
            // verify
            Assert.assertTrue(
                    signature.verify(
                            keyPair.getHexPublicKey(), message, signResult.convertToString()));
            signResult = signature.sign(messageBytes, keyPair);
            Assert.assertTrue(
                    signature.verify(
                            keyPair.getHexPublicKey(), message, signResult.convertToString()));
        }

        // check invalid case
        for (int i = 0; i < 10; i++) {
            message = signature.hash("abcd----" + Integer.toString(i));
            String plainText = "abcd---" + Integer.toString(i + 1);
            String invalidMessage = signature.hash(plainText);
            byte[] invalidMessageBytes = signature.hash(plainText.getBytes());
            Assert.assertTrue(invalidMessage.equals(Hex.toHexString(invalidMessageBytes)));
            // sign
            SignatureResult signResult = signature.sign(message, keyPair);
            // verify
            Assert.assertEquals(
                    false,
                    signature.verify(
                            keyPair.getHexPublicKey(),
                            invalidMessage,
                            signResult.convertToString()));
            signResult = signature.sign(messageBytes, keyPair);
            Assert.assertEquals(
                    false,
                    signature.verify(
                            keyPair.getHexPublicKey(),
                            invalidMessage,
                            signResult.convertToString()));
        }
    }

    @Test
    public void testSignAndVerifyWithKeyManager() {
        String publicKeyPem =
                "keystore/ecdsa/0x45e14c53197adbcb719d915fb93342c25600faaf.public.pem";
        KeyTool verifykeyTool =
                new PEMKeyStore(getClass().getClassLoader().getResource(publicKeyPem).getPath());

        String keyPairPem = "keystore/ecdsa/0x45e14c53197adbcb719d915fb93342c25600faaf.p12";
        KeyTool signKeyTool =
                new P12KeyStore(
                        getClass().getClassLoader().getResource(keyPairPem).getPath(), "123456");
        CryptoSuite cryptoSuite = new CryptoSuite(CryptoType.ECDSA_TYPE);
        // sign and verify message with keyManager
        for (int i = 0; i < 10; i++) {
            String message = cryptoSuite.hash("abcd----" + Integer.toString(i));
            String signature = cryptoSuite.sign(signKeyTool, message);
            Assert.assertTrue(cryptoSuite.verify(verifykeyTool, message, signature));
            String invalidMessage = cryptoSuite.hash("abcde----" + Integer.toString(i));
            Assert.assertTrue(!cryptoSuite.verify(verifykeyTool, invalidMessage, signature));
        }
    }

    public String getKeyStoreFilePath(
            CryptoSuite cryptoSuite, ConfigOption configOption, String postFix) {
        return configOption.getAccountConfig().getKeyStoreDir()
                + File.separator
                + cryptoSuite.getCryptoKeyPair().getKeyStoreSubDir()
                + File.separator
                + cryptoSuite.getCryptoKeyPair().getAddress()
                + postFix;
    }


    @Test
    public void testSMLoadAndStoreKeyPairWithPEM() throws ConfigException {
        testLoadAndStoreKeyPairWithPEM(CryptoType.SM_TYPE);
    }

    @Test
    public void testECDSALoadAndStoreKeyPairWithPEM() throws ConfigException {
        testLoadAndStoreKeyPairWithPEM(CryptoType.ECDSA_TYPE);
    }

    @Test
    public void testSMLoadAndStoreKeyPairWithP12() throws ConfigException {
        testLoadAndStoreKeyPairWithP12(CryptoType.SM_TYPE);
    }

    @Test
    public void testECDSALoadAndStoreKeyPairWithP12() throws ConfigException {
        testLoadAndStoreKeyPairWithP12(CryptoType.ECDSA_TYPE);
    }

    public void testLoadAndStoreKeyPairWithPEM(int cryptoType) throws ConfigException {
        ConfigOption configOption = Config.load("application.properties", CryptoType.ECDSA_TYPE);
        CryptoSuite cryptoSuite = new CryptoSuite(cryptoType);
        cryptoSuite.getCryptoKeyPair().setConfig(configOption);
        cryptoSuite.getCryptoKeyPair().storeKeyPairWithPemFormat();
        CryptoKeyPair orgKeyPair = cryptoSuite.getCryptoKeyPair();

        // get pem file path
        String pemFilePath =
                getKeyStoreFilePath(cryptoSuite, configOption, CryptoKeyPair.PEM_FILE_POSTFIX);
        // load pem file
        KeyTool pemManager = new PEMKeyStore(pemFilePath);
        CryptoKeyPair decodedCryptoKeyPair = cryptoSuite.createKeyPair(pemManager.getKeyPair());

        System.out.println("PEM   orgKeyPair   pub: " + orgKeyPair.getHexPublicKey());
        System.out.println("PEM decodedKeyPair pub: " + decodedCryptoKeyPair.getHexPublicKey());

        System.out.println("PEM   orgKeyPair   pri: " + orgKeyPair.getHexPrivateKey());
        System.out.println("PEM decodedKeyPair pr: " + decodedCryptoKeyPair.getHexPrivateKey());

        // test sign and verify message with
        String publicPemPath = pemFilePath + ".pub";
        KeyTool verifyKeyTool = new PEMKeyStore(publicPemPath);

        checkSignAndVerifyWithKeyManager(
                pemManager, decodedCryptoKeyPair, verifyKeyTool, cryptoSuite);
    }

    public void testLoadAndStoreKeyPairWithP12(int cryptoType) throws ConfigException {
        ConfigOption configOption = Config.load("application.properties", CryptoType.ECDSA_TYPE);
        CryptoSuite cryptoSuite = new CryptoSuite(cryptoType);
        cryptoSuite.getCryptoKeyPair().setConfig(configOption);
        String password = "123";
        cryptoSuite.getCryptoKeyPair().storeKeyPairWithP12Format(password);
        CryptoKeyPair orgKeyPair = cryptoSuite.getCryptoKeyPair();

        // get p12 file path
        String p12FilePath =
                getKeyStoreFilePath(cryptoSuite, configOption, CryptoKeyPair.P12_FILE_POSTFIX);
        // load p12 file
        KeyTool p12Manager = new P12KeyStore(p12FilePath, password);
        CryptoKeyPair decodedCryptoKeyPair = cryptoSuite.createKeyPair(p12Manager.getKeyPair());
        // check the keyPair
        System.out.println("P12   orgKeyPair   pub: " + orgKeyPair.getHexPublicKey());
        System.out.println("P12 decodedKeyPair pub: " + decodedCryptoKeyPair.getHexPublicKey());

        System.out.println("P12   orgKeyPair   pri: " + orgKeyPair.getHexPrivateKey());
        System.out.println("P12 decodedKeyPair pr: " + decodedCryptoKeyPair.getHexPrivateKey());

        Assert.assertTrue(
                orgKeyPair.getHexPrivateKey().equals(decodedCryptoKeyPair.getHexPrivateKey()));
        Assert.assertTrue(
                orgKeyPair.getHexPublicKey().equals(decodedCryptoKeyPair.getHexPublicKey()));

        // test sign and verify message with
        String publicP12Path = p12FilePath + ".pub";
        KeyTool verifyKeyTool = new PEMKeyStore(publicP12Path);
        checkSignAndVerifyWithKeyManager(
                p12Manager, decodedCryptoKeyPair, verifyKeyTool, cryptoSuite);
    }

    private void checkSignAndVerifyWithKeyManager(
            KeyTool pemManager,
            CryptoKeyPair cryptoKeyPair,
            KeyTool verifyKeyTool,
            CryptoSuite cryptoSuite) {
        // sign and verify message with cryptoKeyPair
        for (int i = 0; i < 10; i++) {
            String message = cryptoSuite.hash("abcd----" + Integer.toString(i));
            SignatureResult signature = cryptoSuite.sign(message, cryptoKeyPair);
            Assert.assertTrue(
                    cryptoSuite.verify(
                            cryptoKeyPair.getHexPublicKey(), message, signature.convertToString()));

            Assert.assertTrue(
                    cryptoSuite.verify(
                            cryptoKeyPair.getHexPublicKey(),
                            Hex.decode(message),
                            Hex.decode(signature.convertToString())));

            String invalidMessage = cryptoSuite.hash("abcde----" + Integer.toString(i));
            Assert.assertTrue(
                    !cryptoSuite.verify(
                            cryptoKeyPair.getHexPublicKey(),
                            invalidMessage,
                            signature.convertToString()));
        }
        for (int i = 0; i < 10; i++) {
            String message = cryptoSuite.hash("abcd----" + Integer.toString(i));
            String signature = cryptoSuite.sign(pemManager, message);
            Assert.assertTrue(cryptoSuite.verify(verifyKeyTool, message, signature));
            Assert.assertTrue(
                    cryptoSuite.verify(
                            verifyKeyTool, Hex.decode(message), Hex.decode(signature)));
            String invalidMessage = cryptoSuite.hash("abcde----" + Integer.toString(i));
            Assert.assertTrue(!cryptoSuite.verify(verifyKeyTool, invalidMessage, signature));
        }
    }
}