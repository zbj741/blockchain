package com.buaa.blockchain.crypto.signature;

import com.buaa.blockchain.crypto.exceptions.SignatureException;
import com.buaa.blockchain.crypto.keypair.CryptoKeyPair;
import com.buaa.blockchain.crypto.utils.Hex;
import com.webank.wedpr.crypto.CryptoResult;
import com.webank.wedpr.crypto.NativeInterface;

public class ECDSASignature implements Signature {
    @Override
    public SignatureResult sign(final String message, final CryptoKeyPair keyPair) {
        // convert signature string to SignatureResult struct
        return new ECDSASignatureResult(signWithStringSignature(message, keyPair));
    }

    @Override
    public SignatureResult sign(final byte[] message, final CryptoKeyPair keyPair) {
        return sign(Hex.toHexString(message), keyPair);
    }

    @Override
    public String signWithStringSignature(final String message, final CryptoKeyPair keyPair) {
        CryptoResult signatureResult =
                NativeInterface.secp256k1Sign(keyPair.getHexPrivateKey(), message);
        // call secp256k1Sign failed
        if (signatureResult.wedprErrorMessage != null
                && !signatureResult.wedprErrorMessage.isEmpty()) {
            throw new SignatureException(
                    "Sign with secp256k1 failed:" + signatureResult.wedprErrorMessage);
        }
        // convert signature string to SignatureResult struct
        return signatureResult.signature;
    }

    @Override
    public boolean verify(final String publicKey, final String message, final String signature) {
        CryptoResult verifyResult = NativeInterface.secp256k1verify(publicKey, message, signature);
        // call secp256k1verify failed
        if (verifyResult.wedprErrorMessage != null && !verifyResult.wedprErrorMessage.isEmpty()) {
            throw new SignatureException(
                    "Verify with secp256k1 failed:" + verifyResult.wedprErrorMessage);
        }
        return verifyResult.result;
    }

    @Override
    public boolean verify(final String publicKey, final byte[] message, final byte[] signature) {
        return verify(publicKey, Hex.toHexString(message), Hex.toHexString(signature));
    }
}
