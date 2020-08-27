
package com.esotericsoftware.kryonetty.pool;

import com.esotericsoftware.kryonetty.kryo.Endpoint;
import com.esotericsoftware.kryonetty.kryo.KryoNetty;
import com.esotericsoftware.kryonetty.pipeline.KryonettyInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Provides a skeleton Endpoint implementation using Netty IO.
 *
 * @author Nathan Sweet
 */
public class PooledClient extends Endpoint {

    private final EventLoopGroup group;
    private Bootstrap bootstrap;
    private NettyPool nettyPool;

    public PooledClient(String host, int port, KryoNetty kryoNetty) {
        super(kryoNetty);

        // Note: We don't support KQueue. Boycott OSX and FreeBSD :P

        // inline epoll-variable
        boolean isEpoll = Epoll.isAvailable();

        // Check for eventloop-group
        this.group = isEpoll ? new EpollEventLoopGroup() : new NioEventLoopGroup();

        // Create Bootstrap
        this.bootstrap = prepareBoostrap(this.group);

        this.nettyPool = new NettyPool(host, port, bootstrap, 1);
    }

    private Bootstrap prepareBoostrap(EventLoopGroup eventLoopGroup) {
        // Create Bootstrap
        Bootstrap bootstrap = new Bootstrap()
                .group(eventLoopGroup)
                .channel(Epoll.isAvailable() ? EpollSocketChannel.class : NioSocketChannel.class)
                .handler(new KryonettyInitializer(this))
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

        // Check for extra epoll-options
        if (Epoll.isAvailable()) {
            bootstrap
                    .option(EpollChannelOption.EPOLL_MODE, EpollMode.LEVEL_TRIGGERED)
                    .option(EpollChannelOption.TCP_FASTOPEN_CONNECT, true);
        }
        return bootstrap;
    }

    /**
     * Write the given object to the channel. This will be processed async
     *
     * @param object
     */
    public void send(Object object) {
        send(object, false);
    }

    /**
     * Write the given object to the channel.
     *
     * @param object
     * @param sync
     */
    public void send(Object object, boolean sync) {
        nettyPool.send(object);
    }

    /**
     * Close the channel.
     */
    public void close() {
        eventHandler().unregisterAll();
        group.shutdownGracefully();
        nettyPool.destroy();
    }

    /**
     * @return Gives the type server or client
     */
    @Override
    public Type type() {
        return Type.CLIENT;
    }

}
