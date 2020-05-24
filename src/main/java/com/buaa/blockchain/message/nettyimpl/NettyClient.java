package com.buaa.blockchain.message.nettyimpl;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

/**
 * netty实现client，用于管理主动连接的节点
 * client将所有的连接存储下来，并且使用这些连接来发送消息
 * 对于节点来说，client中保有的可用连接为其所知的集群信息
 *
 * @author hitty
 * */
public class NettyClient {
    NettyMessageImpl nl;
    HashMap<String, Channel> channelHashMap = new HashMap<>();
    ArrayList<String> addressList = new ArrayList<>();
    public NettyClient(NettyMessageImpl nl){
        initClient();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    // TODO 尝试重连
                }
            }
        }).start();
    }
    private void initClient(){
        //init Netty client
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
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
                            socketChannel.pipeline().addLast(new NettyMsgHandler(nl));
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
