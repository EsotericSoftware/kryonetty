![KryoNetty](https://raw.github.com/wiki/EsotericSoftware/kryonetty/logo.jpg)

KryoNetty is a Java library that provides clean and simple API for efficient TCP and UDP client/server network communication using [Netty](http://netty.io/). KryoNetty uses the [Kryo serialization library](https://github.com/EsotericSoftware/kryo) to automatically and efficiently transfer object graphs across the network.

KryoNetty is very similar to [KryoNet](https://github.com/EsotericSoftware/kryonet), which does the exact same thing but using its own NIO-based networking. KryoNet has more features than KryoNetty, which in its current state mostly serves as a simple example of how Kryo can be used with Netty.

## Table of content

- [KryoNetty](#how-to-kryonetty-works)
- [Server](#how-to-start-the-server)
- [Client](#how-to-connect-the-client)
- [Events](#how-to-register-an-event)


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

To register more classes than one:

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


Every option returns the current instance of `KryoNetty`
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

To start the `Server`, the following `start(int port)` is all you need. The `Server` waits for the configured `KryoNetty` instance as argument.

```java
    Server server = new Server(kryoNetty);
    server.start(56566);
```

If you want to let the `Server` run in another thread, use the `ThreadedServer`. This starts the server in a `Executors.newSingleThreadExecutor()`. Simply call:

```java
    Server server = new ThreadedServer(kryoNetty);
    server.start(56566);
```




## How to connect the client

The `Client` configuration works similar to the `Server` configuration. The only difference are the `start(int port)` and `connect(String host, int port)` methods.

```java
    Client client = new Client(kryoNetty);
    client.connect("localhost", 56566);
```

Same way with the `ThreadedClient`

```java
    Client client = new ThreadedClient(kryoNetty);
    client.connect("localhost", 56566);
```




## How to register an Event

In contrast to [KryoNet](https://github.com/EsotericSoftware/kryonet), the event system works a little differently. First of all there are the following events:

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

Own `Listener`-Class

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
        public void onConnect(ConnectEvent event) {
            ChannelHandlerContext ctx = event.getCtx();
            System.out.println("Server: Client connected: " + ctx.channel().remoteAddress());
            ctx.channel().write("make a programmer rich");
        }
    });
```

## KryoSerialization

Since we work with a `Kryo`, `Input` & `Output` in a `Pool<?>` from `kryo-5.0.0`, classes are passed to the `KryoSerialization` constructor for registration & initialization.
Here for example `KryoNetty` is used to pass the parameters `inputBufferSize`, `outputBufferSize`, `maxOutputBufferSize` & classes, which should be registered. 

Please use the [KryoNet discussion group](http://groups.google.com/group/kryonet-users) for [Kryo](https://github.com/EsotericSoftware/kryo)/[KryoNet](https://github.com/EsotericSoftware/kryonet)-specific support. <br>
Use the [LevenProxy Discord Server](https://discord.levenproxy.eu/) for `KryoNetty-dev` support.
