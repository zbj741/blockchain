package com.buaa.blockchain.contract.develop;

import com.buaa.blockchain.contract.State;
import com.buaa.blockchain.contract.core.Contract;
import com.buaa.blockchain.contract.core.DataUnit;

import java.util.Map;

public class ChangeBalance implements Contract {

    String u1Name;
    String u2Name;

    public ChangeBalance(){}

    @Override
    public String getName() {
        return "ChangeBalance";
    }

    @Override
    public String getIntro() {
        return "contractName: ChangeBalance;" +
                "usage: change the balance of two user account, have to provide their names;" +
                "args: String u1Name, String u2Name";
    }


    @Override
    public int initParam(Map<String, DataUnit> args) {
        u1Name = args.get("U1NAME").getString();
        u2Name = args.get("U2NAME").getString();
        return 0;
    }

    @Override
    public void run(State state) {
        int u1b = state.getUserAccountBalance(u1Name);
        int u2b = state.getUserAccountBalance(u2Name);
        state.updateUserAccountBalance(u1Name,u2b);
        state.updateUserAccountBalance(u2Name,u1b);
    }


}
