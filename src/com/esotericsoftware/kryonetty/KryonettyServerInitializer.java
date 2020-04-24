package com.esotericsoftware.kryonetty;

import com.esotericsoftware.kryo.Kryo;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class KryonettyServerInitializer extends ChannelInitializer<SocketChannel> {
	
	final Endpoint endpoint;
	
	private static Kryo SERVER_KRYO;
   private static KryonettyServerHandler SERVER_HANDLER;
   private static KryonettyDecoder DECODER;
   private static KryonettyEncoder ENCODER;
	
   public KryonettyServerInitializer(Endpoint endpoint) {
   	this.endpoint = endpoint;
   	SERVER_KRYO = endpoint.getKryo();
   	SERVER_HANDLER = new KryonettyServerHandler(endpoint);
   	DECODER = new KryonettyDecoder(SERVER_KRYO);
   	ENCODER = new KryonettyEncoder(SERVER_KRYO, 4 * 1024, 16 * 1024);
   }
   
   @Override
   public void initChannel(SocketChannel ch) throws Exception {
       ChannelPipeline pipeline = ch.pipeline();

       // Add the text line codec combination first,
       pipeline.addLast(new LoggingHandler(LogLevel.INFO));
       // the encoder and decoder are static as these are sharable
       pipeline.addLast(DECODER);
       pipeline.addLast(ENCODER);

       // and then business logic.
       pipeline.addLast(new KryonettyServerHandler(endpoint));
   }
}