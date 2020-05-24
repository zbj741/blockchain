package com.buaa.blockchain.message.nettyimpl;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class NettyMsgHandler extends ChannelInboundHandlerAdapter {
    NettyMessageImpl nl;
    public NettyMsgHandler(NettyMessageImpl nl){
        this.nl = nl;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

    }

}
