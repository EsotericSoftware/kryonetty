
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

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientEndpointStressTest extends AbstractBenchmark {

    public static final TestRequest TEST_REQUEST = new TestRequest("I'm not rich, but maybe with tests", 52411, true, Arrays.asList("Test1", "Test2", "Test3", "Test4", "Test5"));

    public static ServerEndpoint serverEndpoint;
    public static ClientEndpoint clientEndpoint;

    public static AtomicInteger requestCounter;
    public static AtomicInteger requestAverage;

    @BeforeClass
    public static void setupClass() {

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
                if(event.getObject() instanceof TestRequest) {
                    requestCounter.getAndIncrement();
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

        requestCounter = new AtomicInteger();
        requestAverage = new AtomicInteger();
    }

    @AfterClass
    public static void afterClass() {
        clientEndpoint.close();
        serverEndpoint.close();
        System.out.println(requestAverage.get() + " packets/sec in average");
    }

    @Test
    public void testEmptyRequests1Sec() throws Exception {
        System.out.println("== Test Empty Request/Sec Behaviour == ");
        final long start = System.nanoTime();

        // More packets = Better Benchmarks
        // Trust me and test it

        int amount = 10_000;
        for (int i = 0; i < amount; i++) {
            clientEndpoint.send(TEST_REQUEST);
        }
        while(requestCounter.get() != amount) {

        }
        requestCounter.set(0);
        final long end = System.nanoTime();
        final long time = (end - start);
        int packetsPerSec = Math.round(amount / (time * (1 / 1000000000f)));
        if(requestAverage.get() != 0) {
            requestAverage.set(Math.round(packetsPerSec + requestAverage.get()) / 2);
        } else {
            requestAverage.set(packetsPerSec);
        }
        System.out.println(amount + "/" + requestCounter.get() + " successful in " + (time * (1 / 1000000000f)) + " seconds");
        System.out.println(packetsPerSec+ " packets/sec");
        System.out.println(requestAverage.get() + " packets/sec in average");
        requestCounter.set(0);
        System.out.println("== Finished Test Empty/Sec Behaviour == ");
    }

}
