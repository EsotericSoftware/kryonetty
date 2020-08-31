package com.esotericsoftware.kryonetty.kryo;

import com.esotericsoftware.kryonetty.network.handler.NetworkEventManager;

public interface EndpointInterface {

    /**
     * @return Returns the type // server or client
     */
    Endpoint.Type getType();

    /**
     * @return Returns the KryoNetty instance
     */
    KryoNetty getKryoNetty();

    /**
     * @return Returns the KryoSerialization instance
     */
    KryoSerialization getKryoSerialization();

    /**
     * @return Returns the NetworkEventManager instance
     */
    NetworkEventManager getEventHandler();

    enum Type { CLIENT, SERVER }
}
