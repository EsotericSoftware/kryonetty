package com.esotericsoftware.kryonetty;

import io.netty.channel.ChannelHandlerContext;

public interface Listener {

	void connected (ChannelHandlerContext ctx);

	void disconnected (ChannelHandlerContext ctx);

	void received (ChannelHandlerContext ctx, Object object);

}
