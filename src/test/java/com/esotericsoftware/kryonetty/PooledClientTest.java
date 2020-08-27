
package com.esotericsoftware.kryonetty;

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.esotericsoftware.kryonetty.kryo.KryoNetty;
import com.esotericsoftware.kryonetty.network.ConnectEvent;
import com.esotericsoftware.kryonetty.network.DisconnectEvent;
import com.esotericsoftware.kryonetty.network.ReceiveEvent;
import com.esotericsoftware.kryonetty.network.handler.NetworkHandler;
import com.esotericsoftware.kryonetty.network.handler.NetworkListener;
import com.esotericsoftware.kryonetty.pool.PooledClient;
import io.netty.channel.ChannelHandlerContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class PooledClientTest extends AbstractBenchmark {

    public static final TestRequest TEST_REQUEST = new TestRequest("I'm not rich, but maybe with tests", 52411, true, Arrays.asList("Test1", "Test2", "Test3", "Test4", "Test5"));

    public static Server server;
    public static PooledClient client;

    public static AtomicBoolean firstRound;
    public static AtomicInteger averageEmpty;
    public static AtomicInteger emptyCounter;
    public static AtomicInteger averageRequest;
    public static AtomicInteger requestCounter;

    @BeforeClass
    public static void setupClass() {

        KryoNetty kryoNetty = new KryoNetty()
                .useExecution()
                .threadSize(128)
                .inputSize(32_000)
                .outputSize(32_000)
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
                if(event.getObject() instanceof EmptyRequest)
                    emptyCounter.incrementAndGet();
                if(event.getObject() instanceof TestRequest)
                    requestCounter.incrementAndGet();
            }

            @NetworkHandler
            public void onDisconnect(DisconnectEvent event) {
                ChannelHandlerContext ctx = event.getCtx();
                System.out.println("Server: Client disconnected: " + ctx.channel().remoteAddress());
            }

        });


        client = new PooledClient("localhost", 54321, kryoNetty);
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

        emptyCounter = new AtomicInteger(0);
        averageEmpty = new AtomicInteger(0);
        requestCounter = new AtomicInteger(0);
        averageRequest = new AtomicInteger(0);
    }

    @AfterClass
    public static void afterClass() {
        client.close();
        server.close();
    }


    /*
    @Test
    public void testString() throws Exception {
        System.out.println("== Test String Behaviour == ");
        client.send("i like the way you do it right thurrrr");
        System.out.println("== Finished Test String Behaviour == ");
        Thread.sleep(500);
    }

    @Test
    public void testRequestClass() throws Exception {
        System.out.println("== Test Request Class Behaviour == ");
        client.send(TEST_REQUEST);
        System.out.println("== Finished Test Request Class Behaviour == ");
        Thread.sleep(500);
    }

    @Test
    public void testMapTenThousand() throws Exception {
        System.out.println("== Test Map (10_000) Behaviour == ");
        HashMap<String, TestRequest> hashMap = new HashMap<>();
        IntStream.range(0, 100_000).forEach(i -> hashMap.put("id#" + i, TEST_REQUEST));
        client.send(hashMap);
        System.out.println("== Finished Test Map (10_000) Behaviour == ");
        Thread.sleep(500);
    }

    @Test
    public void testMapAsBytes() throws Exception {
        System.out.println("== Test Map Bytes Behaviour == ");
        HashMap<String, TestRequest> hashMap = new HashMap<>();
        IntStream.range(0, 100_000).forEach(i -> hashMap.put("id#" + i, TEST_REQUEST));
        byte[] encoded = client.kryoSerialization().encodeToBytes(hashMap);
        client.send(encoded);
        System.out.println("== Finished Test Map Bytes Behaviour == ");
        Thread.sleep(500);
    }

    @Test
    public void testArrayAsBytes() throws Exception {
        System.out.println("== Test ArrayList Bytes Behaviour == ");
        ArrayList<TestRequest> hashMap = new ArrayList<>();
        IntStream.range(0, 10_0000).forEach(i -> hashMap.add(
                new TestRequest("tests" + new Random().nextLong(), new Random().nextLong(), true, Arrays.asList("Test1", "Test2", "Test3", "Test4", "Test5"))));
        byte[] encoded = client.kryoSerialization().encodeToBytes(hashMap);
        client.send(encoded);
        System.out.println("== Finished Test ArrayList Bytes Behaviour == ");
        Thread.sleep(500);
    }

    @Test
    public void testEmptyRequests() throws Exception {
        System.out.println("== Test Empty Request Behaviour == ");
        final long start = System.currentTimeMillis();
        IntStream.range(0, 100_000).forEach(i -> client.send(new EmptyRequest()));
        System.out.println("1Mio took " + (System.currentTimeMillis() - start) + "ms");
        System.out.println("== Finished Test Empty Request Behaviour == ");
        Thread.sleep(500);
    }*/

    @Test
    public void testEmptyRequests1Sec() throws Exception {
        System.out.println("== Test Empty Request/Sec Behaviour == ");
        final long start = System.nanoTime();
        int amount = 50_000;
        for (int i = 0; i < amount; i++) {
            client.send(new EmptyRequest(), true);
        }
        while(emptyCounter.get() != amount) {
            // Wait for all packets transfered
        }
        final long end = System.nanoTime();
        final long time = (end - start);
        int packetsPerSec = Math.round(amount / (time * (1 / 1000000000f)));
        if(averageEmpty.get() != 0) {
            averageEmpty.set(Math.round(packetsPerSec + averageEmpty.get()) / 2);
        } else {
            averageEmpty.set(packetsPerSec);
        }
        System.out.println(amount + "/" + emptyCounter.get() + " successful in " + (time * (1 / 1000000000f)) + " seconds");
        System.out.println(packetsPerSec+ " packets/sec");
        System.out.println(averageEmpty.get() + " packets/sec in average");
        emptyCounter.set(0);
        System.out.println("== Finished Test Empty/Sec Behaviour == ");
    }

    @AfterClass
    public static void summary() {
        System.out.println("Average: " + averageEmpty.get() + " empty packets/sec");
        System.out.println("Average: " + averageRequest.get() + " data packets/sec");
    }
}
