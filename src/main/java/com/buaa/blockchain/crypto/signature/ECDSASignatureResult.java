package com.buaa.blockchain.crypto.signature;

import com.buaa.blockchain.crypto.exceptions.SignatureException;
import com.buaa.blockchain.crypto.rlp.RlpString;
import com.buaa.blockchain.crypto.rlp.RlpType;
import com.buaa.blockchain.crypto.utils.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ECDSASignatureResult extends SignatureResult {
    protected static Logger logger = LoggerFactory.getLogger(SignatureResult.class);
    protected byte v;
    protected static int VBASE = 27;

    public ECDSASignatureResult(byte v, byte[] r, byte[] s) {
        super(r, s);
        this.v = v;
    }

    public ECDSASignatureResult(final String signatureResult) {
        super(signatureResult);
        if (this.signatureBytes.length != 65) {
            throw new SignatureException(
                    "Invalid signature for invalid length " + this.signatureBytes.length);
        }
        this.v = this.signatureBytes[64];
    }

    /**
     * covert signatureResult into String
     *
     * @return the signature string with [r, s, v]
     */
    @Override
    public String convertToString() {
        byte[] SignatureBytes = new byte[65];
        System.arraycopy(this.r, 0, SignatureBytes, 0, 32);
        System.arraycopy(this.s, 0, SignatureBytes, 32, 32);
        SignatureBytes[64] = this.v;
        return Hex.toHexString(SignatureBytes);
    }

    @Override
    public List<RlpType> encode() {
        List<RlpType> encodeResult = new ArrayList<>();
        int encodedV = this.v + VBASE;
        encodeResult.add(RlpString.create((byte) encodedV));
        super.encodeCommonField(encodeResult);
        return encodeResult;
    }

    public byte getV() {
        return v;
    }

    public void setV(byte v) {
        this.v = v;
    }
}
