package com.buaa.blockchain.crypto.exceptions;

public class LoadKeyStoreException extends RuntimeException {
    public LoadKeyStoreException(String message) {
        super(message);
    }

    public LoadKeyStoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
