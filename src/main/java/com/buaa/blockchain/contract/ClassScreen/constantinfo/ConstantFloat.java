package com.buaa.blockchain.contract.ClassScreen.constantinfo;

import com.buaa.blockchain.contract.ClassScreen.ConstantInfo;
import com.buaa.blockchain.contract.ClassScreen.basictype.U2;
import com.buaa.blockchain.contract.ClassScreen.basictype.U4;

import java.io.InputStream;

/**
 * Created by wangxiandeng on 2017/1/25.
 */
public class ConstantFloat extends ConstantInfo {
    public long value;

    @Override
    public void read(InputStream inputStream) {
        value = U4.read(inputStream);
    }
}
