package com.esotericsoftware.kryonetty.kryo;

import io.netty.channel.ChannelHandlerContext;

public interface EndpointReceiver {

    void connected(ChannelHandlerContext ctx);

    void disconnected(ChannelHandlerContext ctx);

    void received(ChannelHandlerContext ctx, Object object);

    Endpoint.Type type();

    EndpointOptions endpointOptions();

    KryoOptions kryoOptions();

    KryoHolder kryoHolder();

    enum Type {CLIENT, SERVER}
}
