package com.buaa.blockchain.crypto.signature;

import com.buaa.blockchain.crypto.exceptions.SignatureException;
import com.buaa.blockchain.crypto.keypair.CryptoKeyPair;
import com.buaa.blockchain.crypto.utils.Hex;
import com.webank.wedpr.crypto.CryptoResult;
import com.webank.wedpr.crypto.NativeInterface;

public class SM2Signature implements Signature {
    @Override
    public SignatureResult sign(final String message, final CryptoKeyPair keyPair) {
        return new SM2SignatureResult(
                keyPair.getHexPublicKey(), signWithStringSignature(message, keyPair));
    }

    @Override
    public SignatureResult sign(final byte[] message, final CryptoKeyPair keyPair) {
        return sign(Hex.toHexString(message), keyPair);
    }

    @Override
    public String signWithStringSignature(final String message, final CryptoKeyPair keyPair) {
        CryptoResult signatureResult =
                NativeInterface.sm2SignWithPub(
                        keyPair.getHexPrivateKey(), keyPair.getHexPublicKey(), message);
        if (signatureResult.wedprErrorMessage != null
                && !signatureResult.wedprErrorMessage.isEmpty()) {
            throw new SignatureException(
                    "Sign with sm2 failed:" + signatureResult.wedprErrorMessage);
        }
        return signatureResult.signature;
    }

    @Override
    public boolean verify(final String publicKey, final String message, final String signature) {
        CryptoResult verifyResult = NativeInterface.sm2verify(publicKey, message, signature);
        if (verifyResult.wedprErrorMessage != null && !verifyResult.wedprErrorMessage.isEmpty()) {
            throw new SignatureException(
                    "Verify with sm2 failed:" + verifyResult.wedprErrorMessage);
        }
        return verifyResult.result;
    }

    @Override
    public boolean verify(final String publicKey, final byte[] message, final byte[] signature) {
        return verify(publicKey, Hex.toHexString(message), Hex.toHexString(signature));
    }
}
