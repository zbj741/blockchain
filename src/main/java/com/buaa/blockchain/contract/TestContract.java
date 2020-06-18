package com.buaa.blockchain.contract;

import com.buaa.blockchain.core.WorldState;

public class TestContract implements BaseContract<WorldState>{
    @Override
    public void update(WorldState worldState, String key, String value) {

    }

    @Override
    public String get(WorldState worldState, String key) {
        return null;
    }

    @Override
    public void delete(WorldState worldState, String key) {

    }
}
