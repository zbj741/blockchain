package com.buaa.blockchain.crypto;

import com.buaa.blockchain.crypto.keypair.CryptoKeyPair;
import com.buaa.blockchain.crypto.keypair.ECDSAKeyPair;
import com.buaa.blockchain.crypto.keypair.SM2KeyPair;
import com.buaa.blockchain.crypto.signature.ECDSASignatureResult;
import com.buaa.blockchain.crypto.signature.SM2SignatureResult;
import com.buaa.blockchain.crypto.signature.SignatureResult;
import com.buaa.blockchain.crypto.utils.Hex;
import com.buaa.blockchain.entity.Transaction;
import com.buaa.blockchain.vm.crypto.ECKey;

import java.math.BigInteger;
import java.security.SignatureException;

/**
 * xxxx
 *
 * @author <a href="http://github.com/hackdapp">hackdapp</a>
 * @date 2021/2/20
 * @since JDK1.8
 */
public class SignTransaction extends Transaction {
    private SignatureResult signatureResult;

    public SignTransaction(byte[] to, BigInteger value, byte[] data, SignatureResult signatureResult) {
        super(to, value, data);
        this.signatureResult = signatureResult;
    }

    public String getSignFrom(){
        if (signatureResult instanceof SM2SignatureResult){
            CryptoKeyPair keyPair = new SM2KeyPair();
            SM2SignatureResult sm2SignatureResult = (SM2SignatureResult) this.signatureResult;
            return keyPair.getAddress(Hex.toHexString(sm2SignatureResult.getPub()));
        } else {
            CryptoKeyPair keyPair = new ECDSAKeyPair();

            ECDSASignatureResult signatureResult = (ECDSASignatureResult)this.signatureResult;
            byte[] r = signatureResult.getR();
            byte[] s = signatureResult.getS();
            byte v = (byte) 0x1b;
            ECKey.ECDSASignature sig = ECKey.ECDSASignature.fromComponents(r, s, v);

            byte[]  encodedTransaction = TransactionEncoder.encode(this);
            byte[] pubKeyBytes = ECKey.recoverPubBytesFromSignature(0, sig, encodedTransaction);
            return keyPair.getAddress(Hex.toHexString(pubKeyBytes));
        }
    }

    public void verify(String from) throws SignatureException {
        String actualFrom = getSignFrom();
        if (!actualFrom.equals(from)) {
            throw new SignatureException("from mismatch");
        }
    }
}
