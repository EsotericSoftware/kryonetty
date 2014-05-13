
package com.esotericsoftware.kryonetty;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;

/** Creates a new ChannelPipeline for Channel, which handles incoming and outgoing messages.
 * @author Nathan Sweet */
public class KryoChannelPipelineFactory implements ChannelPipelineFactory {
	final Endpoint endpoint;
	private Executor executor = new OrderedMemoryAwareThreadPoolExecutor(10, 0, 0, 60, TimeUnit.SECONDS);

	public KryoChannelPipelineFactory (Endpoint endpoint) {
		this.endpoint = endpoint;
	}

	public ChannelPipeline getPipeline () throws Exception {
		Kryo kryo = endpoint.newKryo();
		ChannelPipeline pipeline = Channels.pipeline();
		pipeline.addLast("decoder", new KryoDecoder(kryo));
		pipeline.addLast("encoder", new KryoEncoder(kryo, 4 * 1024, 16 * 1024));
		pipeline.addLast("executor", new ExecutionHandler(executor));
		pipeline.addLast("business", new SimpleChannelUpstreamHandler() {
			public void channelConnected (ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
				endpoint.connected(ctx);
			}

			public void channelDisconnected (ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
				endpoint.disconnected(ctx);
			}

			public void messageReceived (ChannelHandlerContext ctx, MessageEvent e) throws Exception {
				endpoint.received(ctx, e.getMessage());
			}
		});
		return pipeline;
	}

	static private class KryoDecoder extends FrameDecoder {
		private final Kryo kryo;
		private final Input input = new Input();
		private int length = -1;

		public KryoDecoder (Kryo kryo) {
			this.kryo = kryo;
		}

		protected Object decode (ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {
			input.setBuffer(buffer.array(), buffer.readerIndex(), buffer.readableBytes());
			if (length == -1) {
				// Read length.
				if (buffer.readableBytes() < 4) return null;
				length = input.readInt();
				buffer.readerIndex(input.position());
			}
			if (buffer.readableBytes() < length) return null;
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
			return object;
		}
	}

	static private class KryoEncoder extends OneToOneEncoder {
		private final Output output;
		private final Kryo kryo;

		public KryoEncoder (Kryo kryo, int bufferSize, int maxBufferSize) {
			this.kryo = kryo;
			output = new Output(bufferSize, maxBufferSize);
		}

		protected Object encode (ChannelHandlerContext ctx, Channel channel, Object object) throws Exception {
			output.clear();
			output.setPosition(4);
			kryo.writeClassAndObject(output, object);
			int total = output.position();
			output.setPosition(0);
			output.writeInt(total - 4);
			return ChannelBuffers.wrappedBuffer(output.getBuffer(), 0, total);
		}
	}
}
