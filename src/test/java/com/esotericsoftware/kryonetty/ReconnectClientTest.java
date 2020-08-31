
package com.esotericsoftware.kryonetty;

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.esotericsoftware.kryonetty.kryo.KryoNetty;
import com.esotericsoftware.kryonetty.network.ConnectEvent;
import com.esotericsoftware.kryonetty.network.DisconnectEvent;
import com.esotericsoftware.kryonetty.network.ReceiveEvent;
import com.esotericsoftware.kryonetty.network.handler.NetworkHandler;
import com.esotericsoftware.kryonetty.network.handler.NetworkListener;
import com.esotericsoftware.kryonetty.objects.EmptyRequest;
import com.esotericsoftware.kryonetty.objects.TestRequest;
import io.netty.channel.ChannelHandlerContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertTrue;


public class ReconnectClientTest {

    public static Server server;
    public static Client client;

    public static boolean received;

    @BeforeClass
    public static void setupClass() throws Exception {

        KryoNetty kryoNetty = new KryoNetty()
                .useExecution()
                .threadSize(128)
                .inputSize(2048)
                .outputSize(2048)
                .maxOutputSize(-1)
                .register(200, TestRequest.class)
                .register(201, EmptyRequest.class);

        server = new ThreadedServer(kryoNetty);
        server.eventHandler().register(new NetworkListener() {

            @NetworkHandler
            public void onConnect(ConnectEvent event) {
                ChannelHandlerContext ctx = event.getCtx();
                System.out.println("Server: Client connected: " + ctx.channel().remoteAddress());
            }

            @NetworkHandler
            public void onReceive(ReceiveEvent event) {
                if(event.getObject() instanceof EmptyRequest) {
                    received = true;
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

            }

            @NetworkHandler
            public void onDisconnect(DisconnectEvent event) {
                ChannelHandlerContext ctx = event.getCtx();
                System.out.println("Client: Disconnected from server: " + ctx.channel().remoteAddress());
            }
        });

        server.start(54321);
        client.connect("localhost", 54321);

        received = false;

        Thread.sleep(1500L);
    }

    @AfterClass
    public static void afterClass() {
        client.close();
        server.close();
    }

    @Test
    public void testReconnect() throws Exception {
        System.out.println("== Test Reconnect Behaviour == ");
        client.send(new EmptyRequest());
        client.closeChannel();
        System.out.println("Wait for disconnect");
        client.connect("localhost", 54321);
        System.out.println("Send Request");
        client.send(new EmptyRequest());
        Thread.sleep(500L);
        assertTrue(received);
        System.out.println("== Finished Test Reconnect Behaviour == ");
    }

}
