package com.buaa.blockchain.crypto.exceptions;

public class SaveKeyStoreException extends RuntimeException {
    public SaveKeyStoreException(String message) {
        super(message);
    }

    public SaveKeyStoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
