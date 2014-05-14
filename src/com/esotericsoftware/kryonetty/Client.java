
package com.esotericsoftware.kryonetty;

import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

/**
 * Provides a skeleton Endpoint implementation using Netty IO.
 * @author Nathan Sweet
 */
public abstract class Client implements Endpoint {
	/**
	 * Netty bootstrap object used to create the channel.
	 */
	private ClientBootstrap bootstrap;
	/**
	 * Netty channel used to write objects on.
	 */
	private Channel channel;
	/**
	 * Connection timeout in milliseconds
	 */
	private static final int CONNECT_TIMEOUT = 5000;

	/**
	 * Create a new client connected to the given socket.
	 * @param serverAddress Server to connect to.
	 */
	public Client (SocketAddress serverAddress) {
		ExecutorService threadPool = Executors.newCachedThreadPool();
		bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(threadPool, threadPool));
		bootstrap.setPipelineFactory(new KryoChannelPipelineFactory(this));
		bootstrap.setOption("tcpNoDelay", true);
		// bootstrap.setOption("trafficClass", 0x10); // IPTOS_LOWDELAY
		ChannelFuture connect = bootstrap.connect(serverAddress);
		if (!connect.awaitUninterruptibly(CONNECT_TIMEOUT))
				throw new RuntimeException("Timeout connecting.");
		channel = connect.getChannel();
	}

	/**
	 * Write the given object to the channel.
	 * @param object
	 */
	public void send (Object object) {
		channel.write(object);
	}

	/**
	 * Close the channel.
	 */
	public void close () {
		channel.close();
		channel = null;
	}
}
