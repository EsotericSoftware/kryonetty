package com.esotericsoftware.kryonetty;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class KryonettyEncoder extends MessageToByteEncoder {
	private final Output output;
	private final Kryo kryo;

	public KryonettyEncoder (Kryo kryo, int bufferSize, int maxBufferSize) {
		this.kryo = kryo;
		output = new Output(bufferSize, maxBufferSize);
	}

	protected void encode (ChannelHandlerContext ctx, Object object, ByteBuf buffer)
			throws Exception {
		output.clear();
		output.setPosition(4);
		kryo.writeClassAndObject(output, object);
		int total = output.position();
		output.setPosition(0);
		output.writeInt(total - 4);
		buffer.writeBytes(Unpooled.wrappedBuffer(output.getBuffer(), 0, total));
	}
}
