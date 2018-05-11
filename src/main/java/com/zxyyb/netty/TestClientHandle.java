package com.zxyyb.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class TestClientHandle extends ChannelHandlerAdapter {

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {

		for (int i = 0; i < 50; i++) {
			byte[] req = ("QUERY TIME" + i).getBytes();
			ByteBuf firstMessage = Unpooled.buffer(req.length);
			firstMessage.writeBytes(req);

			ctx.writeAndFlush(firstMessage);
		}

	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf buf = (ByteBuf) msg;
		byte[] req = new byte[buf.readableBytes()];
		buf.readBytes(req);
		
		String body = new String(req, "UTF-8");
		System.out.println(" Server callback : " + body);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		// 释放资源
		ctx.close();
	}

}
