package com.buaa.blockchain.contract.core;


import lombok.extern.slf4j.Slf4j;

/**
 * 存储单元类型，应用于智能合约及其运行环境中
 * 提供String,Integer,Boolean,Float,ByteArray 5种类型
 *
 * @author hitty
 * */

@Slf4j
public class DataUnit {
    public static final String INT = "INT";
    public static final String FLOAT = "FLOAT";
    public static final String STRING = "STRING";
    public static final String BOOL = "BOOL";
    public static final String BYTEARRAY = "BYTEARRAY";

    String type;
    String aString;
    Integer aInteger;
    Boolean aBoolean;
    Float aFloat;
    byte[] byteArray;

    public DataUnit(String type, String value){
        this.type = type;
        // 按照类型解码
        if(type.equals(INT)){
            this.aInteger = Integer.valueOf(value);
        }else if(type.equals(BOOL)){
            this.aBoolean = Boolean.valueOf(value);
        }else if(type.equals(FLOAT)){
            this.aFloat = Float.valueOf(value);
        }else if(type.equals(STRING)){
            this.aString = value;
        }
        addDefault();
    }

    public DataUnit(Integer i){
        this.aInteger = i;
        this.type = INT;
        addDefault();
    }
    public DataUnit(Boolean b){
        this.aBoolean = b;
        this.type = BOOL;
        addDefault();
    }
    public DataUnit(Float f){
        this.aFloat = f;
        this.type = FLOAT;
        addDefault();
    }
    public DataUnit(String value){
        this.type = STRING;
        this.aString = value;
        addDefault();
    }
    public DataUnit(byte[] array){
        this.type = BYTEARRAY;
        this.byteArray = array;
        addDefault();
    }
    public DataUnit(){
        addDefault();
    }

    void addDefault(){
        if(null == type){
            type = STRING;
        }
        if(null == aInteger){
            aInteger = 1;
        }
        if(null == aString){
            aString = "";
        }
        if(null == aBoolean){
            aBoolean = false;
        }
        if(null == aFloat){
            aFloat = 0.0f;
        }
        if(null == byteArray){
            byteArray = new byte[]{};
        }
    }

    public Integer getInteger(){
        return aInteger;
    }

    public String getString() {
        return aString;
    }

    public Boolean getBoolean() {
        return aBoolean;
    }

    public Float getFloat() {
        return aFloat;
    }

    public String getValue(){
        switch (type){
            case INT:{
                return aInteger.toString();
            }
            case FLOAT:{
                return aFloat.toString();
            }
            case BOOL:{
                return aBoolean.toString();
            }
            case STRING:{
                return aString;
            }
            case BYTEARRAY:{
                return new String(byteArray);
            }
            default: {
                log.warn("getValue(): no type in this DataUnit, return a 0 length string.");
                return "";
            }
        }
    }

    public byte[] getByteArray(){
        return  this.byteArray;
    }


}