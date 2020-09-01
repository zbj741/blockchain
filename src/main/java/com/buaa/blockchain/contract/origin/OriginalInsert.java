package com.buaa.blockchain.contract.origin;

import com.buaa.blockchain.contract.State;
import com.buaa.blockchain.contract.core.Contract;
import com.buaa.blockchain.contract.core.DataUnit;

import java.util.Map;

/**
 * 原生的Insert智能合约
 * 用于将一对K-V以String的形式插入StateDB
 *
 * @author hitty
 * */
public class OriginalInsert implements Contract {

    String key;
    String value;

    @Override
    public String getName() {
        return "OriginalInsert";
    }

    @Override
    public String getIntro() {
        return null;
    }

    @Override
    public int initParam(Map<String, DataUnit> args) {
        key = args.get("KEY").value.toString();
        value = args.get("VALUE").value.toString();
        return 0;
    }

    @Override
    public void run(State state) {
        state.update(key,value);
    }
}
