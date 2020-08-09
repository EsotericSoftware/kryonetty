package com.esotericsoftware.kryonetty.kryo;

import com.esotericsoftware.kryonetty.network.handler.NetworkEventManager;

public interface EndpointReceiver {

    /**
     * @return Gives the type server or client
     */
    Endpoint.Type type();

    /**
     * @return Give to the type server or client
     */
    KryoNetty kryoNetty();

    /**
     * @return Returns the KryoSerialization instance
     */
    KryoSerialization kryoSerialization();

    /**
     * @return Returns the NetworkEventManager instance
     */
    NetworkEventManager eventHandler();

    enum Type { CLIENT, SERVER }
}
