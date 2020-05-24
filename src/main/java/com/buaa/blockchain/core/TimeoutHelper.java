package com.buaa.blockchain.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 超时等待器，用于检测主节点做块流程的超时
 * 每一个节点startNewRound时，非主节点注册一个超时器。
 * 正常情况下，收到区块并且storeBlock后，会开启下一轮；当长时间未收到区块，则自动进入下一轮
 *
 * @author hitty
 * */

@Component
public class TimeoutHelper {

    private long timeout = 20000l;

    private HashMap<String, CountDownLatch> timeoutList = new HashMap<>();

    @Autowired
    TimeoutHelper(){

    }
    public Boolean startWait(int height,int round){
        String key = height+"_"+round;
        CountDownLatch countDownLatch = new CountDownLatch(1);
        timeoutList.put(key,countDownLatch);
        try {
            countDownLatch.await(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException interruptedException){
            // TODO 处理异常
        }finally {
            timeoutList.remove(key);
            if(countDownLatch.getCount() > 0){
                // 超时结束
                return false;
            }else{
                // 被正常notified结束
                return true;
            }
        }
    }
    public void notified(int height,int round){
        String key = height+"_"+round;
        if(this.timeoutList.keySet().contains(key)){
            timeoutList.get(key).countDown();
        }
    }
}
