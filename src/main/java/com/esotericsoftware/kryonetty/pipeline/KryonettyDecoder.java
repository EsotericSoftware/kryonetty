package com.esotericsoftware.kryonetty.pipeline;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryonetty.kryo.Endpoint;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.io.ByteArrayInputStream;
import java.util.List;

public class KryonettyDecoder extends ByteToMessageDecoder {

	private final Endpoint endpoint;

	public KryonettyDecoder (Endpoint endpoint) {
		this.endpoint = endpoint;
	}

	@Override
	protected void decode (ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) {

		if (buffer.readableBytes() < 2)
			return;

		buffer.markReaderIndex();

		int contentLength = buffer.readInt();

		if (buffer.readableBytes() < contentLength) {
			buffer.resetReaderIndex();
			return;
		}

		byte[] buf = new byte[contentLength];
		buffer.readBytes(buf);

		Kryo kryo = endpoint.kryoSerialization().getKryo();
		Input input = endpoint.kryoSerialization().getInput();
		input.setInputStream(new ByteArrayInputStream(buf));
		Object object = kryo.readClassAndObject(input);
		input.close();
		endpoint.kryoSerialization().freeKryo(kryo);
		endpoint.kryoSerialization().freeInput(input);
		out.add(object);
	}
}
