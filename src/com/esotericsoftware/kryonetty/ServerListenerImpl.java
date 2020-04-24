package com.esotericsoftware.kryonetty;

import java.util.ArrayList;
import java.util.List;

import com.esotericsoftware.kryo.Kryo;

import io.netty.channel.ChannelHandlerContext;

/**
 * An implementation of Server that uses the listener model as per KryoNet.
 * @author Philip Whitehouse
 *
 */
public class ServerListenerImpl extends Server {
	private List<Listener> listeners;
	private Kryo kryo;


	public ServerListenerImpl () {
		super();
		listeners = new ArrayList<Listener>();
	}
	
	public void addListener(Listener l) {
		listeners.add(l);
	}
	
	@Override
	public void connected (ChannelHandlerContext ctx) {
		for(Listener listener : listeners) {
			listener.connected(ctx);
		}
	}

	@Override
	public void disconnected (ChannelHandlerContext ctx) {
		for(Listener listener : listeners) {
			listener.disconnected(ctx);
		}
	}

	@Override
	public void received (ChannelHandlerContext ctx, Object object) {
		for(Listener listener : listeners) {
			listener.received(ctx, object);
		}
	}

	@Override
	public Kryo getKryo () {
		return kryo;
	}

}
