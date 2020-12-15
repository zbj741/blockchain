package com.buaa.blockchain.crypto.hash;

public interface Hash {
    String hash(final String inputData);

    String hashBytes(byte[] inputBytes);

    byte[] hash(final byte[] inputBytes);
}
