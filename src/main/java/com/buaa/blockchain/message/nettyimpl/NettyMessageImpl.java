package com.buaa.blockchain.message.nettyimpl;

import com.buaa.blockchain.message.MessageCallBack;
import com.buaa.blockchain.message.MessageService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

@Slf4j

public class NettyMessageImpl implements MessageService {
    // 本地地址（外网）
    public String ipv4;
    public int port;
    // 外网节点地址列表
    public String ipv4List;
    private NettyServer server;
    private NettyClient client;
    // 本地地址
    private String address;
    // 回调接口，用于将网络层发生的事件传递给上层调用者
    public MessageCallBack messageCallBack = null;
    // 在配置文件中的限定的节点地址列表
    public String[] addressList;
    // 集群节点集合，TreeSet保证有序排列
    protected Set<String> clusterAddressList = new TreeSet<>();

    public NettyMessageImpl(String ipv4,int port,String rawList){
        this.ipv4 = ipv4;
        this.port = port;
        this.ipv4List = rawList;
        this.address = ipv4+":"+port;
        if(null == ipv4List || ipv4List.length() < 10){
            this.addressList = new String[1];
            this.addressList[0] = this.address;
        }else{
            this.addressList = ipv4List.split(",");
        }
        this.server = new NettyServer(this,ipv4,port);
        this.client = new NettyClient(this);
    }


    @Override
    public void broadcasting(Object message) {
        String jsonStr = (String) message;
        // 打包成protobuf形式
        JsonMessageProto.JsonMessage.Builder builder = JsonMessageProto.JsonMessage.newBuilder();
        builder.setMsgType(0);
        builder.setContent(jsonStr);
        JsonMessageProto.JsonMessage msg = builder.build();
        client.broadcast(msg);
    }

    @Override
    public void singleSend(Object message, String address) {
        String jsonStr = (String) message;
        // 打包成protobuf形式
        JsonMessageProto.JsonMessage.Builder builder = JsonMessageProto.JsonMessage.newBuilder();
        builder.setMsgType(0);
        builder.setContent(jsonStr);
        JsonMessageProto.JsonMessage msg = builder.build();
        client.singleSend(msg,address);
    }

    @Override
    public void multiSend(Object message, Set<String> addressList) {
        String jsonStr = (String) message;
        // 打包成protobuf形式
        JsonMessageProto.JsonMessage.Builder builder = JsonMessageProto.JsonMessage.newBuilder();
        builder.setMsgType(0);
        builder.setContent(jsonStr);
        JsonMessageProto.JsonMessage msg = builder.build();
        client.multiSend(msg,addressList);
    }

    @Override
    public String getLocalAddress() {
        return address;
    }

    @Override
    public Set<String> getClusterAddressList() {
        return clusterAddressList;
    }

    @Override
    public void setMessageCallBack(MessageCallBack messageCallBack) {
        this.messageCallBack = messageCallBack;
    }

    @Override
    public void onClusterChange() {

    }



}
