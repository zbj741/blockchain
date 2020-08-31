package com.buaa.blockchain.contract.ClassScreen.constantinfo;

import com.buaa.blockchain.contract.ClassScreen.ConstantInfo;
import com.buaa.blockchain.contract.ClassScreen.basictype.U2;

import java.io.InputStream;

/**
 * Created by wangxiandeng on 2017/1/25.
 */
public class ConstantInvokeDynamic extends ConstantInfo {
    public int bootstrapMethodAttrIndex;
    public int nameAndTypeIndex;

    @Override
    public void read(InputStream inputStream) {
        bootstrapMethodAttrIndex = U2.read(inputStream);
        nameAndTypeIndex = U2.read(inputStream);
    }
}
