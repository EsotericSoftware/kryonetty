
package com.esotericsoftware.kryonetty;

import java.net.InetSocketAddress;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * Skeleton Kryo server implementation using Netty.
 * @author Nathan Sweet
 */
public abstract class Server implements Endpoint {
	private ServerBootstrap bootstrap;
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	private Channel channel;

	public Server () {
		bossGroup = new NioEventLoopGroup(1);
		workerGroup = new NioEventLoopGroup();
        
		bootstrap = new ServerBootstrap();
		
		bootstrap.group(bossGroup, workerGroup)
			.channel(NioServerSocketChannel.class)
			.handler(new LoggingHandler(LogLevel.INFO));
		bootstrap.childHandler(new KryonettyServerInitializer(this));
		bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
		bootstrap.childOption(ChannelOption.SO_REUSEADDR, true);
	}
	
	public void start(int port) {
		try {
			// Start the server.
         ChannelFuture f = bootstrap.bind(new InetSocketAddress(port));
         channel = f.sync().channel();

         // Wait until the server socket is closed.
         //f.channel().closeFuture().sync();
		}catch(InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void close () {
		channel.close();
		channel = null;
	}
}
