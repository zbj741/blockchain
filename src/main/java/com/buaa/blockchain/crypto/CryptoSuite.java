package com.buaa.blockchain.crypto;

import com.buaa.blockchain.config.ConfigOption;
import com.buaa.blockchain.config.model.AccountConfig;
import com.buaa.blockchain.config.model.CryptoType;
import com.buaa.blockchain.crypto.exceptions.LoadKeyStoreException;
import com.buaa.blockchain.crypto.exceptions.UnsupportedCryptoTypeException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyPair;

public class CryptoSuite {

    private static Logger logger = LoggerFactory.getLogger(CryptoSuite.class);

    public final int cryptoTypeConfig;

    public final Signature signatureImpl;
    public final Hash hashImpl;
    private final CryptoKeyPair keyPairFactory;
    private CryptoKeyPair cryptoKeyPair;
    private ConfigOption config;

    public CryptoSuite(int cryptoTypeConfig, ConfigOption configOption) {
        this(cryptoTypeConfig);
        logger.info("init CryptoSuite, cryptoType: {}", cryptoTypeConfig);
        setConfig(configOption);
        // doesn't set the account name, generate the keyPair randomly
        if (!configOption.getAccountConfig().isAccountConfigured()) {
            createKeyPair();
            return;
        }
        loadAccount(configOption);
    }
    /**
     * init the common crypto implementation according to the crypto type
     *
     * @param cryptoTypeConfig the crypto type config number
     */
    public CryptoSuite(int cryptoTypeConfig) {
        this.cryptoTypeConfig = cryptoTypeConfig;
        if (this.cryptoTypeConfig == CryptoType.ECDSA_TYPE) {
            this.signatureImpl = new ECDSASignature();
            this.hashImpl = new Keccak256();
            this.keyPairFactory = new ECDSAKeyPair();

        } else if (this.cryptoTypeConfig == CryptoType.SM_TYPE) {
            this.signatureImpl = new SM2Signature();
            this.hashImpl = new SM3Hash();
            this.keyPairFactory = new SM2KeyPair();

        } else {
            throw new UnsupportedCryptoTypeException(
                    "only support "
                            + CryptoType.ECDSA_TYPE
                            + "/"
                            + CryptoType.SM_TYPE
                            + " crypto type");
        }
        // create keyPair randomly
        createKeyPair();
    }

    public void loadAccount(String accountFileFormat, String accountFilePath, String password) {
        KeyTool keyTool = null;
        if (accountFileFormat.compareToIgnoreCase("p12") == 0) {
            keyTool = new P12KeyStore(accountFilePath, password);
        } else if (accountFileFormat.compareToIgnoreCase("pem") == 0) {
            keyTool = new PEMKeyStore(accountFilePath);
        } else {
            throw new LoadKeyStoreException(
                    "unsupported account file format : "
                            + accountFileFormat
                            + ", current supported are p12 and pem");
        }
        logger.debug("Load account from {}", accountFilePath);
        createKeyPair(keyTool.getKeyPair());
    }

    private void loadAccount(ConfigOption configOption) {
        AccountConfig accountConfig = configOption.getAccountConfig();
        String accountFilePath = accountConfig.getAccountFilePath();
        if (accountFilePath == null || accountFilePath.equals("")) {
            if (accountConfig.getAccountFileFormat().compareToIgnoreCase("p12") == 0) {
                accountFilePath =
                        keyPairFactory.getP12KeyStoreFilePath(accountConfig.getAccountAddress());
            } else if (accountConfig.getAccountFileFormat().compareToIgnoreCase("pem") == 0) {
                accountFilePath =
                        keyPairFactory.getPemKeyStoreFilePath(accountConfig.getAccountAddress());
            }
        }
        loadAccount(
                accountConfig.getAccountFileFormat(),
                accountFilePath,
                accountConfig.getAccountPassword());
    }

    public void setConfig(ConfigOption config) {
        this.config = config;
        this.keyPairFactory.setConfig(config);
    }

    public int getCryptoTypeConfig() {
        return cryptoTypeConfig;
    }

    public Signature getSignatureImpl() {
        return signatureImpl;
    }

    public Hash getHashImpl() {
        return hashImpl;
    }

    public String hash(final String inputData) {
        return hashImpl.hash(inputData);
    }

    public byte[] hash(final byte[] inputBytes) {
        return hashImpl.hash(inputBytes);
    }

    public SignatureResult sign(final byte[] message, final CryptoKeyPair keyPair) {
        return signatureImpl.sign(message, keyPair);
    }

    public SignatureResult sign(final String message, final CryptoKeyPair keyPair) {
        return signatureImpl.sign(message, keyPair);
    }

    // for AMOP topic verify, generate signature
    public String sign(KeyTool keyTool, String message) {
        CryptoKeyPair cryptoKeyPair = this.keyPairFactory.createKeyPair(keyTool.getKeyPair());
        return signatureImpl.signWithStringSignature(message, cryptoKeyPair);
    }

    // for AMOP topic verify, verify the signature
    public boolean verify(KeyTool keyTool, String message, String signature) {
        return verify(keyTool.getHexedPublicKey(), message, signature);
    }

    public boolean verify(KeyTool keyTool, byte[] message, byte[] signature) {
        return verify(keyTool.getHexedPublicKey(), message, signature);
    }

    public boolean verify(final String publicKey, final String message, final String signature) {
        return signatureImpl.verify(publicKey, message, signature);
    }

    public boolean verify(final String publicKey, final byte[] message, final byte[] signature) {
        return signatureImpl.verify(publicKey, message, signature);
    }

    public CryptoKeyPair createKeyPair() {
        this.cryptoKeyPair = this.keyPairFactory.generateKeyPair();
        this.cryptoKeyPair.setConfig(config);
        return this.cryptoKeyPair;
    }

    public CryptoKeyPair createKeyPair(KeyPair keyPair) {
        this.cryptoKeyPair = this.keyPairFactory.createKeyPair(keyPair);
        this.cryptoKeyPair.setConfig(config);
        return this.cryptoKeyPair;
    }

    public CryptoKeyPair createKeyPair(String hexedPrivateKey) {
        this.cryptoKeyPair = this.keyPairFactory.createKeyPair(hexedPrivateKey);
        this.cryptoKeyPair.setConfig(config);
        return this.cryptoKeyPair;
    }

    public void setCryptoKeyPair(CryptoKeyPair cryptoKeyPair) {
        this.cryptoKeyPair = cryptoKeyPair;
        this.cryptoKeyPair.setConfig(config);
    }

    public CryptoKeyPair getCryptoKeyPair() {
        return this.cryptoKeyPair;
    }

    public ConfigOption getConfig() {
        return this.config;
    }

    public CryptoKeyPair getKeyPairFactory() {
        return this.keyPairFactory;
    }
}
