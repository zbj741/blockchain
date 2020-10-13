package com.buaa.blockchain.utils;

/**
 * 超时后执行某个方法的工具
 * */
public class TimeoutHelper{
    // 超时时间 以毫秒为单位
    private int timeout;
    // 用于消耗时间的线程
    TimeoutCallBack timeoutCallBack;
    // 执行的线程
    private Thread workThread;
    // 是否取消
    private boolean isCancel;
    public TimeoutHelper(int timeout,TimeoutCallBack timeoutCallBack){
        this.isCancel = false;
        this.timeout = timeout;
        this.timeoutCallBack = timeoutCallBack;
    }
    public void cancel(){
        this.isCancel = true;
    }
    public void startWork(){
        this.workThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 睡眠
                    Thread.sleep(timeout);
                    // 执行回调
                    if(!isCancel){
                        timeoutCallBack.callback();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        workThread.start();
    }

}

