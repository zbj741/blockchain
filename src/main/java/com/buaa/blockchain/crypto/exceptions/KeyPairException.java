package com.buaa.blockchain.crypto.exceptions;

public class KeyPairException extends RuntimeException {
    public KeyPairException(String message) {
        super(message);
    }

    public KeyPairException(String message, Throwable cause) {
        super(message, cause);
    }
}
