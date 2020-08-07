
package com.esotericsoftware.kryonetty;

import com.esotericsoftware.kryonetty.kryo.EndpointOptions;
import com.esotericsoftware.kryonetty.kryo.KryoOptions;
import io.netty.channel.ChannelHandlerContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SimpleTest {

    private static final TestRequest TEST_REQUEST = new TestRequest("I'm not rich. but maybe with tests", 52411, true, Arrays.asList("Test1", "Test2", "Test3", "Test4", "Test5"));

    protected static boolean testRequestReceived;
    private static Server server;
    private static Client client;

    @BeforeClass
    public static void setupClass() {

        KryoOptions kryoOptions = new KryoOptions()
                .inputSize(4096)
                .outputSize(4096)
                .register(TestRequest.class);

        EndpointOptions endpointOptions = new EndpointOptions()
                .useLogging()
                .useExecution()
                .threadSize(16);

        server = new Server(kryoOptions, endpointOptions) {
            public void connected(ChannelHandlerContext ctx) {
                System.out.println("Server: Client connected: " + ctx.channel().remoteAddress());
                ctx.channel().write("make a programmer rich");
            }

            public void disconnected(ChannelHandlerContext ctx) {
                System.out.println("Server: Client disconnected: " + ctx.channel().remoteAddress());
            }

            public void received(ChannelHandlerContext ctx, Object object) {
                System.out.println("Server: Received: " + object);
                if (object instanceof TestRequest) {
                    testRequestReceived = true;
                    TestRequest request = (TestRequest) object;
                    try {
                        send(ctx.channel(), request);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        };
        client = new Client(kryoOptions, endpointOptions) {
            public void connected(ChannelHandlerContext ctx) {
                System.out.println("Client: Connected to server: " + ctx.channel().remoteAddress());
            }

            public void disconnected(ChannelHandlerContext ctx) {
                System.out.println("Client: Disconnected from server: " + ctx.channel().remoteAddress());
            }

            public void received(ChannelHandlerContext ctx, Object object) {
                System.out.println("Client: Received: " + object);
                if(object instanceof TestRequest) {
                    TestRequest request = (TestRequest) object;
                    assertEquals(request.someText, TEST_REQUEST.someText);
                    assertEquals(request.someLong, TEST_REQUEST.someLong);
                    assertEquals(request.someBoolean, TEST_REQUEST.someBoolean);
                    assertEquals(request.someList, TEST_REQUEST.someList);
                }
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
