package com.esotericsoftware.kryonetty.kryo;

import com.esotericsoftware.kryonetty.network.handler.NetworkEventManager;

public interface EndpointReceiver {

    Endpoint.Type type();

    KryoNetty kryoNetty();

    KryoSerialization kryoHolder();

    NetworkEventManager eventHandler();

    enum Type { CLIENT, SERVER }
}