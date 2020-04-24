package com.esotericsoftware.kryonetty;

import java.util.List;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

import io.netty.buffer.AbstractByteBuf;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class KryonettyDecoder extends ByteToMessageDecoder {
	private final Kryo kryo;
	private final Input input = new Input();
	private int length = -1;

	public KryonettyDecoder (Kryo kryo) {
		this.kryo = kryo;
	}

	@Override
	protected void decode (ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out)
			throws Exception {
		//Netty 4.1 apparently does not like ByteBuf's "array()" being used for direct buffers,
		//   so a workaround is necessary to extract the byte array needed by the Input class
		byte[] byteArray = new byte[buffer.readableBytes()];
		buffer.readBytes(byteArray);
		buffer.resetReaderIndex();
		
		input.setBuffer(byteArray, buffer.readerIndex(), buffer.readableBytes());
		if (length == -1) {
			// Read length.
			if (buffer.readableBytes() < 4) return;
			length = input.readInt();
			buffer.readerIndex(input.position());
		}
		if (buffer.readableBytes() < length) return;
		length = -1;
		Object object = kryo.readClassAndObject(input);
		// Dumps out bytes for JMeter's TCP Sampler (BinaryTCPClientImpl classname):
		// System.out.println("--");
		// for (int i = buffer.readerIndex() - 4; i < input.position(); i++) {
		// String hex = Integer.toHexString(input.getBuffer()[i] & 0xff);
		// if (hex.length() == 1) hex = "0" + hex;
		// System.out.print(hex.toUpperCase());
		// }
		// System.out.println("\n--");
		buffer.readerIndex(input.position());
		out.add(object);
	}
}
