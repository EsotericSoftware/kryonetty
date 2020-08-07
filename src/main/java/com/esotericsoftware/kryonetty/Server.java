
package com.esotericsoftware.kryonetty;

import com.esotericsoftware.kryonetty.kryo.Endpoint;
import com.esotericsoftware.kryonetty.kryo.EndpointOptions;
import com.esotericsoftware.kryonetty.kryo.KryoOptions;
import com.esotericsoftware.kryonetty.pipeline.KryonettyHandler;
import com.esotericsoftware.kryonetty.pipeline.KryonettyInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

/**
 * Skeleton Kryo server implementation using Netty.
 *
 * @author Nathan Sweet
 */
public class Server extends Endpoint {

    private final ServerBootstrap bootstrap;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private Channel channel;

    public Server(KryoOptions kryoOptions, EndpointOptions endpointOptions) {
        super(kryoOptions, endpointOptions);

        boolean isEpoll = Epoll.isAvailable();

        this.bossGroup = isEpoll ? new EpollEventLoopGroup(1) : new NioEventLoopGroup(1);
        this.workerGroup = isEpoll ? new EpollEventLoopGroup() : new NioEventLoopGroup();

        this.bootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(isEpoll ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .childHandler(new KryonettyInitializer(this, new KryonettyHandler(this)))
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_REUSEADDR, true);
    }

    public void start(int port) {
        try {
            // Start the server.
            ChannelFuture f = bootstrap.bind(new InetSocketAddress(port));
            channel = f.sync().channel();

            // Wait until the server socket is closed.
            //f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        eventHandler().unregisterAll();
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        channel.close();
        channel = null;
    }

    /**
     * Write the given object to the channel. This will be processed async
     *
     * @param object
     */
    public ChannelFuture send(ChannelHandlerContext ctx, Object object) throws InterruptedException {
        return send(ctx, object, false);
    }

    /**
     * Write the given object to the channel.
     *
     * @param object
     * @param sync
     */
    public ChannelFuture send(ChannelHandlerContext ctx, Object object, boolean sync) throws InterruptedException {
        if (sync) {
            return ctx.writeAndFlush(object).sync();
        } else {
            return ctx.writeAndFlush(object);
        }
    }

    @Override
    public Type type() {
        return Type.SERVER;
    }

}
