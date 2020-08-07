
package com.esotericsoftware.kryonetty;

import com.esotericsoftware.kryonetty.kryo.Endpoint;
import com.esotericsoftware.kryonetty.kryo.KryoNetty;
import com.esotericsoftware.kryonetty.pipeline.KryonettyHandler;
import com.esotericsoftware.kryonetty.pipeline.KryonettyInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

/**
 * Provides a skeleton Endpoint implementation using Netty IO.
 *
 * @author Nathan Sweet
 */
public class Client extends Endpoint {

    private final Bootstrap bootstrap;
    private final EventLoopGroup group;
    private Channel channel;

    public Client(KryoNetty kryoNetty) {
        super(kryoNetty);

        boolean isEpoll = Epoll.isAvailable();

        this.group = isEpoll ? new EpollEventLoopGroup() : new NioEventLoopGroup();

        this.bootstrap = new Bootstrap()
                .group(group)
                .channel(isEpoll ? EpollSocketChannel.class : NioSocketChannel.class)
                .handler(new KryonettyInitializer(this, new KryonettyHandler(this)))
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        if(isEpoll) {
            bootstrap
                    .option(EpollChannelOption.EPOLL_MODE, EpollMode.EDGE_TRIGGERED)
                    .option(EpollChannelOption.TCP_FASTOPEN_CONNECT, true);
        }
    }

    /**
     * Write the given object to the channel. This will be processed async
     *
     * @param object
     */
    public ChannelFuture send(Object object) throws InterruptedException {
        return send(object, false);
    }

    /**
     * Write the given object to the channel.
     *
     * @param object
     * @param sync
     */
    public ChannelFuture send(Object object, boolean sync) throws InterruptedException {
        if (sync) {
            return channel.writeAndFlush(object).sync();
        } else {
            return channel.writeAndFlush(object);
        }
    }

    /**
     * Close the channel.
     */
    public void close() {
        eventHandler().unregisterAll();
        group.shutdownGracefully();
        channel.close();
        channel = null;
    }

    public void connect(String host, int port) {
        try {
            // Start the client

            ChannelFuture f = bootstrap.connect(new InetSocketAddress(host, port));
            channel = f.sync().channel();

            // Wait until the connection is closed.
//         ChannelFuture testFuture = channel.closeFuture().sync();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public Type type() {
        return Type.CLIENT;
    }

}
