package com.esotericsoftware.kryonetty.network;

import com.esotericsoftware.kryonetty.network.handler.NetworkEvent;
import io.netty.channel.ChannelHandlerContext;

public class ReceiveEvent implements NetworkEvent {

    private final ChannelHandlerContext ctx;
    private final Object object;

    public ReceiveEvent(ChannelHandlerContext ctx, Object object) {
        this.ctx = ctx;
        this.object = object;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public Object getObject() {
        return object;
    }
}
