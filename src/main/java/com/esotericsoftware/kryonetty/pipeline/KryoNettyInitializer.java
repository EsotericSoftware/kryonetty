package com.esotericsoftware.kryonetty.pipeline;

import com.esotericsoftware.kryonetty.kryo.Endpoint;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

public class KryoNettyInitializer extends ChannelInitializer<SocketChannel> {

    private final Endpoint IEndpoint;
    private final EventExecutorGroup executorGroup;

    public KryoNettyInitializer(Endpoint IEndpoint) {
        this.IEndpoint = IEndpoint;

        // Initialize if execution is enabled
        if (IEndpoint.getKryoNetty().getExecutionThreadSize() > 0)
            this.executorGroup = new DefaultEventExecutorGroup(IEndpoint.getKryoNetty().getExecutionThreadSize());
        else
            this.executorGroup = null;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        // Get pipeline-instance
        ChannelPipeline pipeline = ch.pipeline();

        // Add logging-handler if enabled
        if (IEndpoint.getKryoNetty().isUseLogging())
            pipeline.addLast("logging-handler", new LoggingHandler(LogLevel.INFO));


        // kryo codecs
        pipeline.addLast("decoder", new KryoNettyDecoder(IEndpoint));
        pipeline.addLast("encoder", new KryoNettyEncoder(IEndpoint));

        if (IEndpoint.getKryoNetty().isUseExecution()) {

            // and then async-executed business logic.
            pipeline.addLast(executorGroup, new KryoNettyHandler(IEndpoint));

        } else {

            // and then async-executed business logic.
            pipeline.addLast(new KryoNettyHandler(IEndpoint));
        }
    }
}