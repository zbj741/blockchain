package com.buaa.blockchain.contract;

import com.buaa.blockchain.contract.component.IContract;

/**
 * xxxx
 *
 * @author <a href="http://github.com/hackdapp">hackdapp</a>
 * @date 2021/1/6
 * @since JDK1.8
 */
public class TestConctract extends IContract {

    public int add(int a, int b){
        final int c = a + b;
        LOG("a,b,"+c);
        return c;
    }
}
