package com.buaa.blockchain.message.nettyimpl;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * netty中pipeline的最后一个环节，用于将JsonMessage解码等工作
 *
 * @author hitty
 * */
public class NettyClientHandler extends ChannelInboundHandlerAdapter {
    NettyMessageImpl nl;
    NettyClient nettyClient;
    public NettyClientHandler(NettyMessageImpl nl,NettyClient nettyClient){
        this.nl = nl;
        this.nettyClient = nettyClient;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        nettyClient.removeChannel(ctx.channel());
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

    }

}
