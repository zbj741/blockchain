package com.buaa.blockchain.entity;


import com.buaa.blockchain.contract.core.DataUnit;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 用于转换为调用智能合约的字段，放入Transaction的Data
 * */
@Data
public class ContractCaller {
    String contractName;
    Map<String, DataUnit> arg;
    public ContractCaller(){
        contractName = "";
        arg = new HashMap<>();
    }
    public ContractCaller(String contractName,Map<String,DataUnit> arg){
        this.contractName = contractName;
        this.arg = arg;
    }

}
