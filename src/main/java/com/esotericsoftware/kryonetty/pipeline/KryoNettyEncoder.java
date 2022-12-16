package com.esotericsoftware.kryonetty.pipeline;

import com.esotericsoftware.kryonetty.kryo.Endpoint;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class KryoNettyEncoder extends MessageToByteEncoder<Object> {

	private final Endpoint endpoint;

	public KryoNettyEncoder(Endpoint endpoint) {
		this.endpoint = endpoint;
	}

	protected void encode (ChannelHandlerContext ctx, Object object, ByteBuf out) {
		// Get object-encoding method from KryoSerialization
		byte[] objectBytes = endpoint.getKryoSerialization().encodeObject(object);

		// Write the length to the output-buffer
		out.writeInt(objectBytes.length);
		// Write the content data to the output-buffer
		out.writeBytes(objectBytes);
	}
}
