package com.buaa.blockchain.contract.util.classreader.basicinfo;

import com.buaa.blockchain.contract.util.classreader.ConstantPool;

import java.io.InputStream;

/**
 * Created by wangxiandeng on 2017/1/25.
 */
public abstract class BasicInfo {
    protected ConstantPool mCp;

    public BasicInfo(ConstantPool cp) {
        mCp = cp;
    }

    public abstract void read(InputStream inputStream);
}
