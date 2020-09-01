package com.buaa.blockchain.contract.core;



import com.buaa.blockchain.contract.State;
import com.buaa.blockchain.contract.core.DataUnit;

import java.util.Map;

/**
 * 智能合约接口
 * 用户编写的智能合约需要实现这个接口，完成参数写入和生成原子合约
 *
 * @author hitty
 * */
public interface Contract {
    /**
     * 返回智能合约名，用作uuid
     * */
    String getName();
    /**
     * 返回智能合约信息，Json字符串形式给出
     * */
    String getIntro();
    /**
     * 初始化参数
     * @param args 参数，以<标识名,值>的形式放入Map中
     * */
    int initParam(Map<String, DataUnit> args);

    /**
     * 直接执行，跳过vm执行环境直接完成对state的操作，不可控
     * */
    void run(State state);
}
