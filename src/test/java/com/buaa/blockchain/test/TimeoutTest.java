package com.buaa.blockchain.test;

import com.buaa.blockchain.utils.TimeoutCallBack;
import com.buaa.blockchain.utils.TimeoutHelper;

public class TimeoutTest {
    public static void main(String[] args) {
        TimeoutHelper timeoutHelper = new TimeoutHelper(5000, new TimeoutCallBack() {
            @Override
            public void callback() {
                System.out.println("is timeout, do something....");
            }
        });
        timeoutHelper.startWork();
    }
}
