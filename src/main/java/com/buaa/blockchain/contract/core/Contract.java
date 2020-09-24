package com.buaa.blockchain.contract.core;

import com.buaa.blockchain.contract.State;

import java.util.Map;
import java.util.List;

/**
 * 智能合约接口
 * 用户编写的智能合约需要实现这个接口
 *
 * @author hitty
 * */
public interface Contract {
    /**
     * 返回智能合约名
     * */
    String getName();
    /**
     * 返回智能合约信息
     * */
    String getIntro();
    /**
     * 初始化参数
     * @param args 参数，以<标识名,值>的形式放入Map中
     * */
    int initParam(Map<String, DataUnit> args);

    /**
     * 执行
     * */
    void run(State state);

}
