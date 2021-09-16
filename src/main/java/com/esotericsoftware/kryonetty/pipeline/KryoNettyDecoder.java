package com.esotericsoftware.kryonetty.pipeline;

import com.esotericsoftware.kryonetty.kryo.Endpoint;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class KryoNettyDecoder extends ByteToMessageDecoder {

	private final Endpoint endpoint;

	public KryoNettyDecoder(Endpoint endpoint) {
		this.endpoint = endpoint;
	}

	@Override
	protected void decode (ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {

		// Get sure bytes are more than 2
		if (in.readableBytes() < 2)
			return;

		// mark the reader index, to get back to it.
		in.markReaderIndex();

		// read the content data length
		int contentLength = in.readInt();

		// be sure the content data length doesn't exceeds the readable bytes
		if (in.readableBytes() < contentLength) {
			in.resetReaderIndex();
			return;
		}

		// create a new byte[] for decoding
		byte[] objectBytes = new byte[contentLength];
		// read the ByteBuf-data into the new byte[]
		in.readBytes(objectBytes);

		// Get object-decoding methods from KryoSerialization
		Object object = endpoint.getKryoSerialization().decodeObject(objectBytes);

		// add the object to the output-list
		out.add(object);
	}
}
