package com.buaa.blockchain.contract.util.classreader.constantinfo;

import com.buaa.blockchain.contract.util.classreader.ConstantInfo;
import com.buaa.blockchain.contract.util.classreader.basictype.U1;
import com.buaa.blockchain.contract.util.classreader.basictype.U2;

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
