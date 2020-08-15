package com.buaa.blockchain.message.nettyimpl;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

/**
 * netty实现server端，用来被其他的节点的client端来连接
 * server端收取其他节点的消息
 * server端不记录自身被连接的信息，其自身所感知的集群网络状况由NettyClient管理
 *
 * @author hitty
 * */
public class NettyServer {
    NettyMessageImpl nl;
    int port;
    String ip;
    NettyServer(NettyMessageImpl nl, String ip, int port){
        NettyServer ts = this;
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try{
            ServerBootstrap ssmpServerBootstrap = new ServerBootstrap();
            ssmpServerBootstrap.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    //.handler(null)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel socketChannel){
                            socketChannel.pipeline().addLast(new ProtobufVarint32FrameDecoder());
                            socketChannel.pipeline().
                                    addLast(new ProtobufDecoder(
                                            JsonMessageProto.JsonMessage.getDefaultInstance()));
                            socketChannel.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
                            socketChannel.pipeline().addLast(new ProtobufEncoder());
                            socketChannel.pipeline().addLast(new NettyServerHandler(nl,ts));


                        }
                    });
            // ChannelFuture channelFuture = ssmpServerBootstrap.bind(ip,port).sync();
            ChannelFuture channelFuture = ssmpServerBootstrap.bind(port).sync();
            channelFuture.channel().closeFuture().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    // TODO 关闭前的处理

                    bossGroup.shutdownGracefully();
                    workerGroup.shutdownGracefully();
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
    }
}
