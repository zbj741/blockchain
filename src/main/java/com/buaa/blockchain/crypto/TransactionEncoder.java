package com.buaa.blockchain.crypto;

import com.buaa.blockchain.config.CryptoType;
import com.buaa.blockchain.crypto.keypair.CryptoKeyPair;
import com.buaa.blockchain.crypto.rlp.RlpEncoder;
import com.buaa.blockchain.crypto.rlp.RlpList;
import com.buaa.blockchain.crypto.rlp.RlpString;
import com.buaa.blockchain.crypto.rlp.RlpType;
import com.buaa.blockchain.crypto.signature.SignatureResult;
import com.buaa.blockchain.crypto.utils.Numeric;
import com.buaa.blockchain.entity.Transaction;

import java.util.ArrayList;
import java.util.List;


public class TransactionEncoder {

    public static byte[] signMessage(Transaction transaction, CryptoKeyPair keyPair) {
        byte[] encodedTransaction = encode(transaction);
        CryptoSuite cryptoSuite = new CryptoSuite(CryptoType.ECDSA_TYPE);
        SignatureResult signatureResult = cryptoSuite.sign(encodedTransaction, keyPair);
        return encode(transaction, signatureResult);
    }

    public static byte[] encode(Transaction transaction) {
        return encode(transaction, null);
    }

    private static byte[] encode(Transaction transaction, SignatureResult signatureResult) {
        List<RlpType> values = asRlpValues(transaction, signatureResult);
        RlpList rlpList = new RlpList(values);
        return RlpEncoder.encode(rlpList);
    }

    private static List<RlpType> asRlpValues(Transaction transaction,  SignatureResult signatureResult) {
        List<RlpType> result = new ArrayList<>();
        String to = new String(transaction.getTo());
        if (to != null && to.length() > 0) {
            result.add(RlpString.create(Numeric.hexStringToByteArray(to)));
        } else {
            result.add(RlpString.create(""));
        }
        result.add(RlpString.create(transaction.getValue()));
        result.add(RlpString.create(transaction.getData()));

        if (signatureResult != null) {
            result.addAll(signatureResult.encode());
        }
        return result;
    }
}
