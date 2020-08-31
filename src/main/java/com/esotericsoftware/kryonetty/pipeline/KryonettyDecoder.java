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
	protected void decode (ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {

		if (in.readableBytes() < 2)
			return;

		in.markReaderIndex();

		int contentLength = in.readInt();

		if (in.readableBytes() < contentLength) {
			in.resetReaderIndex();
			return;
		}

		byte[] buf = new byte[contentLength];
		in.readBytes(buf);

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
