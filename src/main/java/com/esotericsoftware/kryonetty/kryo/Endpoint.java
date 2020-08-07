
package com.esotericsoftware.kryonetty.kryo;

import com.esotericsoftware.kryonetty.network.handler.NetworkEventManager;

public abstract class Endpoint implements EndpointReceiver {

	private final KryoNetty kryoNetty;
	private final KryoHolder kryoHolder;
	private final NetworkEventManager networkEventManager;

	public Endpoint(KryoNetty kryoNetty) {
		this.kryoNetty = kryoNetty;
		this.kryoHolder = new KryoHolder(kryoNetty);
		this.networkEventManager = new NetworkEventManager();
	}

	@Override
	public KryoHolder kryoHolder() {
		return this.kryoHolder;
	}

	@Override
	public KryoNetty kryoNetty() {
		return this.kryoNetty;
	}

	@Override
	public NetworkEventManager eventHandler() {
		return this.networkEventManager;
	}
}
