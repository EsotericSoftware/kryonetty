
package com.esotericsoftware.kryonetty;

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
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
import java.util.HashMap;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SimpleTest extends AbstractBenchmark {

    public static final TestRequest TEST_REQUEST = new TestRequest("I'm not rich, but maybe with tests", 52411, true, Arrays.asList("Test1", "Test2", "Test3", "Test4", "Test5"));

    public static boolean testRequestReceived;
    public static Server server;
    public static Client client;

    @BeforeClass
    public static void setupClass() {

        KryoNetty kryoNetty = new KryoNetty()
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

            @NetworkHandler
            public void onReceive(ReceiveEvent event) {
                Object object = event.getObject();
                ChannelHandlerContext ctx = event.getCtx();
                System.out.println("Server: Received: " + object + " from " + ctx.channel().remoteAddress());
                if (object instanceof TestRequest) {
                    testRequestReceived = true;
                    TestRequest request = (TestRequest) object;
                    server.send(ctx, request, false);
                }
                if (object instanceof HashMap) {
                    HashMap<String, TestRequest> hashMap = (HashMap<String, TestRequest>) event.getObject();
                    server.send(ctx, hashMap, false);
                }
            }

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

            @NetworkHandler
            public void onReceive(ReceiveEvent event) {
                Object object = event.getObject();
                ChannelHandlerContext ctx = event.getCtx();
                System.out.println("Client: Received: " + object + " from " + ctx.channel().remoteAddress());
                if (object instanceof TestRequest) {
                    TestRequest request = (TestRequest) object;
                    assertEquals(request.someText, TEST_REQUEST.someText);
                    assertEquals(request.someLong, TEST_REQUEST.someLong);
                    assertEquals(request.someBoolean, TEST_REQUEST.someBoolean);
                    assertEquals(request.someList, TEST_REQUEST.someList);
                    System.out.println("Client: Finished Tests!");
                }
            }

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
    public void testString() throws Exception {
        System.out.println("== Test String Behaviour == ");
        client.send("i like the way you do it right thurrrr");
        Thread.sleep(500);
    }

    @Test
    public void testRequestClass() throws Exception {
        System.out.println("== Test Request Class Behaviour == ");
        client.send(TEST_REQUEST);
        Thread.sleep(500);
        assertTrue(testRequestReceived);
    }

    @Test
    public void testMap() throws Exception {
        System.out.println("== Test Map Behaviour == ");
        HashMap<String, TestRequest> hashMap = new HashMap<>();
        IntStream.range(0, 50).forEach(i -> hashMap.put("id#" + i, TEST_REQUEST));
        client.send(hashMap);
        Thread.sleep(500);
        assertTrue(testRequestReceived);
    }
}
