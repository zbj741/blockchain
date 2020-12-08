package com.buaa.blockchain.crypto.keypair;

import com.buaa.blockchain.crypto.hash.SM3Hash;
import com.webank.wedpr.crypto.CryptoResult;
import com.webank.wedpr.crypto.NativeInterface;

import java.security.KeyPair;

public class SM2KeyPair extends CryptoKeyPair {
    public SM2KeyPair() {
        initSM2KeyPairObject();
    }

    public SM2KeyPair(KeyPair javaKeyPair) {
        super(javaKeyPair);
        initSM2KeyPairObject();
    }

    protected SM2KeyPair(CryptoResult sm2keyPairInfo) {
        super(sm2keyPairInfo);
        initSM2KeyPairObject();
    }

    private void initSM2KeyPairObject() {
        this.keyStoreSubDir = GM_ACCOUNT_SUBDIR;
        this.hashImpl = new SM3Hash();
        this.curveName = CryptoKeyPair.SM2_CURVE_NAME;
        this.signatureAlgorithm = SM_SIGNATURE_ALGORITHM;
    }

    /**
     * generate keyPair randomly
     *
     * @return the generated keyPair
     */
    @Override
    public CryptoKeyPair generateKeyPair() {
        return new SM2KeyPair(NativeInterface.sm2keyPair());
    }

    @Override
    public CryptoKeyPair createKeyPair(KeyPair javaKeyPair) {
        return new SM2KeyPair(javaKeyPair);
    }
}
