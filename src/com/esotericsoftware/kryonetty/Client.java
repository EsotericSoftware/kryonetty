
package com.esotericsoftware.kryonetty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.SocketAddress;

/**
 * Provides a skeleton Endpoint implementation using Netty IO.
 * @author Nathan Sweet
 */
public abstract class Client implements Endpoint {
	/**
	 * Netty bootstrap object used to create the channel.
	 */
	private Bootstrap bootstrap;
	/**
	 * Netty channel used to write objects on.
	 */
	private Channel channel;
	/**
	 * Netty EventLoopGroup used for events.
	 */
	private EventLoopGroup group;
	/**
	 * Connection timeout in milliseconds
	 */
	private static final int CONNECT_TIMEOUT = 5000;
	
	/**
	 * Create a new client connected to the given socket.
	 */
	public Client() {
		group = new NioEventLoopGroup();
		
		bootstrap = new Bootstrap();
		bootstrap.group(group)
				.channel(NioSocketChannel.class)
				.option(ChannelOption.TCP_NODELAY, true)
				.handler(new KryonettyClientInitializer(this));
	}

	/**
	 * Write the given object to the channel.
	 * @param obj
	 */
	public void send(Object obj) throws InterruptedException {
		ChannelFuture lastWriteFuture = null;
		lastWriteFuture = channel.writeAndFlush(obj);
		
		if (lastWriteFuture != null) {
			lastWriteFuture.sync();
      }
	}

	/**
	 * Close the channel.
	 */
	public void close () {
		channel.close();
		channel = null;
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
}
