
package com.esotericsoftware.kryonetty;

import io.netty.channel.ChannelHandlerContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.InetSocketAddress;

import static org.junit.Assert.assertTrue;

public class SimpleTest {
	protected static boolean testRequestReceived;
	private static Server server;
	private static Client client;

	@BeforeClass
	public static void setupClass() {
		server = new Server() {
			public void connected (ChannelHandlerContext ctx) {
				System.out.println("Server: Client connected: " + ctx.channel().remoteAddress());
				ctx.channel().write("make a programmer rich");
			}

			public void disconnected (ChannelHandlerContext ctx) {
				System.out.println("Server: Client disconnected: " + ctx.channel().remoteAddress());
			}

			public void received (ChannelHandlerContext ctx, Object object) {
				System.out.println("Server: Received: " + object);
				if(object instanceof TestRequest) {
					testRequestReceived = true;
				}
			}

			@Override
			public KryoHolder getKryoHolder() {
				return new KryoHolder(-1, -1, String.class, TestRequest.class);
			}
		};
		client = new Client() {
			public void connected (ChannelHandlerContext ctx) {
				System.out.println("Client: Connected to server: " + ctx.channel().remoteAddress());
			}

			public void disconnected (ChannelHandlerContext ctx) {
				System.out.println("Client: Disconnected from server: " + ctx.channel().remoteAddress());
			}

			public void received (ChannelHandlerContext ctx, Object object) {
				System.out.println("Client: Received: " + object);
			}

			@Override
			public KryoHolder getKryoHolder() {
				return new KryoHolder(-1, -1, String.class, TestRequest.class);
			}
		};
		
		server.start(54321);
		client.connect(new InetSocketAddress("localhost", 54321));
	}
	
	@AfterClass
	public static void afterClass() {
		client.close();
		server.close();
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
		TestRequest request = new TestRequest();
		request.someText = "Bwuk!";
		client.send(request);
		Thread.sleep(1000);
		assertTrue(testRequestReceived);
	}
}
