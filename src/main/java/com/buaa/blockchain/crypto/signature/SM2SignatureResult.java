package com.buaa.blockchain.crypto.signature;


import com.buaa.blockchain.crypto.rlp.RlpString;
import com.buaa.blockchain.crypto.rlp.RlpType;
import com.buaa.blockchain.crypto.utils.Hex;

import java.util.ArrayList;
import java.util.List;

public class SM2SignatureResult extends SignatureResult {
    protected byte[] pub;

    public SM2SignatureResult(final String hexPublicKey, final String signatureString) {
        super(signatureString);
        this.pub = Hex.decode(hexPublicKey.substring(2));
    }

    public SM2SignatureResult(byte[] pub, byte[] r, byte[] s) {
        super(r, s);
        this.pub = pub;
    }

    /**
     * covert signatureResult into String
     *
     * @return the signature string with [r, s]
     */
    @Override
    public String convertToString() {
        byte[] SignatureBytes = new byte[64];
        System.arraycopy(this.r, 0, SignatureBytes, 0, 32);
        System.arraycopy(this.s, 0, SignatureBytes, 32, 32);
        return Hex.toHexString(SignatureBytes);
    }

    @Override
    public List<RlpType> encode() {
        List<RlpType> encodeResult = new ArrayList<>();
        encodeResult.add(RlpString.create(this.pub));
        super.encodeCommonField(encodeResult);
        return encodeResult;
    }

    public byte[] getPub() {
        return pub;
    }

    public void setPub(byte[] pub) {
        this.pub = pub;
    }
}
