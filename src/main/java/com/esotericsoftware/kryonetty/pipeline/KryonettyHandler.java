package com.esotericsoftware.kryonetty.pipeline;

import com.esotericsoftware.kryonetty.kryo.Endpoint;
import com.esotericsoftware.kryonetty.network.ConnectEvent;
import com.esotericsoftware.kryonetty.network.DisconnectEvent;
import com.esotericsoftware.kryonetty.network.ReceiveEvent;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KryonettyHandler extends ChannelInboundHandlerAdapter {

    final Endpoint endpoint;
    final ExecutorService executorService;

    public KryonettyHandler(Endpoint endpoint) {
        this.endpoint = endpoint;
        this.executorService = Executors.newCachedThreadPool();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        endpoint.eventHandler().callEvent(new ConnectEvent(ctx));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        endpoint.eventHandler().callEvent(new DisconnectEvent(ctx));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
        endpoint.eventHandler().callEvent(new ReceiveEvent(ctx, msg));
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}