
package com.esotericsoftware.kryonetty.kryo;

import com.esotericsoftware.kryonetty.network.handler.NetworkEventManager;

public abstract class Endpoint implements EndpointInterface {

	private final KryoNetty kryoNetty;
	private final KryoSerialization kryoSerialization;
	private final NetworkEventManager networkEventManager;

	public Endpoint(KryoNetty kryoNetty) {
		this.kryoNetty = kryoNetty;
		this.kryoSerialization = new KryoSerialization(kryoNetty);
		this.networkEventManager = new NetworkEventManager();
	}

	@Override
	public KryoSerialization getKryoSerialization() {
		return this.kryoSerialization;
	}

	@Override
	public KryoNetty getKryoNetty() {
		return this.kryoNetty;
	}

	@Override
	public NetworkEventManager getEventHandler() {
		return this.networkEventManager;
	}
}
