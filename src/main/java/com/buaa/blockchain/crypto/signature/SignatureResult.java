package com.buaa.blockchain.crypto.signature;


import com.buaa.blockchain.crypto.exceptions.SignatureException;
import com.buaa.blockchain.crypto.rlp.RlpString;
import com.buaa.blockchain.crypto.rlp.RlpType;
import com.buaa.blockchain.crypto.utils.ByteUtils;
import com.buaa.blockchain.crypto.utils.Hex;

import java.util.List;

public abstract class SignatureResult {
    protected byte[] r;
    protected byte[] s;
    protected byte[] signatureBytes;

    SignatureResult(final byte[] r, final byte[] s) {
        this.r = r;
        this.s = s;
    }

    /**
     * Recover v, r, s from signature string The first 32 bytes are r, and the 32 bytes after r are
     * s
     *
     * @param signatureString the signatureString
     */
    SignatureResult(final String signatureString) {
        this.signatureBytes = Hex.decode(signatureString);
        // at least 64 bytes
        if (this.signatureBytes.length < 64) {
            throw new SignatureException(
                    "Invalid signature: "
                            + signatureString
                            + ", signatureString len: "
                            + signatureString.length()
                            + ", signatureBytes size:"
                            + signatureBytes.length);
        }
        // get R
        this.r = new byte[32];
        System.arraycopy(this.signatureBytes, 0, this.r, 0, 32);
        // get S
        this.s = new byte[32];
        System.arraycopy(this.signatureBytes, 32, this.s, 0, 32);
    }

    public byte[] getR() {
        return r;
    }

    public byte[] getS() {
        return s;
    }

    public byte[] getSignatureBytes() {
        return signatureBytes;
    }

    public void setR(byte[] r) {
        this.r = r;
    }

    public void setS(byte[] s) {
        this.s = s;
    }

    public void setSignatureBytes(byte[] signatureBytes) {
        this.signatureBytes = signatureBytes;
    }

    protected void encodeCommonField(List<RlpType> encodeResult) {
        encodeResult.add(RlpString.create(ByteUtils.trimLeadingZeroes(this.getR())));
        encodeResult.add(RlpString.create(ByteUtils.trimLeadingZeroes(this.getS())));
    }

    /**
     * covert signatureResult into String
     *
     * @return signatureResult in string form can be used as a verify parameter
     */
    public abstract String convertToString();

    /**
     * encode the signatureResult into rlp-list
     *
     * @return the encoded rlp-list with r, s, v( or pub)
     */
    public abstract List<RlpType> encode();
}
