
package com.esotericsoftware.kryonetty.kryo;

import com.esotericsoftware.kryonetty.network.handler.NetworkEventManager;

/** @author Nathan Sweet */
public abstract class Endpoint implements EndpointReceiver {

	private final KryoOptions kryoOptions;
	private final EndpointOptions endpointOptions;
	private final KryoHolder kryoHolder;
	private final NetworkEventManager networkEventManager;

	public Endpoint(KryoOptions kryoOptions, EndpointOptions endpointOptions) {
		this.kryoOptions = kryoOptions;
		this.endpointOptions = endpointOptions;
		this.kryoHolder = new KryoHolder(this);
		this.networkEventManager = new NetworkEventManager();
	}

	@Override
	public KryoOptions kryoOptions() {
		return this.kryoOptions;
	}

	@Override
	public KryoHolder kryoHolder() {
		return this.kryoHolder;
	}

	@Override
	public EndpointOptions endpointOptions() {
		return this.endpointOptions;
	}

	@Override
	public NetworkEventManager eventHandler() {
		return this.networkEventManager;
	}
}
