package com.buaa.blockchain.message;


import org.jgroups.*;
import org.jgroups.conf.ConfiguratorFactory;
import org.jgroups.conf.ProtocolStackConfigurator;
import org.jgroups.stack.IpAddress;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;


/**
 * 消息系统的JGroups实现类。
 * JGroups中的Message类默认提供存储对象的空间，此处统一规定将内容打包为String类型；
 *
 * 由于是单线程运行的，在收到其他节点发来的消息时，会阻塞。所以调用本实现类并实现messageCallBack时也是串行的。
 *
 * @author hitty
 * */

public class JGroupsMessageImpl extends ReceiverAdapter implements MessageService {
    // 本地地址
    private String address;
    // JGroups通道
    private JChannel channel;
    // 回调接口，用于将网络层发生的事件传递给上层调用者
    private MessageCallBack messageCallBack = null;
    // 集群节点集合，TreeSet保证有序排列
    private Set<String> clusterAddressList = new TreeSet<>();
    static{
        System.setProperty("java.net.preferIPv6Stack", "false");
        System.setProperty("java.net.preferIPv4Stack", "true");
    }
    /**
     * 初始化
     * */
    public JGroupsMessageImpl(){
        //InputStream is = this.getClass().getClassLoader().getResourceAsStream("msg-tcp.xml");
        try {
            InputStream is = new BufferedInputStream(new FileInputStream("."+File.separator+"config"+File.separator+"jgroups-tcp.xml"));
            // 从msg-tcp.xml中获取配置参数
            ProtocolStackConfigurator config = ConfiguratorFactory.getStackConfigurator(is);
            // 配置channel
            this.channel = new JChannel(config);
            this.channel.setReceiver(this);
            this.channel.connect("BlockCluster");
            this.channel.getState(null, 10000);
            // 获取本节点地址
            this.address = channel.down(new Event(
                    Event.GET_PHYSICAL_ADDRESS, channel.getAddress())).toString();

        } catch (Exception e) {
            //log.error("JGroupsMessageImpl(): Cannot init. Shut down!");
            e.printStackTrace();
        }
        // 成功从配置文件中生成channel
        //log.warn("JGroupsMessageImpl(): init complete via jgroups-tcp.xml, address="+this.address);
    }

    /**
     * 使用set的方式填充messageCallBack
     * */
    public void setMessageCallBack(MessageCallBack messageCallBack){
        this.messageCallBack = messageCallBack;
    }

    /**
     * 重写ReceiverAdapter的receive方法，当收到消息时由JGroups负责在此处通知
     * */
    @Override
    public void receive(Message msg) {
        // TODO 日志

        // 调用messageCallBack处理
        if(null != this.messageCallBack){
            // 将和具体业务逻辑有关的部分传递给回调接口，Message类的解析仅仅在JGroups范围内
            String msgString = (String) msg.getObject();
            messageCallBack.OnMessageReceived(msgString);
        }
    }

    /**
     * 重写ReceiverAdapter的viewAccepted方法，此方法被调用时证明集群出现变化
     *
     * */
    @Override
    public void viewAccepted(View view) {
        synchronized (clusterAddressList){
            // 深拷贝clusterAddressList
            Set<String> pre = new HashSet<>();
            for(String ads : this.clusterAddressList){
                pre.add(ads);
            }
            // 重新填写
            clusterAddressList.clear();
            for(Address address : this.channel.getView().getMembers()){
                this.clusterAddressList.add(
                        this.channel.down(new Event(Event.GET_PHYSICAL_ADDRESS, address)).toString()
                );
            }
            //log.warn("Cluster Changed:pre="+pre.toString()+
            //        " *** now="+this.clusterAddressList.toString());
            // 回调
            if(null != this.messageCallBack){
                this.messageCallBack.OnClusterChanged(pre,this.clusterAddressList);
            }
        }


    }

    /**
     * 广播消息给所有集群中的节点
     * @param message
     * */
    @Override
    public void broadcasting(Object message) {
        try{
            Message msg = new Message(null,null,message);
            channel.send(msg);
        }catch (Exception e){
            // TODO 处理异常
        }

    }

    /**
     * 单点发送消息
     * @param message 发送的消息
     * @param address 目的地址
     * */
    @Override
    public void singleSend(Object message, String address) {
        try{
            Message msg = new Message(new IpAddress(address),channel.getAddress(),message);
            channel.send(msg);
        }catch (Exception e){
            // TODO 处理异常
        }
    }

    /**
     * 组播消息
     * @param message
     * @param addressList
     * */
    @Override
    public void MultiSend(Object message, Set<String> addressList) {

    }

    @Override
    public Set<String> getClusterAddressList() {
        return clusterAddressList;
    }

    @Override
    public void onClusterChange() {
        // TODO 此处功能暂时放到JGroups的viewAccept方法中

    }

    public void showInfo(){
        System.out.println(address);
    }

    /**
     * 本地地址
     * */
    @Override
    public String getLocalAddress() {
        return this.address;
    }
}

