package com.buaa.blockchain.contract.ClassScreen.constantinfo;

import com.buaa.blockchain.contract.ClassScreen.ConstantInfo;
import com.buaa.blockchain.contract.ClassScreen.basictype.U1;
import com.buaa.blockchain.contract.ClassScreen.basictype.U2;

import java.io.InputStream;

/**
 * Created by wangxiandeng on 2017/1/25.
 */
public class ConstantMethodHandle extends ConstantInfo {
    public short referenceKind;
    public int referenceIndex;

    @Override
    public void read(InputStream inputStream) {
        referenceKind = U1.read(inputStream);
        referenceIndex = U2.read(inputStream);
    }
}
