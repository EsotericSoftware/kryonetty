package com.esotericsoftware.kryonetty.netty;

import com.esotericsoftware.kryonetty.kryo.Endpoint;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

public class KryonettyInitializer extends ChannelInitializer<SocketChannel> {

    private final Endpoint endpoint;
    private final SimpleChannelInboundHandler<Object> channelHandler;
    private final EventExecutorGroup executorGroup;

    public KryonettyInitializer(Endpoint endpoint, SimpleChannelInboundHandler<Object> channelHandler) {
        this.endpoint = endpoint;
        this.channelHandler = channelHandler;
        this.executorGroup = new DefaultEventExecutorGroup(endpoint.endpointOptions().getExecutionThreadSize());
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        if(endpoint.endpointOptions().isUseLogging()) {
            pipeline.addLast("logging-handler", new LoggingHandler(LogLevel.INFO));
        }

        // kryo codecs
        pipeline.addLast("decoder", new KryonettyDecoder(endpoint));
        pipeline.addLast("encoder", new KryonettyEncoder(endpoint));

        if(endpoint.endpointOptions().isUseExecution()) {
            // and then executed business logic.
            pipeline.addLast(executorGroup, this.channelHandler);
        } else {
            // and then business logic.
            pipeline.addLast(this.channelHandler);
        }
    }
}