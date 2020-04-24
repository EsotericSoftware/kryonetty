package com.esotericsoftware.kryonetty;

import java.net.InetAddress;
import java.util.Date;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class KryonettyServerHandler extends SimpleChannelInboundHandler<Object> {
	
	final Endpoint endpoint;
	
	public KryonettyServerHandler(Endpoint endpoint) {
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