package com.buaa.blockchain.contract.core;

/**
 * 存储单元类型，应用于智能合约及其运行环境中
 * 提供String,Integer,Boolean,Float,ByteArray 5种类型
 *
 * @author hitty
 * */
public class DataUnit {
    public static final String INT = "INT";
    public static final String FLOAT = "FLOAT";
    public static final String STRING = "STRING";
    public static final String BOOL = "BOOL";
    public static final String BYTEARRAY = "BYTEARRAY";

    public String type;
    public Object value;

    public DataUnit(String type, String value){
        this.type = type;
        // 按照类型解码
        if(type.equals(INT)){
            this.value = Integer.valueOf(value);
        }else if(type.equals(BOOL)){
            this.value = Boolean.valueOf(value);
        }else if(type.equals(FLOAT)){
            this.value = Float.valueOf(value);
        }else if(type.equals(STRING)){
            this.value = value;
        }
    }

    public DataUnit(Integer i){
        this.value = i;
        this.type = INT;
    }
    public DataUnit(Boolean b){
        this.value = b;
        this.type = BOOL;
    }
    public DataUnit(Float f){
        this.value = f;
        this.type = FLOAT;
    }
    public DataUnit(String value){
        this.type = STRING;
        this.value = value;
    }
    public DataUnit(byte[] array){
        this.type = BYTEARRAY;
        this.value = array;
    }
    public DataUnit(){}

    public boolean equals(DataUnit dataUnit){
        if(this.type.equals(dataUnit.type)){
            return this.value.equals(dataUnit.value);
        }else{
            return false;
        }
    }

}