/** interface for sign/verify functions */
package com.buaa.blockchain.crypto.signature;


import com.buaa.blockchain.crypto.keypair.CryptoKeyPair;

public interface Signature {
    /**
     * sign message with the given keyPair
     *
     * @param message the message to be signed, must be hash value
     * @param keyPair the keyPair used to generate the signature
     * @return the signature result
     */
    SignatureResult sign(final byte[] message, final CryptoKeyPair keyPair);

    SignatureResult sign(final String message, final CryptoKeyPair keyPair);

    String signWithStringSignature(final String message, final CryptoKeyPair keyPair);

    /**
     * verify signature
     *
     * @param publicKey the publickey
     * @param message the message, must be hash value
     * @param signature the signature to be verified
     * @return true/false
     */
    boolean verify(final String publicKey, final String message, final String signature);

    boolean verify(final String publicKey, final byte[] message, final byte[] signature);
}
