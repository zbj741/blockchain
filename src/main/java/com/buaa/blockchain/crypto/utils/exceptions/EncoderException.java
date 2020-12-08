package com.buaa.blockchain.crypto.utils.exceptions;

/** Exception thrown if an attempt is made to encode invalid data, or some other failure occurs. */
public class EncoderException extends IllegalStateException {
    private final Throwable cause;

    public EncoderException(String msg, Throwable cause) {
        super(msg);

        this.cause = cause;
    }

    @Override
    public final synchronized Throwable getCause() {
        return cause;
    }
}
