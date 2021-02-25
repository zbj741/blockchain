package com.buaa.blockchain.crypto;


import com.buaa.blockchain.crypto.rlp.RlpDecoder;
import com.buaa.blockchain.crypto.rlp.RlpList;
import com.buaa.blockchain.crypto.rlp.RlpString;
import com.buaa.blockchain.crypto.signature.ECDSASignatureResult;
import com.buaa.blockchain.crypto.signature.SignatureResult;
import com.buaa.blockchain.crypto.utils.Numeric;
import com.buaa.blockchain.entity.Transaction;

import java.math.BigInteger;

public class TransactionDecoder {

    public static Transaction decode(String hexTransaction) {
        byte[] transaction = Numeric.hexStringToByteArray(hexTransaction);
        RlpList rlpList = RlpDecoder.decode(transaction);
        RlpList values = (RlpList) rlpList.getValues().get(0);
        byte[] to = ((RlpString) values.getValues().get(0)).asString().getBytes();
        BigInteger value = ((RlpString) values.getValues().get(1)).asPositiveBigInteger();
        byte[] data = ((RlpString) values.getValues().get(2)).asString().getBytes();

        if (values.getValues().size() > 3) {
            byte v = ((RlpString) values.getValues().get(3)).getBytes()[0];
            byte[] r = Numeric.toBytesPadded(Numeric.toBigInt(((RlpString) values.getValues().get(4)).getBytes()), 32);
            byte[] s = Numeric.toBytesPadded(Numeric.toBigInt(((RlpString) values.getValues().get(5)).getBytes()), 32);
            SignatureResult signatureResult = new ECDSASignatureResult(v, r, s);
            return new SignTransaction(to, value, data, signatureResult);
        } else {
            return new Transaction(to, value, data);
        }
    }

}
