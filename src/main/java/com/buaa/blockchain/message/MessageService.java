package com.buaa.blockchain.message;

import java.util.Set;

/**
 * 消息服务的接口
 * 消息服务只负责消息的收发、网络状态的检查；消息服务将收到的数据通知回调函数，本身不关心消息怎么处理
 * 消息服务的实现类会持有BlockchainService的引用，从而执行其中的MessageCallBack
 *
 * @author hitty
 */
public interface MessageService {
    /**
     * 广播消息给集群中所有的节点
     * */
    void broadcasting(String message);
    /**
     * 单点发送
     * */
    void singleSend(String message,String address);
    /**
     * 多点发送
     * */
    void multiSend(String message, Set<String> addressList);
    /**
     * 本地地址
     * */
    String getLocalAddress();
    /**
     * 获取已连接的集群节点地址
     * */
    Set<String> getClusterAddressList();
    /**
     * 注册定义核心功能回调函数
     * */
    void setMessageCallBack(MessageCallBack messageCallBack);
    /**
     * 集群网络变动
     * */
    void onClusterChange();

    String JGROUPS = "JGROUPS";
    String NETTY = "NETTY";
}
