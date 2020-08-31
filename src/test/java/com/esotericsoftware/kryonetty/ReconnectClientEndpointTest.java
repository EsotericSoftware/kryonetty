
package com.esotericsoftware.kryonetty;

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


public class ReconnectClientEndpointTest {

    public static ServerEndpoint serverEndpoint;
    public static ClientEndpoint clientEndpoint;

    public static boolean received;

    @BeforeClass
    public static void setupClass() throws Exception {

        KryoNetty kryoNetty = new KryoNetty()
                .useExecution()
                .threadSize(32)
                .inputSize(1024)
                .outputSize(1024)
                .maxOutputSize(-1)
                .register(200, TestRequest.class)
                .register(201, EmptyRequest.class);

        serverEndpoint = new ThreadedServerEndpoint(kryoNetty);
        serverEndpoint.getEventHandler().register(new NetworkListener() {

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


        clientEndpoint = new ThreadedClientEndpoint(kryoNetty);
        clientEndpoint.getEventHandler().register(new NetworkListener() {

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

        serverEndpoint.start(54321);
        clientEndpoint.connect("localhost", 54321);

        received = false;

        Thread.sleep(1500L);
    }

    @AfterClass
    public static void afterClass() {
        clientEndpoint.close();
        serverEndpoint.close();
    }

    @Test
    public void testReconnect() throws Exception {
        System.out.println("== Test Reconnect Behaviour == ");
        clientEndpoint.send(new EmptyRequest());
        clientEndpoint.closeChannel();
        System.out.println("Wait for disconnect");
        clientEndpoint.connect("localhost", 54321);
        System.out.println("Send Request");
        clientEndpoint.send(new EmptyRequest());
        Thread.sleep(500L);
        assertTrue(received);
        System.out.println("== Finished Test Reconnect Behaviour == ");
    }

}
