
package com.esotericsoftware.kryonetty;

import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

/** @author Nathan Sweet */
public abstract class Client implements Endpoint {
	private ClientBootstrap bootstrap;
	private Channel channel;

	public Client (SocketAddress serverAddress) {
		ExecutorService threadPool = Executors.newCachedThreadPool();
		bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(threadPool, threadPool));
		bootstrap.setPipelineFactory(new KryoChannelPipelineFactory(this));
		bootstrap.setOption("tcpNoDelay", true);
		// bootstrap.setOption("trafficClass", 0x10); // IPTOS_LOWDELAY
		ChannelFuture connect = bootstrap.connect(serverAddress);
		if (!connect.awaitUninterruptibly(5000)) throw new RuntimeException("Timeout connecting.");
		channel = connect.getChannel();
	}

	public void send (Object object) {
		channel.write(object);
	}

	public void close () {
		channel.close();
		channel = null;
	}
}
