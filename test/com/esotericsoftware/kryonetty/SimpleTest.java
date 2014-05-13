
package com.esotericsoftware.kryonetty;

import static org.junit.Assert.assertTrue;

import com.esotericsoftware.kryo.Kryo;

import java.net.InetSocketAddress;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.junit.Before;
import org.junit.Test;

public class SimpleTest {
	protected boolean testRequestReceived;
	private Server server;
	private Client client;

	@Before
	public void setup() {
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
		client.send("i like the way you do it right thurrrr");
		client.close();
		server.close();
	}
	
	@Test
	public void testRegisterClass() throws Exception {

		client.getKryo().register(TestRequest.class);
		server.getKryo().register(TestRequest.class);
		TestRequest request = new TestRequest();
		request.someText = "Bwuk!";
		client.send(request);
		Thread.sleep(1000);
		client.close();
		assertTrue(testRequestReceived);
	}
}
