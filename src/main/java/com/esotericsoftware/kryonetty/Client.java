
package com.esotericsoftware.kryonetty;

import com.esotericsoftware.kryonetty.kryo.Endpoint;
import com.esotericsoftware.kryonetty.kryo.KryoNetty;
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

        // Note: We don't support KQueue. Boycott OSX and FreeBSD :P

        // inline epoll-variable
        boolean isEpoll = Epoll.isAvailable();

        // Check for eventloop-group
        this.group = isEpoll ? new EpollEventLoopGroup() : new NioEventLoopGroup();

        // Create Bootstrap
        this.bootstrap = new Bootstrap()
                .group(group)
                .channel(isEpoll ? EpollSocketChannel.class : NioSocketChannel.class)
                .handler(new KryonettyInitializer(this))
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

        // Check for extra epoll-options
        if(isEpoll) {
            bootstrap
                    .option(EpollChannelOption.EPOLL_MODE, EpollMode.EDGE_TRIGGERED)
                    .option(EpollChannelOption.TCP_FASTOPEN_CONNECT, true);
        }
    }


    /**
     * Return if the client is connected or not
     */
    public boolean isConnected() {
        return this.channel != null && this.channel.isOpen() && this.channel.isActive();
    }

    /**
     * Write the given object to the channel. This will be processed async
     *
     * @param object
     */
    public ChannelFuture send(Object object) {
        return send(object, false);
    }

    /**
     * Write the given object to the channel.
     *
     * @param object
     * @param sync
     */
    public ChannelFuture send(Object object, boolean sync) {
        if(isConnected()) {
            if (sync) {
                try {
                    return channel.writeAndFlush(object).sync();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                return channel.writeAndFlush(object);
            }
        }
        return null;
    }

    public void reconnect(String host, int port) {
        if(channel != null) {
            channel.close();
            channel = null;
        }
        if(!isConnected()) {
            connect(host, port);
        }
    }

    /**
     * Close the channel.
     */
    public void close() {
        eventHandler().unregisterAll();
        group.shutdownGracefully();
        if(channel != null) {
            channel.close();
            channel = null;
        }
    }

    /**
     * Connects the client to the given host and port
     */
    public void connect(String host, int port) {
        if(!isConnected()) {
            try {
                // Start the client and wait for the connection to be established.
                channel = bootstrap.connect(new InetSocketAddress(host, port)).sync().channel();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @return Gives the type server or client
     */
    @Override
    public Type type() {
        return Type.CLIENT;
    }

}
