![KryoNetty](https://raw.github.com/wiki/EsotericSoftware/kryonetty/logo.jpg)

KryoNetty is a Java library that provides clean and simple API for efficient TCP client/server network communication using [Netty](http://netty.io/). KryoNetty uses the [Kryo serialization library](https://github.com/EsotericSoftware/kryo) to automatically and efficiently transfer object graphs across the network.

KryoNetty is very similar to [KryoNet](https://github.com/EsotericSoftware/kryonet), which does the exact same thing but using its own NIO-based networking. KryoNet also has more features than KryoNetty.

## Documentation

- [KryoNetty](#how-to-kryonetty-works)
- [Server](#how-to-start-the-server)
- [Client](#how-to-connect-the-client)
- [Events](#how-to-register-an-event)
- [Download](#add-as-dependecy)
- [Build](#build-from-source)
- [KryoSerialization](#why-kryoserialization)

## How to KryoNetty works 
`KryoNetty` passes the configuration of the different components to the classes & references behind it.

```java
    KryoNetty kryoNetty = new KryoNetty()
        .useLogging()
        .useExecution()
        .threadSize(16)
        .inputSize(4096)
        .outputSize(4096)
        .maxOutputSize(-1)
        .register(TestRequest.class);
```

Please note that Kryo gives every class an index-number. If you change the order, errors may occur if both programs do not have the same order of registered classes. To register more classes than one.

```java
    KryoNetty kryoNetty = new KryoNetty()
        .useLogging()
        .useExecution()
        .threadSize(16)
        .inputSize(4096)
        .outputSize(4096)
        .maxOutputSize(-1)
        .register(
            TestRequest.class,
            TestResponse.class,
            SomeClass.class,
            Random.class
        )
```

Or with index-ids:

```java
    KryoNetty kryoNetty = new KryoNetty()
        .useLogging()
        .useExecution()
        .threadSize(16)
        .inputSize(4096)
        .outputSize(4096)
        .maxOutputSize(-1)
        .register(100, TestRequest.class)
        .register(101, TestResponse.class)
        .register(102, SomeClass.class)
        .register(103, Random.class);
```

Every option returns the current instance of `KryoNetty`<br>
Current Options:
- `useLogging()` 
    - enables usage of `LoggingHandler.class`
    - default: false
- `useExecution()` 
    - enables usage of `EventExecutorGroup.class`
    - default: false
- `threadSize()` 
    - sets threads of `EventExecutorGroup.class` 
    - default: 0
- `inputSize()` 
    - sets buffer-size of `Input.class` 
    - default: 2048
- `outputSize()` 
    - sets buffer-size of `Output.class`
    - default: 2048
- `maxOutputSize()` 
    - sets max-buffer-size of `Output.class` 
    - default: -1

## How to start the server

To start the `Server`, the following call `start(int port)` is all you need. The `Server` need a configured `KryoNetty` instance as argument.

```java
    Server server = new Server(kryoNetty);
    server.start(56566);
```

## How to connect the client

The `Client` configuration works quite similar to the `Server`. The only difference are the `start(int port)` and `connect(String host, int port)` method-calls.

```java
    Client client = new Client(kryoNetty);
    client.connect("localhost", 56566);
```

## How to register an Event

In contrast to [KryoNet](https://github.com/EsotericSoftware/kryonet), the event system works a little differently. First of all there are the three following events:

- `ConnectEvent`
    - server: new client connects 
    - client: client connects to server
- `DisconnectEvent`
    - server: client disconnects (server-side)
    - client: client disconnects (client-side)
- `ReceiveEvent`
    - server: receives new object from client
    - client: receives new object from server 

Here are two possibilities to register a `Listener` on an `Event`:

Class implementing `Listener`-Interface (recommended)

```java
    public class ConnectionListener implements NetworkListener {
        @NetworkHandler
        public void onConnect(ConnectEvent connectEvent) {
            ChannelHandlerContext ctx = event.getCtx();
            System.out.println("Server: Client connected: " + ctx.channel().remoteAddress());
        }
    }

    // In main-method
    server.eventHandler().register(new ConnectionListener());
```


New `Listener`-Instance in `register(NetworkListener listener)`:

```java
    server.eventHandler().register(new NetworkListener() {
        @NetworkHandler
        public void onConnect(DisconnectEvent event) {
            ChannelHandlerContext ctx = event.getCtx();
            System.out.println("Server: Client disconnected: " + ctx.channel().remoteAddress());
        }
    });
```

Here an example to process an object which is fired via a `ReceiveEvent`.

```java

    public class ReceiveListener implements NetworkListener {
        @NetworkHandler
        public void onReceive(ReceiveEvent event) {
            ChannelHandlerContext ctx = event.getCtx();
            Object object = event.getObject();
            System.out.println("Server: Client received: " + ctx.channel().remoteAddress() + "/" + object);
            if(object instanceof Boolean) {
                Boolean result = (Boolean) object;
                System.out.println("Result is: " + result);
            }
        }
    }

```

## Add as dependency

First of all add `jitpack.io` as repository. 

```java
    repositories {
        maven { url 'https://jitpack.io' }
    }
```

After that you can add it as dependency.
```java
    dependencies {
        implementation 'com.github.EsotericSoftware:kryonetty:master-SNAPSHOT'
    }
```

## Build from source

If you want to build `kryonetty` from source, clone this repository and run `./gradlew shadowJar`. 
The output-file will be in the directory: `/build/libs/kryonetty-{version}-all.jar`
Gradle downloads the required dependencies and inserts all components into the output-file.

- [ShadowJar Plugin](https://imperceptiblethoughts.com/shadow)

## Why KryoSerialization

Why do we use Kryo and not for example the Netty ObjectEncoder? Quite simple. Kryo is noticeably faster and also easy to use. 
(Benchmark links:)
- [Kryo Benchmarks](https://github.com/EsotericSoftware/kryo#benchmarks)
- [JVM Serializers](https://github.com/eishay/jvm-serializers/wiki)

Since we work with a `Kryo`, `Input` & `Output` in a `Pool<?>` from `kryo-5.3.0`, classes are passed to the `KryoSerialization` constructor for registration & initialization.
For more documentation see `KryoSerialization.class`

Please use the [KryoNet discussion group](http://groups.google.com/group/kryonet-users) for [Kryo](https://github.com/EsotericSoftware/kryo)/[KryoNet](https://github.com/EsotericSoftware/kryonet)-specific support. <br>
