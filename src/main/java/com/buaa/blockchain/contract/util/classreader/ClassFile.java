package com.buaa.blockchain.contract.util.classreader;

import com.buaa.blockchain.contract.util.classreader.basicinfo.MemberInfo;

/**
 * Created by wanginbeijing on 2017/1/24.
 */
public class ClassFile {
    public long magic;
    public int minorVersion;
    public int majorVersion;
    public ConstantPool constantPool;
     //??pool count
    public int accessFlag;
    public String className;
    public String superClass;
    public int interfaceCount;
    public String[] interfaces;
    public int fieldCount;
    public MemberInfo[] fields;
    public int methodCount;
    public MemberInfo[] methods;
}
