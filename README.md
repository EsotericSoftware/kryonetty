![KryoNetty](https://raw.github.com/wiki/EsotericSoftware/kryonetty/logo.jpg)

KryoNetty is a Java library that provides clean and simple API for efficient TCP and UDP client/server network communication using [Netty](http://netty.io/). KryoNetty uses the [Kryo serialization library](https://github.com/EsotericSoftware/kryo) to automatically and efficiently transfer object graphs across the network.

KryoNetty is very similar to [KryoNet](https://github.com/EsotericSoftware/kryonet), which does the exact same thing but using its own NIO-based networking. KryoNet has more features than KryoNetty, which in its current state mostly serves as a simple example of how Kryo can be used with Netty.

# Kryo Registration

Since we work with a `Kryo.class`, `Input.class` & `Output.class` pool from `kryo-5.0.0`, classes are passed to the `KryoHolder.class` constructor for registration. 
Example from `SimpleTest.class`:

<code>@Override<br>
public KryoHolder getKryoHolder() {<br>
    return new KryoHolder(-1, -1, String.class, TestRequest.class);<br>
    }<br>
</code>

Please use the [KryoNet discussion group](http://groups.google.com/group/kryonet-users) for support.
