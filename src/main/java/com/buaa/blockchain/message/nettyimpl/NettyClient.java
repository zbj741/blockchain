package com.buaa.blockchain.message.nettyimpl;

import com.buaa.blockchain.utils.Utils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * netty实现client，用于管理主动连接的节点
 * client将所有的连接存储下来，并且使用这些连接来发送消息
 * 对于节点来说，client中保有的可用连接为其所知的集群信息
 *
 * @author hitty
 * */
public class NettyClient {
    NettyMessageImpl nl;
    // bootstrap管理连接发起
    private Bootstrap bootstrap;
    // 记录集群节点的地址和netty的channel的映射关系，要求这些channel是可用的
    ConcurrentHashMap<String, Channel> channelMap = new ConcurrentHashMap<>();
    public NettyClient(NettyMessageImpl nl){
        this.nl = nl;
        initClient();
        // 开启一个线程，尝试重连所有在配置文件中出现的地址【线程中不能出现阻塞】
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        Thread.sleep(2000);
                        for(String s : nl.addressList){
                            addChannel(s);
                        }
                        for(Map.Entry<String,Channel> entry : channelMap.entrySet()){
                            if(!entry.getValue().isActive()){
                                removeChannel(entry.getKey());
                            }
                        }
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                }
            }
        }).start();
    }
    private void initClient(){
        //init Netty client
        NettyClient ts = this;
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            bootstrap = new Bootstrap();
            bootstrap.group(group).
                    channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,1000)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel socketChannel) {
                            socketChannel.pipeline().addLast(new ProtobufVarint32FrameDecoder());
                            socketChannel.pipeline().
                                    addLast(new ProtobufDecoder(
                                            JsonMessageProto.JsonMessage.getDefaultInstance()));
                            socketChannel.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
                            socketChannel.pipeline().addLast(new ProtobufEncoder());
                            socketChannel.pipeline().addLast(new NettyClientHandler(nl,ts));
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加channel
     * */
    private void addChannel(String address){
        if(channelMap.keySet().contains(address)){
            // 避免重新连接
            return ;
        }
        try{
            bootstrap.connect(Utils.String2InetSocketAddress(address)).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) {
                    try{
                        if(channelFuture.channel().isActive()){
                            // 通知集群变动
                            synchronized (this){
                                Set<String> pre = new TreeSet<>();
                                for(String s : channelMap.keySet()){
                                    pre.add(s);
                                }
                                channelMap.put(address,channelFuture.channel());
                                Set<String> now = new TreeSet<>();
                                for(String s : channelMap.keySet()){
                                    now.add(s);
                                }
                                nl.clusterAddressList = now;
                                if(null != nl.messageCallBack){
                                    nl.messageCallBack.OnClusterChanged(pre,now);
                                }
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 删除channel
     * */
    public void removeChannel(String address){
        if(channelMap.keySet().contains(address)){
            channelMap.get(address).close();
            synchronized (this){
                // 通知集群变动
                Set<String> pre = new TreeSet<>();
                for(String s : channelMap.keySet()){
                    pre.add(s);
                }
                channelMap.remove(address);
                Set<String> now = new TreeSet<>();
                for(String s : channelMap.keySet()){
                    now.add(s);
                }
                nl.clusterAddressList = now;
                if(null != nl.messageCallBack){
                    nl.messageCallBack.OnClusterChanged(pre,now);
                }
            }
        }

    }

    /**
     * 广播
     * */
    public void broadcast(Object msg){
        for(Channel channel : channelMap.values()){
            channel.writeAndFlush(msg);
        }
    }

    /**
     * 单播
     * */
    public void singleSend(Object message, String address) {
        if(channelMap.keySet().contains(address)){
            channelMap.get(address).writeAndFlush(message);
        }
    }

    /**
     * 多播
     * */
    public void multiSend(Object message, Set<String> addressList) {
        for(String address : addressList){
            singleSend(message,address);
        }
    }

}
