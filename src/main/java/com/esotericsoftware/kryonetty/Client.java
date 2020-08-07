
package com.esotericsoftware.kryonetty;

import com.esotericsoftware.kryonetty.kryo.Endpoint;
import com.esotericsoftware.kryonetty.kryo.EndpointOptions;
import com.esotericsoftware.kryonetty.kryo.KryoOptions;
import com.esotericsoftware.kryonetty.netty.KryonettyHandler;
import com.esotericsoftware.kryonetty.netty.KryonettyInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.SocketAddress;

/**
 * Provides a skeleton Endpoint implementation using Netty IO.
 *
 * @author Nathan Sweet
 */
public abstract class Client extends Endpoint {

    private final Bootstrap bootstrap;
    private final EventLoopGroup group;
    private Channel channel;

    public Client(KryoOptions kryoOptions, EndpointOptions endpointOptions) {
        super(kryoOptions, endpointOptions);

        boolean isEpoll = Epoll.isAvailable();

        this.group = isEpoll ? new EpollEventLoopGroup() : new NioEventLoopGroup();

        this.bootstrap = new Bootstrap()
                .group(group)
                .channel(isEpoll ? EpollSocketChannel.class : NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new KryonettyInitializer(this, new KryonettyHandler(this)));
    }

    /**
     * Write the given object to the channel.
     *
     * @param object
     */
    public ChannelFuture send(Object object) throws InterruptedException {
        return send(object, true);
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
        channel.close();
        channel = null;
        group.shutdownGracefully();
    }

    public void connect(SocketAddress serverAddress) {
        try {
            // Start the client
            ChannelFuture f = bootstrap.connect(serverAddress);
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
