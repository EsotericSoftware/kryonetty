package com.esotericsoftware.kryonetty;

import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of Server that uses the listener model as per KryoNet.
 * @author Philip Whitehouse
 *
 */
public class ServerListenerImpl extends Server {

	private final KryoHolder kryoHolder;
	private List<Listener> listeners;


	public ServerListenerImpl () {
		super();
		kryoHolder = new KryoHolder(32768, 32768, null);
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
	public KryoHolder getKryoHolder() {
		return kryoHolder;
	}

}
