package com.buaa.blockchain.contract.util.classreader.constantinfo;

import com.buaa.blockchain.contract.util.classreader.ConstantInfo;
import com.buaa.blockchain.contract.util.classreader.basictype.U2;

import java.io.InputStream;

/**
 * Created by wangxiandeng on 2017/1/25.
 */
public class ConstantMethodType extends ConstantInfo {
    int descType;

    @Override
    public void read(InputStream inputStream) {
        descType= U2.read(inputStream);
    }
}
