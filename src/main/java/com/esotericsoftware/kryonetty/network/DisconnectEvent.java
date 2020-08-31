package com.esotericsoftware.kryonetty.network;

import com.esotericsoftware.kryonetty.network.handler.NetworkEvent;
import io.netty.channel.ChannelHandlerContext;

public class DisconnectEvent implements NetworkEvent {

    private final ChannelHandlerContext ctx;

    public DisconnectEvent(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }
}
