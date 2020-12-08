package com.buaa.blockchain.crypto.hash;


import com.buaa.blockchain.crypto.exceptions.HashException;
import com.buaa.blockchain.crypto.utils.Hex;
import com.webank.wedpr.crypto.CryptoResult;
import com.webank.wedpr.crypto.NativeInterface;

public class Keccak256 implements Hash {

    @Override
    public String hash(final String inputData) {
        return calculateHash(inputData.getBytes());
    }

    @Override
    public byte[] hash(final byte[] inputBytes) {
        return Hex.decode(calculateHash(inputBytes));
    }

    @Override
    public String hashBytes(byte[] inputBytes) {
        return calculateHash(inputBytes);
    }

    private String calculateHash(final byte[] inputBytes) {
        CryptoResult hashResult = NativeInterface.keccak256(Hex.toHexString(inputBytes));
        if (hashResult.wedprErrorMessage != null && !hashResult.wedprErrorMessage.isEmpty()) {
            throw new HashException(
                    "Calculate hash with keccak256 failed! error message:"
                            + hashResult.wedprErrorMessage);
        }
        return hashResult.hash;
    }
}
