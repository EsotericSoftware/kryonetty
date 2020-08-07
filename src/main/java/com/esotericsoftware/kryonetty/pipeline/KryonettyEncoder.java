package com.esotericsoftware.kryonetty.pipeline;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryonetty.kryo.Endpoint;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.ByteArrayOutputStream;

public class KryonettyEncoder extends MessageToByteEncoder {

	private final Endpoint endpoint;

	public KryonettyEncoder (Endpoint endpoint) {
		this.endpoint = endpoint;
	}

	protected void encode (ChannelHandlerContext ctx, Object object, ByteBuf buffer) {
		Kryo kryo = endpoint.kryoHolder().getKryo();
		Output output = endpoint.kryoHolder().getOutput();

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		output.setOutputStream(outStream);

		kryo.writeClassAndObject(output, object);
		output.flush();
		output.close();

		byte[] outArray = outStream.toByteArray();
		buffer.writeShort(outArray.length);
		buffer.writeBytes(outArray);
		endpoint.kryoHolder().freeOutput(output);
		endpoint.kryoHolder().freeKryo(kryo);
	}
}
