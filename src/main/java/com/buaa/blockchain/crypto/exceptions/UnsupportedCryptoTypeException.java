package com.buaa.blockchain.crypto.exceptions;

public class UnsupportedCryptoTypeException extends RuntimeException {
    public UnsupportedCryptoTypeException(String message) {
        super(message);
    }

    public UnsupportedCryptoTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
