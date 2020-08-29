package com.buaa.blockchain.contract.core;

import com.buaa.blockchain.contract.Constant;

/**
 * 存储单元类型，应用于智能合约及其运行环境中
 * 提供String,Integer,Boolean,Float四种类型
 *
 * @author hitty
 * */
public class DataUnit {
    public String type;
    public Object value;

    public DataUnit(String type, String value){
        this.type = type;
        // 按照类型解码
        if(type.equals(Constant.INT)){
            this.value = Integer.valueOf(value);
        }else if(type.equals(Constant.BOOL)){
            this.value = Boolean.valueOf(value);
        }else if(type.equals(Constant.FLOAT)){
            this.value = Float.valueOf(value);
        }else{
            this.value = value;
        }

    }
    public DataUnit(Integer i){
        this.value = i;
        this.type = Constant.INT;
    }
    public DataUnit(Boolean b){
        this.value = b;
        this.type = Constant.BOOL;
    }
    public DataUnit(Float f){
        this.value = f;
        this.type = Constant.FLOAT;
    }
    public DataUnit(String value){
        this.type = Constant.STRING;
        this.value = value;
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