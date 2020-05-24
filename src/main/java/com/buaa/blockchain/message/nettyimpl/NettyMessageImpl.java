package com.buaa.blockchain.message.nettyimpl;

import com.buaa.blockchain.message.MessageCallBack;
import com.buaa.blockchain.message.MessageService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.util.Set;
import java.util.TreeSet;

@Slf4j
public class NettyMessageImpl implements MessageService {

    @Value("${buaa.blockchain.msg.ipv4}")
    String ipv4;
    @Value("${buaa.blockchain.msg.port}")
    int port;
    @Value("${buaa.blockchain.msg.ipv4.list}")
    String ipv4List;

    private NettyServer server;
    private NettyClient client;
    // 本地地址
    private String address;
    // 回调接口，用于将网络层发生的事件传递给上层调用者
    private MessageCallBack messageCallBack = null;
    // 集群节点集合，TreeSet保证有序排列
    private Set<String> clusterAddressList = new TreeSet<>();

    public NettyMessageImpl(){
        this.address = ipv4+":"+port;
        this.server = new NettyServer(this,ipv4,port);

    }




    @Override
    public void broadcasting(Object message) {

    }

    @Override
    public void singleSend(Object message, String address) {

    }

    @Override
    public void MultiSend(Object message, Set<String> addressList) {

    }

    @Override
    public String getLocalAddress() {
        return address;
    }

    @Override
    public Set<String> getClusterAddressList() {
        return null;
    }

    @Override
    public void setMessageCallBack(MessageCallBack messageCallBack) {
        this.messageCallBack = messageCallBack;
    }

    @Override
    public void onClusterChange() {

    }


}
