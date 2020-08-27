package com.esotericsoftware.kryonetty.pool;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;

import java.net.InetSocketAddress;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NettyPool {

    private final ExecutorService executor;
    private final String host;
    private final int port;
    private final Bootstrap bootstrap;
    private final int poolSize;
    private final Set<Channel> activeChannelSet;
    private final Deque<Channel> freeChannelDeque;

    public NettyPool(String host, int port, Bootstrap bootstrap, int poolSize) {
        this.executor = Executors.newFixedThreadPool(poolSize);
        this.host = host;
        this.port = port;
        this.bootstrap = bootstrap;
        this.poolSize = poolSize;
        this.activeChannelSet = new HashSet<>();
        this.freeChannelDeque = new ArrayDeque<>();
    }

    private Channel newChannel() {
        synchronized (activeChannelSet) {
            Channel channel = bootstrap.connect(new InetSocketAddress(this.host, this.port)).syncUninterruptibly().channel();
            activeChannelSet.add(channel);
            channel.closeFuture().addListener((ChannelFutureListener) closeFuture -> {
                synchronized (activeChannelSet) {
                    activeChannelSet.remove(closeFuture.channel());
                }
            });
            return channel;
        }
    }

    private Channel waitForFree() {
        Channel channel = null;
        while (!freeChannelDeque.isEmpty()) {
            channel = freeChannelDeque.pollFirst();
            if (channel.isOpen())
                break;
        }
        return channel;
    }

    public Channel borrow() {
        synchronized (freeChannelDeque) {
            while (!freeChannelDeque.isEmpty()) {
                Channel channel = freeChannelDeque.pollFirst();
                if (channel.isOpen())
                    return channel;
            }
        }
        return activeChannelSet.size() < poolSize ? newChannel() : waitForFree();
    }

    public void release(Channel channel) {
        synchronized (freeChannelDeque) {
            freeChannelDeque.addLast(channel);
        }
    }

    public void send(Object object) {
        executor.execute(() -> {
            try {
                Channel channel = borrow();
                if (channel != null) {
                    channel.writeAndFlush(object);
                    release(channel);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void destroy() {
        freeChannelDeque.forEach(Channel::close);
        activeChannelSet.forEach(Channel::close);
    }

}
