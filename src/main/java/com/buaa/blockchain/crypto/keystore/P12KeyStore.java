package com.buaa.blockchain.crypto.keystore;

import com.buaa.blockchain.crypto.exceptions.LoadKeyStoreException;
import com.buaa.blockchain.crypto.exceptions.SaveKeyStoreException;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.x509.X509V3CertificateGenerator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;

public class P12KeyStore extends KeyTool {
    private static final String NAME = "key";
    private KeyStore keyStore;

    public P12KeyStore(final String keyStoreFile, final String password) {
        super(keyStoreFile, password);
    }

    public P12KeyStore(InputStream keyStoreFileInputStream, final String password) {
        super(keyStoreFileInputStream, password);
    }

    @Override
    public PublicKey getPublicKey() {
        try {
            Certificate certificate = keyStore.getCertificate(NAME);
            return certificate.getPublicKey();
        } catch (KeyStoreException e) {
            throw new LoadKeyStoreException(
                    "getPublicKey from p12 file "
                            + keyStoreFile
                            + " failed, error message: "
                            + e.getMessage(),
                    e);
        }
    }

    /**
     * load keyPair from the given input stream
     *
     * @param in the input stream that should used to load keyPair
     */
    protected void load(InputStream in) {
        try {
            keyStore = KeyStore.getInstance("PKCS12", "BC");
            String password = "";
            if (this.password != null) {
                password = this.password;
            }
            keyStore.load(in, password.toCharArray());

        } catch (IOException
                | CertificateException
                | NoSuchAlgorithmException
                | NoSuchProviderException
                | KeyStoreException e) {
            String errorMessage =
                    "load keys from p12 file "
                            + keyStoreFile
                            + " failed, error message:"
                            + e.getMessage();
            logger.error(errorMessage);
            throw new LoadKeyStoreException(errorMessage, e);
        }
    }

    /**
     * get private key from the keyStore
     *
     * @return the private key
     */
    protected PrivateKey getPrivateKey() {
        try {
            return (PrivateKey) keyStore.getKey(NAME, password.toCharArray());
        } catch (UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException e) {
            String errorMessage =
                    "get private key from "
                            + keyStoreFile
                            + " failed for UnrecoverableKeyException, error message"
                            + e.getMessage();
            logger.error(errorMessage);
            throw new LoadKeyStoreException(errorMessage, e);
        }
    }

    public static void storeKeyPairWithP12Format(
            String hexedPrivateKey,
            String password,
            String privateKeyFilePath,
            String curveName,
            String signatureAlgorithm)
            throws SaveKeyStoreException {
        try {
            PrivateKey privateKey = convertHexedStringToPrivateKey(hexedPrivateKey, curveName);
            // save the private key
            KeyStore keyStore = KeyStore.getInstance("PKCS12", "BC");
            // load to init the keyStore
            keyStore.load(null, password.toCharArray());
            KeyPair keyPair = new KeyPair(getPublicKeyFromPrivateKey(privateKey), privateKey);
            // Since KeyStore setEntry must set the certificate chain, a self-signed certificate is
            // generated
            Certificate[] certChain = new Certificate[1];
            certChain[0] = generateSelfSignedCertificate(keyPair, signatureAlgorithm);
            keyStore.setKeyEntry(NAME, privateKey, password.toCharArray(), certChain);
            keyStore.store(new FileOutputStream(privateKeyFilePath), password.toCharArray());
            // store the public key
            storePublicKeyWithPem(privateKey, privateKeyFilePath);
        } catch (IOException
                | KeyStoreException
                | NoSuchProviderException
                | NoSuchAlgorithmException
                | CertificateException
                | LoadKeyStoreException
                | InvalidKeyException
                | SignatureException e) {
            throw new SaveKeyStoreException(
                    "save private key into "
                            + privateKeyFilePath
                            + " failed, error information: "
                            + e.getMessage(),
                    e);
        }
    }

    /**
     * generate self-signed certificate
     *
     * @param keyPair the keyPair used to generated the certificate
     * @param signatureAlgorithm the signature algorithm of the cert
     * @throws NoSuchAlgorithmException no such algorithm exception
     * @throws CertificateEncodingException error occurs when encoding certificate
     * @throws NoSuchProviderException no such provider exception
     * @throws InvalidKeyException invalid key exception
     * @throws SignatureException generic signature exception
     * @return the generated self-signed certificate object
     */
    public static X509Certificate generateSelfSignedCertificate(
            KeyPair keyPair, String signatureAlgorithm)
            throws NoSuchAlgorithmException, CertificateEncodingException, NoSuchProviderException,
                    InvalidKeyException, SignatureException {
        X509V3CertificateGenerator cert = new X509V3CertificateGenerator();
        cert.setSerialNumber(BigInteger.valueOf(1)); // or generate a random number
        cert.setSubjectDN(new X509Principal("CN=localhost")); // see examples to add O,OU etc
        cert.setIssuerDN(new X509Principal("CN=localhost")); // same since it is self-signed
        cert.setPublicKey(keyPair.getPublic());
        Calendar notBefore = Calendar.getInstance();
        Calendar notAfter = Calendar.getInstance();
        notBefore.add(Calendar.YEAR, 100);
        cert.setNotBefore(notBefore.getTime());
        cert.setNotAfter(notAfter.getTime());
        cert.setSignatureAlgorithm(signatureAlgorithm);
        cert.setPublicKey(keyPair.getPublic());
        PrivateKey signingKey = keyPair.getPrivate();
        return cert.generate(signingKey, "BC");
    }
}
