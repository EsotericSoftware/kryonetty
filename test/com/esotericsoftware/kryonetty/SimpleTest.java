
package com.esotericsoftware.kryonetty;

import com.esotericsoftware.kryo.Kryo;

import java.net.InetSocketAddress;

import junit.framework.TestCase;

import org.jboss.netty.channel.ChannelHandlerContext;

public class SimpleTest extends TestCase {
	protected boolean testRequestReceived;

	public void testSimple () throws Exception {
		Server server = new Server(54321) {
			public void connected (ChannelHandlerContext ctx) {
				System.out.println("Server: Client connected: " + ctx.getChannel().getRemoteAddress());
				ctx.getChannel().write("make a programmer rich");
			}

			public void disconnected (ChannelHandlerContext ctx) {
				System.out.println("Server: Client disconnected: " + ctx.getChannel().getRemoteAddress());
			}

			public void received (ChannelHandlerContext ctx, Object object) {
				System.out.println("Server: Received: " + object);
				if(object instanceof TestRequest) {
					testRequestReceived = true;
				}
			}

			public Kryo getKryo () {
				return new Kryo();
			}
		};

		Client client = new Client(new InetSocketAddress("localhost", 54321)) {
			public void connected (ChannelHandlerContext ctx) {
				System.out.println("Client: Connected to server: " + ctx.getChannel().getRemoteAddress());
			}

			public void disconnected (ChannelHandlerContext ctx) {
				System.out.println("Client: Disconnected from server: " + ctx.getChannel().getRemoteAddress());
			}

			public void received (ChannelHandlerContext ctx, Object object) {
				System.out.println("Client: Received: " + object);
			}

			public Kryo getKryo () {
				return new Kryo();
			}
		};
		client.getKryo().register(TestRequest.class);
		server.getKryo().register(TestRequest.class);
		TestRequest request = new TestRequest();
		request.someText = "Bwuk!";
		client.send("i like the way you do it right thurrrr");
		client.send(request);
		Thread.sleep(1000);
		client.close();
		assertTrue(testRequestReceived);
	}
}
