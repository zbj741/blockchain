package com.buaa.blockchain.message;

import java.util.Set;

/**
 * 在MessageService的实现类中，对于触发特定事件，留出接口自定义处理逻辑
 *
 *
 * @author hitty
 *
 * */
public interface MessageCallBack {
    /**
     * 收到消息
     * @param content 消息内容，Json字符串，具体的解析放在业务逻辑里面执行
     * */
    void onMessageReceived(Object content);
    /**
     * 集群变化
     * @param pre 变化之前的集群地址表
     * @param now 变化之后的集群地址表
     *  */
    void onClusterChanged(Set<String> pre,Set<String> now);
}
