
package com.esotericsoftware.kryonetty.kryo;

/** @author Nathan Sweet */
public abstract class Endpoint implements EndpointReceiver {

	private final KryoOptions kryoOptions;
	private final EndpointOptions endpointOptions;
	private final KryoHolder kryoHolder;

	public Endpoint(KryoOptions kryoOptions, EndpointOptions endpointOptions) {
		this.kryoOptions = kryoOptions;
		this.endpointOptions = endpointOptions;
		this.kryoHolder = new KryoHolder(this);
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
}
