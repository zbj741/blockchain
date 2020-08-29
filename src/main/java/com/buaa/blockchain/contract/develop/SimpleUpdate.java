package com.buaa.blockchain.contract.develop;

import com.buaa.blockchain.contract.State;
import com.buaa.blockchain.contract.core.Contract;
import com.buaa.blockchain.contract.core.DataUnit;


import java.util.Map;


/**
 * 智能合约样例
 * 简单的将传入的两个参数作为kv存入statedb
 *
 * @author hitty
 * */
public class SimpleUpdate implements Contract {

    String key;
    String value;

    public SimpleUpdate(){}

    @Override
    public String getName() {
        return "SimpleUpdate";
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
