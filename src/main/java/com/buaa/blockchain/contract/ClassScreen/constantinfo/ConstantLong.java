package com.buaa.blockchain.contract.ClassScreen.constantinfo;

import com.buaa.blockchain.contract.ClassScreen.ConstantInfo;
import com.buaa.blockchain.contract.ClassScreen.basictype.U4;

import java.io.InputStream;

/**
 * Created by wangxiandeng on 2017/1/25.
 */
public class ConstantLong extends ConstantInfo {
    public long highValue;
    public long lowValue;

    @Override
    public void read(InputStream inputStream) {
        highValue = U4.read(inputStream);
        lowValue = U4.read(inputStream);
    }
}
