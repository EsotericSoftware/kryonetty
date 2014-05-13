
package com.esotericsoftware.kryonetty;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

/** @author Nathan Sweet */
public abstract class Server implements Endpoint {
	private ServerBootstrap bootstrap;

	public Server (int port) {
		ExecutorService threadPool = Executors.newCachedThreadPool();
		bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(threadPool, threadPool));
		bootstrap.setPipelineFactory(new KryoChannelPipelineFactory(this));
		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.reuseAddress", true);
		bootstrap.bind(new InetSocketAddress(port));
	}
}
