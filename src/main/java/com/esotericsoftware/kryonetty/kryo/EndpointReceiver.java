package com.esotericsoftware.kryonetty.kryo;

import com.esotericsoftware.kryonetty.network.handler.NetworkEventManager;

public interface EndpointReceiver {

    Endpoint.Type type();

    KryoNetty kryoNetty();

    KryoSerialization kryoSerialization();

    NetworkEventManager eventHandler();

    enum Type { CLIENT, SERVER }
}
