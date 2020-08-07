package com.esotericsoftware.kryonetty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class KryonettyClientInitializer extends ChannelInitializer<SocketChannel> {

    private final Endpoint endpoint;
    private final KryonettyClientHandler clientHandler;

   public KryonettyClientInitializer(Endpoint endpoint) {
   	this.endpoint = endpoint;
   	clientHandler = new KryonettyClientHandler(endpoint);
   }
	
   @Override
   public void initChannel(SocketChannel ch) throws Exception {
       ChannelPipeline p = ch.pipeline();
       
       //Add the logging, decoder, and encoder first
       p.addLast(new LoggingHandler(LogLevel.INFO));
       p.addLast(new KryonettyDecoder(endpoint));
       p.addLast(new KryonettyEncoder(endpoint));

       // and then business logic.
       p.addLast(clientHandler);
   }
}