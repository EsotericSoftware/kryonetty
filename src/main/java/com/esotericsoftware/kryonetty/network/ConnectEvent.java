package com.esotericsoftware.kryonetty.network;

import com.esotericsoftware.kryonetty.network.handler.NetworkEvent;
import io.netty.channel.ChannelHandlerContext;

public class ConnectEvent implements NetworkEvent {

    private final ChannelHandlerContext ctx;

    public ConnectEvent(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }
}
