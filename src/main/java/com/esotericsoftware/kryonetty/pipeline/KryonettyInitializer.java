package com.esotericsoftware.kryonetty.pipeline;

import com.esotericsoftware.kryonetty.kryo.Endpoint;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

public class KryonettyInitializer extends ChannelInitializer<SocketChannel> {

    private final Endpoint endpoint;
    private final EventExecutorGroup executorGroup;

    public KryonettyInitializer(Endpoint endpoint) {
        this.endpoint = endpoint;

        // Initialize if execution is enabled
        if (endpoint.kryoNetty().getExecutionThreadSize() > 0)
            this.executorGroup = new DefaultEventExecutorGroup(endpoint.kryoNetty().getExecutionThreadSize());
        else
            this.executorGroup = null;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        // Get pipeline-instance
        ChannelPipeline pipeline = ch.pipeline();

        // Add logging-handler if enabled
        if (endpoint.kryoNetty().isUseLogging()) {
            pipeline.addLast("logging-handler", new LoggingHandler(LogLevel.INFO));
        }

        // kryo codecs
        pipeline.addLast("decoder", new KryonettyDecoder(endpoint));
        pipeline.addLast("encoder", new KryonettyEncoder(endpoint));

        if (endpoint.kryoNetty().isUseExecution()) {

            // and then async-executed business logic.
            pipeline.addLast(executorGroup, new KryonettyHandler(endpoint));

        } else {

            // and then async-executed business logic.
            pipeline.addLast(new KryonettyHandler(endpoint));
        }
    }
}