package com.buaa.blockchain.contract.ClassScreen.constantinfo;

import com.buaa.blockchain.contract.ClassScreen.ConstantInfo;
import com.buaa.blockchain.contract.ClassScreen.basictype.U4;

import java.io.InputStream;

/**
 * Created by wanginbeijing on 2017/1/24.
 */
public class ConstantInteger extends ConstantInfo {
    public long value;

    @Override
    public void read(InputStream inputStream) {
        value = U4.read(inputStream);
    }
}
