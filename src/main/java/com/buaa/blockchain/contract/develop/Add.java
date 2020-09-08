package com.buaa.blockchain.contract.develop;

import com.buaa.blockchain.contract.State;
import com.buaa.blockchain.contract.core.Contract;
import com.buaa.blockchain.contract.core.DataUnit;


import java.util.Map;


/**
 * 智能合约样例-Add
 *
 *
 * @author hitty
 * */
public class Add implements Contract {
    // 存入state的key
    String key;
    // 被加数1
    String value1;
    // 被加数2
    String value2;

    public Add(){}

    @Override
    public String getName() {
        return "Add";
    }

    @Override
    public String getIntro() {
        return null;
    }

    @Override
    public int initParam(Map<String, DataUnit> args) {
        key = args.get("KEY").value.toString();
        value1 = args.get("VALUE_1").value.toString();
        value2 = args.get("VALUE_2").value.toString();
        return 0;
    }

    @Override
    public void run(State state) {
        int v1 = Integer.valueOf(value1);
        int v2 = Integer.valueOf(value2);
        String res = (v1+v2)+"";
        state.update(key,res);
    }
}
