package com.esotericsoftware.kryonetty.netty;

import com.esotericsoftware.kryonetty.kryo.Endpoint;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class KryonettyHandler extends SimpleChannelInboundHandler<Object> {

	final Endpoint endpoint;

	public KryonettyHandler(Endpoint endpoint) {
		this.endpoint = endpoint;
	}

	@Override
   public void channelActive(ChannelHandlerContext ctx) throws Exception {
       super.channelActive(ctx);
       endpoint.connected(ctx);
   }
	 
	 @Override
	 public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		 super.channelInactive(ctx);
		 endpoint.disconnected(ctx);
	 }

   @Override
   public void channelRead0(ChannelHandlerContext ctx, Object msg) {
  	 endpoint.received(ctx, msg);
   }

   @Override
   public void channelReadComplete(ChannelHandlerContext ctx) {
      ctx.flush();
   }

   @Override
   public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
  	 super.exceptionCaught(ctx, cause);
   }
}