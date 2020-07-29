package com.buaa.blockchain.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.concurrent.*;

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
    private CountDownLatch countDownLatch = null;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private Boolean isBusy = false;
    private int height;
    private int round;
    boolean res = true;
    @Autowired
    TimeoutHelper(){

    }

    /**
     * 开始等待
     * */
    public synchronized void startWait(int height,int round){
        if(!isBusy){
            this.height = height;
            this.round = round;
            this.res = true;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    countDownLatch = new CountDownLatch(1);
                    try{
                        // 开始等待超时
                        res = countDownLatch.await(timeout,TimeUnit.MILLISECONDS);
                    }catch (InterruptedException e){

                    }finally {
                        if(!res){
                            // 是超时引起的

                        }
                    }
                }
            });
        }
    }

    /**
     * 解除等待并且定时器删除
     * */
    public void notified(int height,int round){
        if(this.height == height && this.round == round && isBusy){
            countDownLatch.countDown();
            this.height = 0;
            this.round = 0;
            this.isBusy = false;
        }
    }
}
