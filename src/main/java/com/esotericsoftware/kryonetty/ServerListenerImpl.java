package com.esotericsoftware.kryonetty;

import com.esotericsoftware.kryonetty.kryo.EndpointOptions;
import com.esotericsoftware.kryonetty.kryo.KryoOptions;
import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of Server that uses the listener model as per KryoNet.
 *
 * @author Philip Whitehouse
 */
public class ServerListenerImpl extends Server {

    private List<Listener> listeners;


    public ServerListenerImpl(KryoOptions options, EndpointOptions endpointOptions) {
        super(options, endpointOptions);
        listeners = new ArrayList<Listener>();
    }

    public void addListener(Listener l) {
        listeners.add(l);
    }

    @Override
    public void connected(ChannelHandlerContext ctx) {
        for (Listener listener : listeners) {
            listener.connected(ctx);
        }
    }

    @Override
    public void disconnected(ChannelHandlerContext ctx) {
        for (Listener listener : listeners) {
            listener.disconnected(ctx);
        }
    }

    @Override
    public void received(ChannelHandlerContext ctx, Object object) {
        for (Listener listener : listeners) {
            listener.received(ctx, object);
        }
    }

}
