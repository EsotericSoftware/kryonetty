
package com.esotericsoftware.kryonetty;

import com.esotericsoftware.kryonetty.kryo.Endpoint;
import com.esotericsoftware.kryonetty.kryo.EndpointOptions;
import com.esotericsoftware.kryonetty.kryo.KryoOptions;
import com.esotericsoftware.kryonetty.netty.KryonettyHandler;
import com.esotericsoftware.kryonetty.netty.KryonettyInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
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
public abstract class Server extends Endpoint {

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
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        channel.close();
        channel = null;
    }

    /**
     * Write the given object to the channel.
     *
     * @param object
     */
    public ChannelFuture send(Channel client, Object object) throws InterruptedException {
        return send(client, object, true);
    }

    /**
     * Write the given object to the channel.
     *
     * @param object
     * @param sync
     */
    public ChannelFuture send(Channel client, Object object, boolean sync) throws InterruptedException {
        if (sync) {
            return client.writeAndFlush(object).sync();
        } else {
            return client.writeAndFlush(object);
        }
    }

    @Override
    public Type type() {
        return Type.SERVER;
    }

}
