package com.buaa.blockchain.crypto.keystore;

import com.buaa.blockchain.crypto.exceptions.LoadKeyStoreException;
import com.buaa.blockchain.crypto.exceptions.SaveKeyStoreException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class PEMKeyStore extends KeyTool {
    public static final String PRIVATE_KEY = "PRIVATE KEY";
    private PemObject pem;

    public PEMKeyStore(final String keyStoreFile) {
        super(keyStoreFile);
    }

    public PEMKeyStore(InputStream keyStoreFileInputStream) {
        super(keyStoreFileInputStream);
    }

    @Override
    protected PublicKey getPublicKey() {
        try {
            X509EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(pem.getContent());
            KeyFactory keyFactory =
                    KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
            return keyFactory.generatePublic(encodedKeySpec);
        } catch (InvalidKeySpecException | NoSuchProviderException | NoSuchAlgorithmException e) {
            throw new LoadKeyStoreException(
                    "getPublicKey from pem file "
                            + keyStoreFile
                            + " failed, error message: "
                            + e.getMessage(),
                    e);
        }
    }

    public static void storeKeyPairWithPemFormat(
            String hexedPrivateKey, String privateKeyFilePath, String curveName)
            throws SaveKeyStoreException {
        try {
            PrivateKey privateKey = convertHexedStringToPrivateKey(hexedPrivateKey, curveName);
            // save the private key
            PemWriter writer = new PemWriter(new FileWriter(privateKeyFilePath));
            writer.writeObject(new PemObject(PRIVATE_KEY, privateKey.getEncoded()));
            writer.flush();
            writer.close();
            storePublicKeyWithPem(privateKey, privateKeyFilePath);
        } catch (IOException | LoadKeyStoreException e) {
            throw new SaveKeyStoreException(
                    "save keys into "
                            + privateKeyFilePath
                            + " failed, error information: "
                            + e.getMessage(),
                    e);
        }
    }

    protected void load(InputStream in) {
        try {
            PemReader pemReader = new PemReader(new InputStreamReader(in));
            pem = pemReader.readPemObject();
            pemReader.close();
        } catch (IOException e) {
            String errorMessage =
                    "load key info from the pem file "
                            + keyStoreFile
                            + " failed, error message:"
                            + e.getMessage();
            logger.error(errorMessage);
            throw new LoadKeyStoreException(errorMessage, e);
        }
        if (pem == null) {
            logger.error("The file " + keyStoreFile + " does not represent a pem account.");
            throw new LoadKeyStoreException("The file does not represent a pem account.");
        }
    }

    protected PrivateKey getPrivateKey() {
        try {
            PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(pem.getContent());
            KeyFactory keyFacotry =
                    KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
            return keyFacotry.generatePrivate(encodedKeySpec);
        } catch (InvalidKeySpecException | NoSuchProviderException | NoSuchAlgorithmException e) {
            String errorMessage =
                    "getPrivateKey from pem file "
                            + keyStoreFile
                            + " failed, error message:"
                            + e.getMessage();
            logger.error(errorMessage);
            throw new LoadKeyStoreException(errorMessage, e);
        }
    }
}
