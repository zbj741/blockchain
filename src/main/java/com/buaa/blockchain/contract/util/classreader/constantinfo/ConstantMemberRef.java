package com.buaa.blockchain.contract.util.classreader.constantinfo;

import com.buaa.blockchain.contract.util.classreader.ConstantInfo;
import com.buaa.blockchain.contract.util.classreader.basictype.U2;

import java.io.InputStream;

/**
 * Created by wangxiandeng on 2017/1/25.
 */
public class ConstantMemberRef extends ConstantInfo{
    public int classIndex;
    public int nameAndTypeIndex;


    @Override
    public void read(InputStream inputStream) {
        classIndex= U2.read(inputStream);
        nameAndTypeIndex=U2.read(inputStream);
    }
}
