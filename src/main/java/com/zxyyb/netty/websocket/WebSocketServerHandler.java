package com.zxyyb.netty.websocket;

import java.util.Date;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderUtil;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;

public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {

	private WebSocketServerHandshaker handShaker;

	@Override
	protected void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {

		if (msg instanceof FullHttpRequest) {
			// 传统的 http 接入
			handleHttpRequest(ctx, (FullHttpRequest) msg);

		} else if (msg instanceof WebSocketFrame) {
			// WebSocket 接入
			handleWebSocketFrame(ctx, (WebSocketFrame) msg);
		}

	}

	// ===========WebSocket 接入
	private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {

		if (frame instanceof CloseWebSocketFrame) {
			// 关闭链路指令
			handShaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
			return;
		} else if (frame instanceof PingWebSocketFrame) {
			// ping 消息
			ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
			return;
		} else if (!(frame instanceof TextWebSocketFrame)) {
			// 二进制消息 这里不处理
			throw new UnsupportedOperationException(String.format("%s frame types not supported.", frame.getClass().getName()));
		}

		// 返回应答消息 ， 构建新的TextWebSocketFrame 消息返回给客户端。
		String body = ((TextWebSocketFrame) frame).text();
		ctx.channel().write(new TextWebSocketFrame(body + " , Welcome ! " + new Date().toString()));

	}

	// ===============传统的 http 接入
	private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) {

		if (!request.decoderResult().isSuccess() || !"websocket".equals(request.headers().get("Upgrade"))) {

			sendHttpResponse(ctx, request, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
			return;
		}

		String webSocketURL = "ws:http://localhost:8088/websocket";
		//构建握手工厂,通过他构建握手相应消息返回客户端，同时动态将编码和解码类动态添加到ChannelPipeline中。
		WebSocketServerHandshakerFactory shakerFactory = new WebSocketServerHandshakerFactory(webSocketURL, null, false);
		handShaker = shakerFactory.newHandshaker(request);
		if (handShaker == null)
			WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
		else
			handShaker.handshake(ctx.channel(), request);
	}

	private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest request, FullHttpResponse response) {

		// 返回应答给客户端
		if (response.status().code() != 200) {
			ByteBuf buf = Unpooled.copiedBuffer(response.status().toString(), CharsetUtil.UTF_8);
			response.content().writeBytes(buf);
			buf.release();
			HttpHeaderUtil.setContentLength(response, response.content().readableBytes());
		}

		// 如果是非keep-alive ,关闭连接
		ChannelFuture cf = ctx.channel().writeAndFlush(response);
		if (!HttpHeaderUtil.isKeepAlive(request) || response.status().code() != 200)
			cf.addListener(ChannelFutureListener.CLOSE);
	}

	// --------------------
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

}
