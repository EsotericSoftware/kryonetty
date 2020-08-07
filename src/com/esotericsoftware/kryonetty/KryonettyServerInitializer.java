package com.esotericsoftware.kryonetty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class KryonettyServerInitializer extends ChannelInitializer<SocketChannel> {

    private final Endpoint endpoint;
    private final KryonettyServerHandler serverHandler;

    public KryonettyServerInitializer(Endpoint endpoint) {
        this.endpoint = endpoint;
        this.serverHandler = new KryonettyServerHandler(endpoint);
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        // Add the text line codec combination first,
        pipeline.addLast(new LoggingHandler(LogLevel.INFO));
        pipeline.addLast(new KryonettyDecoder(endpoint));
        pipeline.addLast(new KryonettyEncoder(endpoint));

        // and then business logic.
        pipeline.addLast(new KryonettyServerHandler(endpoint));
    }
}