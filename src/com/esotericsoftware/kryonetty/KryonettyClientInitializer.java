package com.esotericsoftware.kryonetty;

import com.esotericsoftware.kryo.Kryo;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class KryonettyClientInitializer extends ChannelInitializer<SocketChannel> {
	
	final Endpoint endpoint;
	
	private static Kryo CLIENT_KRYO;
   private static KryonettyClientHandler CLIENT_HANDLER;
   private static KryonettyDecoder DECODER;
   private static KryonettyEncoder ENCODER;
   
   public KryonettyClientInitializer(Endpoint endpoint) {
   	this.endpoint = endpoint;
   	CLIENT_KRYO = endpoint.getKryo();
   	CLIENT_HANDLER = new KryonettyClientHandler(endpoint);
   	DECODER = new KryonettyDecoder(CLIENT_KRYO);
   	ENCODER = new KryonettyEncoder(CLIENT_KRYO, 4 * 1024, 16 * 1024);
   }
	
   @Override
   public void initChannel(SocketChannel ch) throws Exception {
       ChannelPipeline p = ch.pipeline();
       
       //Add the logging, decoder, and encoder first
       p.addLast(new LoggingHandler(LogLevel.INFO));
       p.addLast(DECODER);
       p.addLast(ENCODER);

       // and then business logic.
       p.addLast(CLIENT_HANDLER);
   }
}