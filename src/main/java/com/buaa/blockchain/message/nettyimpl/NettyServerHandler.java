package com.buaa.blockchain.message.nettyimpl;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * netty中pipeline的最后一个环节，用于将JsonMessage解码等工作
 *
 * @author hitty
 * */
public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    NettyMessageImpl nl;
    NettyServer nettyServer;
    public NettyServerHandler(NettyMessageImpl nl, NettyServer nettyServer){
        this.nl = nl;
        this.nettyServer = nettyServer;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 将接受到的msg转换为entity中的Message
        JsonMessageProto.JsonMessage jmsg = (JsonMessageProto.JsonMessage) msg;
        int type = jmsg.getMsgType();
        String content = jmsg.getContent();
        nl.messageCallBack.onMessageReceived(content);
    }

}
