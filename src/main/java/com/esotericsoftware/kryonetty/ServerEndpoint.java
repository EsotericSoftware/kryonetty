
package com.esotericsoftware.kryonetty;

import com.esotericsoftware.kryonetty.kryo.Endpoint;
import com.esotericsoftware.kryonetty.kryo.KryoNetty;
import com.esotericsoftware.kryonetty.pipeline.KryoNettyInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

public class ServerEndpoint extends Endpoint {

    private final ServerBootstrap bootstrap;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private Channel channel;

    public ServerEndpoint(KryoNetty kryoNetty) {
        super(kryoNetty);

        // inline epoll-variable
        boolean isEpoll = Epoll.isAvailable();

        // get runtime processors for thread-size
        int cores = Runtime.getRuntime().availableProcessors();

        // Check for eventloop-groups
        this.bossGroup = isEpoll ? new EpollEventLoopGroup(2 * cores) : new NioEventLoopGroup(2 * cores);
        this.workerGroup = isEpoll ? new EpollEventLoopGroup(10 * cores) : new NioEventLoopGroup(10 * cores);

        // Create ServerBootstrap
        this.bootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(isEpoll ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .childHandler(new KryoNettyInitializer(this))
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.IP_TOS, 24)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.SO_REUSEADDR, true);

        // Check for extra epoll-options
        if(isEpoll) {
            bootstrap
                    .childOption(EpollChannelOption.EPOLL_MODE, EpollMode.LEVEL_TRIGGERED)
                    .option(EpollChannelOption.TCP_FASTOPEN, 3)
                    .option(EpollChannelOption.SO_REUSEPORT, true);
        }
    }

    /**
     * Let the server bind to the given port
     */
    public void start(int port) {
        try {
            // Start the server and wait for socket to be bind to the given port
            channel = bootstrap.bind(new InetSocketAddress(port)).sync().channel();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes the server socket.
     */
    public void close() {
        // unregister network-events
        getEventHandler().unregisterAll();

        // shutdown eventloop-groups
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();

        // close server-channel
        channel.close();
    }

    /**
     * Write the given object to the channel. This will be processed async
     *
     * @param object
     */
    public ChannelFuture send(ChannelHandlerContext ctx, Object object) {
        // use send-method, default-behaviour: async
        return send(ctx, object, false);
    }

    /**
     * Write the given object to the channel.
     *
     * @param object
     * @param sync
     */
    public ChannelFuture send(ChannelHandlerContext ctx, Object object, boolean sync) {
        if (sync) {
            try {
                return ctx.writeAndFlush(object).sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        } else {
            return ctx.writeAndFlush(object);
        }
    }

    /**
     * @return Gives the type server or client
     */
    @Override
    public Type getType() {
        return Type.SERVER;
    }

}
