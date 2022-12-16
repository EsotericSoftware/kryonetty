
package com.esotericsoftware.kryonetty;

import com.esotericsoftware.kryonetty.kryo.Endpoint;
import com.esotericsoftware.kryonetty.kryo.KryoNetty;
import com.esotericsoftware.kryonetty.pipeline.KryoNettyInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

public class ClientEndpoint extends Endpoint {

    private final EventLoopGroup group;
    private final Bootstrap bootstrap;
    private Channel channel;

    public ClientEndpoint(KryoNetty kryoNetty) {
        super(kryoNetty);

        // Note: We don't support KQueue. Boycott OSX and FreeBSD :P

        // inline epoll-variable
        boolean isEpoll = Epoll.isAvailable();

        // get runtime processors for thread-size
        int cores = Runtime.getRuntime().availableProcessors();

        // Check for eventloop-group
        this.group = isEpoll ? new EpollEventLoopGroup(10 * cores) : new NioEventLoopGroup(10 * cores);

        // Create Bootstrap
        this.bootstrap = prepareBoostrap(this.group);

    }

    private Bootstrap prepareBoostrap(EventLoopGroup eventLoopGroup) {
        // Create Bootstrap
        Bootstrap bootstrap = new Bootstrap()
                .group(eventLoopGroup)
                .channel(Epoll.isAvailable() ? EpollSocketChannel.class : NioSocketChannel.class)
                .handler(new KryoNettyInitializer(this))
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
        if (isConnected()) {
            if (sync) {
                try {
                    channel.writeAndFlush(object).sync();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                if(channel.eventLoop() == null || channel.eventLoop().inEventLoop()) {
                    channel.writeAndFlush(object);
                } else {
                    channel.eventLoop().execute(() -> channel.writeAndFlush(object));
                }
            }
        }
    }

    public void reconnect(String host, int port) {
        closeChannel();
        connect(host, port);
    }

    public void closeChannel(){
        if (isConnected()) {
            try {
                channel.close().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Close the channel.
     */
    public void close() {
        getEventHandler().unregisterAll();
        closeChannel();
        group.shutdownGracefully();
    }

    /**
     * Connects the client to the given host and port
     */
    public void connect(String host, int port) {
        // Close the Channel if it's already connected
        if (!isConnected()) {
            closeChannel();
        }
        // Start the client and wait for the connection to be established.
        try {
            this.channel = this.bootstrap.connect(new InetSocketAddress(host, port)).sync().channel();
        } catch (InterruptedException e) {
            e.printStackTrace();

        }
    }

    /**
     * @return Gives the type server or client
     */
    @Override
    public Type getType() {
        return Type.CLIENT;
    }

}
