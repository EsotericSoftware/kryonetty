
package com.esotericsoftware.kryonetty;

import static org.junit.Assert.assertTrue;

import com.esotericsoftware.kryo.Kryo;

import java.net.InetSocketAddress;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SimpleTest {
	protected static boolean testRequestReceived;
	private static Server server;
	private static Client client;

	@BeforeClass
	public static void setupClass() {
		server = new Server(54321) {
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
		client = new Client(new InetSocketAddress("localhost", 54321)) {
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
	}
	
	@Test
	public void testSimple () throws Exception {
		System.out.println("== Test Simple Behaviour == ");
		client.send("i like the way you do it right thurrrr");
		Thread.sleep(1000);
	}
	
	@Test
	public void testCustomClass() throws Exception {
		System.out.println("== Test Custom Class Behaviour == ");
		client.getKryo().register(TestRequest.class);
		server.getKryo().register(TestRequest.class);
		TestRequest request = new TestRequest();
		request.someText = "Bwuk!";
		client.send(request);
		Thread.sleep(1000);
		assertTrue(testRequestReceived);
	}
}
