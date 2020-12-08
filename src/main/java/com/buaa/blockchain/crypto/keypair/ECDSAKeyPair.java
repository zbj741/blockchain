package com.buaa.blockchain.crypto.keypair;

import com.buaa.blockchain.crypto.hash.Keccak256;
import com.webank.wedpr.crypto.CryptoResult;
import com.webank.wedpr.crypto.NativeInterface;

import java.security.KeyPair;

public class ECDSAKeyPair extends CryptoKeyPair {
    public ECDSAKeyPair() {
        initECDSAKeyPair();
    }

    public ECDSAKeyPair(KeyPair javaKeyPair) {
        super(javaKeyPair);
        initECDSAKeyPair();
    }

    protected ECDSAKeyPair(final CryptoResult ecKeyPairInfo) {
        super(ecKeyPairInfo);
        initECDSAKeyPair();
    }

    private void initECDSAKeyPair() {
        this.hashImpl = new Keccak256();
        this.curveName = CryptoKeyPair.ECDSA_CURVE_NAME;
        this.keyStoreSubDir = ECDSA_ACCOUNT_SUBDIR;
        this.signatureAlgorithm = ECDSA_SIGNATURE_ALGORITHM;
    }

    /**
     * generate keyPair randomly
     *
     * @return the generated keyPair
     */
    @Override
    public CryptoKeyPair generateKeyPair() {
        return new ECDSAKeyPair(NativeInterface.secp256k1keyPair());
    }

    @Override
    public CryptoKeyPair createKeyPair(KeyPair javaKeyPair) {
        return new ECDSAKeyPair(javaKeyPair);
    }
}
