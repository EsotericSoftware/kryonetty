
package com.esotericsoftware.kryonetty;

import com.esotericsoftware.kryonetty.kryo.KryoNetty;
import com.esotericsoftware.kryonetty.network.ConnectEvent;
import com.esotericsoftware.kryonetty.network.DisconnectEvent;
import com.esotericsoftware.kryonetty.network.ReceiveEvent;
import com.esotericsoftware.kryonetty.network.handler.NetworkHandler;
import com.esotericsoftware.kryonetty.network.handler.NetworkListener;
import io.netty.channel.ChannelHandlerContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SimpleTest {

    public static final TestRequest TEST_REQUEST = new TestRequest("I'm not rich, but maybe with tests", 52411, true, Arrays.asList("Test1", "Test2", "Test3", "Test4", "Test5"));

    public static boolean testRequestReceived;
    public static Server server;
    public static Client client;

    @BeforeClass
    public static void setupClass() {

        KryoNetty kryoNetty = new KryoNetty()
                .useLogging()
                .useExecution()
                .threadSize(16)
                .inputSize(4096)
                .outputSize(4096)
                .maxOutputSize(-1)
                .register(TestRequest.class);

        server = new ThreadedServer(kryoNetty);
        server.eventHandler().register(new NetworkListener() {
            @NetworkHandler
            public void onConnect(ConnectEvent event) {
                ChannelHandlerContext ctx = event.getCtx();
                System.out.println("Server: Client connected: " + ctx.channel().remoteAddress());
                ctx.channel().write("make a programmer rich");
            }
        });
        server.eventHandler().register(new NetworkListener() {
            @NetworkHandler
            public void onReceive(ReceiveEvent event) {
                Object object = event.getObject();
                ChannelHandlerContext ctx = event.getCtx();
                System.out.println("Server: Received: " + object);
                if (object instanceof TestRequest) {
                    testRequestReceived = true;
                    TestRequest request = (TestRequest) object;
                    try {
                        server.send(ctx, request, false);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        server.eventHandler().register(new NetworkListener() {
            @NetworkHandler
            public void onDisconnect(DisconnectEvent event) {
                ChannelHandlerContext ctx = event.getCtx();
                System.out.println("Server: Client disconnected: " + ctx.channel().remoteAddress());
            }
        });


        client = new ThreadedClient(kryoNetty);

        client.eventHandler().register(new NetworkListener() {
            @NetworkHandler
            public void onConnect(ConnectEvent event) {
                ChannelHandlerContext ctx = event.getCtx();
                System.out.println("Client: Connected to server: " + ctx.channel().remoteAddress());
            }
        });
        client.eventHandler().register(new NetworkListener() {
            @NetworkHandler
            public void onReceive(ReceiveEvent event) {
                Object object = event.getObject();
                ChannelHandlerContext ctx = event.getCtx();
                System.out.println("Client: Received: " + object);
                if(object instanceof TestRequest) {
                    TestRequest request = (TestRequest) object;
                    assertEquals(request.someText, TEST_REQUEST.someText);
                    assertEquals(request.someLong, TEST_REQUEST.someLong);
                    assertEquals(request.someBoolean, TEST_REQUEST.someBoolean);
                    assertEquals(request.someList, TEST_REQUEST.someList);
                    System.out.println("Client: Finished Tests!");
                }
            }
        });
        client.eventHandler().register(new NetworkListener() {
            @NetworkHandler
            public void onDisconnect(DisconnectEvent event) {
                ChannelHandlerContext ctx = event.getCtx();
                System.out.println("Client: Disconnected from server: " + ctx.channel().remoteAddress());
            }
        });

        server.start(54321);
        client.connect("localhost", 54321);
    }

    @AfterClass
    public static void afterClass() {
        client.close();
        server.close();
    }

    @Test
    public void testSimple() throws Exception {
        System.out.println("== Test Simple Behaviour == ");
        client.send("i like the way you do it right thurrrr");
        Thread.sleep(1000);
    }

    @Test
    public void testCustomClass() throws Exception {
        System.out.println("== Test Custom Class Behaviour == ");
        client.send(TEST_REQUEST);
        Thread.sleep(1000);
        assertTrue(testRequestReceived);
    }
}
