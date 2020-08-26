
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

public class TestEmptyRequest extends AbstractBenchmark {

    public static final TestRequest TEST_REQUEST = new TestRequest("I'm not rich, but maybe with tests", 52411, true, Arrays.asList("Test1", "Test2", "Test3", "Test4", "Test5"));

    public static Server server;
    public static Client client;

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

            }

            @NetworkHandler
            public void onDisconnect(DisconnectEvent event) {
                ChannelHandlerContext ctx = event.getCtx();
                System.out.println("Server: Client disconnected: " + ctx.channel().remoteAddress());
            }

        });


        client = new Client(kryoNetty);
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
        int amount = 25_000;
        for (int i = 0; i < amount; i++) {
            client.send(new EmptyRequest(), true);
        }
        final long end = System.nanoTime();
        System.out.println(amount / ((end - start) * (1 / 1000000000f)) + " packets/sec (" + amount + " packets took " + ((end - start) * (1 / 1000000000f)) + " seconds)");
        System.out.println("== Finished Test Empty/Sec Behaviour == ");
    }

    @Test
    public void testTestRequests1Sec() throws Exception {
        System.out.println("== Test TestRequest/Sec Behaviour == ");
        final long start = System.nanoTime();
        int amount = 25_000;
        for (int i = 0; i < amount; i++) {
            client.send(TEST_REQUEST, true);
        }
        final long end = System.nanoTime();
        System.out.println(amount / ((end - start) * (1 / 1000000000f)) + " packets/sec (" + amount + " packets took " + ((end - start) * (1 / 1000000000f)) + " seconds)");
        System.out.println("== Finished TestRequest/Sec Behaviour == ");
    }

    @Test
    public void testTestRequestMap1Sec() throws Exception {
        System.out.println("== Test TestRequestMap/Sec Behaviour == ");
        HashMap<String, EmptyRequest> hashMap = new HashMap<>();
        IntStream.range(0, 10_000).forEach(i -> hashMap.put("id#" + i, new EmptyRequest()));
        final long start = System.nanoTime();
        int amount = 5_000;
        for (int i = 0; i < amount; i++) {
            client.send(hashMap, true);
        }
        final long end = System.nanoTime();
        System.out.println(amount / ((end - start) * (1 / 1000000000f)) + " packets/sec (" + amount + " packets took " + ((end - start) * (1 / 1000000000f)) + " seconds)");
        System.out.println("== Finished TestRequestMap/Sec Behaviour == ");
    }
}
