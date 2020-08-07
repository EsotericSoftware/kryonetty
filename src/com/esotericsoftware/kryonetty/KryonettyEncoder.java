package com.esotericsoftware.kryonetty;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.ByteArrayOutputStream;

public class KryonettyEncoder extends MessageToByteEncoder {

	private final Endpoint endpoint;

	public KryonettyEncoder (Endpoint endpoint) {
		this.endpoint = endpoint;
	}

	protected void encode (ChannelHandlerContext ctx, Object object, ByteBuf buffer)
			throws Exception {
		Kryo kryo = endpoint.getKryoHolder().getKryo();
		Output output = endpoint.getKryoHolder().getOutput();

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		output.setOutputStream(outStream);

		kryo.writeClassAndObject(output, object);
		output.flush();
		output.close();

		byte[] outArray = outStream.toByteArray();
		buffer.writeShort(outArray.length);
		buffer.writeBytes(outArray);
		endpoint.getKryoHolder().freeOutput(output);
		endpoint.getKryoHolder().freeKryo(kryo);
	}
}
