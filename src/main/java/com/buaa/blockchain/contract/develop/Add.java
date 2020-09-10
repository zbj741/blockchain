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
    Integer value1;
    // 被加数2
    Integer value2;

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
        key = args.get("KEY").getString();
        value1 = args.get("VALUE_1").getInteger();
        value2 = args.get("VALUE_2").getInteger();
        return 0;
    }

    @Override
    public void run(State state) {
        String res = (value1+value2)+"";
        state.update(key,res);
    }
}
