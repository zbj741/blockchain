package com.buaa.blockchain.crypto.hash;


import com.buaa.blockchain.crypto.exceptions.HashException;
import com.buaa.blockchain.crypto.utils.Hex;
import com.webank.wedpr.crypto.CryptoResult;
import com.webank.wedpr.crypto.NativeInterface;

public class SM3Hash implements Hash {
    @Override
    public String hash(final String inputData) {
        return calculateHash(inputData.getBytes());
    }

    @Override
    public String hashBytes(byte[] inputBytes) {
        return calculateHash(inputBytes);
    }

    @Override
    public byte[] hash(final byte[] inputBytes) {
        return Hex.decode(calculateHash(inputBytes));
    }

    private String calculateHash(final byte[] inputBytes) {
        CryptoResult hashResult = NativeInterface.sm3(Hex.toHexString(inputBytes));
        if (hashResult.wedprErrorMessage != null && !hashResult.wedprErrorMessage.isEmpty()) {
            throw new HashException(
                    "calculate hash with sm3 failed, error message:"
                            + hashResult.wedprErrorMessage);
        }
        return hashResult.hash;
    }
}
