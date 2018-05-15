package com.zxyyb.netty.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

public class WebSocketServer {

	private static final int port = 8088;

	private static final String host = "192.168.48.1";
	
	private static final int maxContentLength = 65536;

	public static void main(String[] args) {

		new WebSocketServer().run();
		
	}

	private void run() {

		EventLoopGroup boss = new NioEventLoopGroup();
		EventLoopGroup worker = new NioEventLoopGroup();

		try {

			ServerBootstrap sb = new ServerBootstrap();
			sb.group(boss, worker).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {

				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					
					//将请求和应答消息比那吗或者解码为http消息
					ch.pipeline().addLast("http-codec",new HttpServerCodec());
					//多个消息组合成完整的消息
					ch.pipeline().addLast("aggregator",new HttpObjectAggregator(maxContentLength));
					//向客户端发送HTML5文件，它主要用于支持浏览器和服务端进行WebSocket 通信
					ch.pipeline().addLast("http-chunked",new ChunkedWriteHandler());
					
					ch.pipeline().addLast("handler",new WebSocketServerHandler());
				}

			});

			ChannelFuture cf = sb.bind(host, port).sync();
			
			System.out.println("websocked server started.");
			
			cf.channel().closeFuture().sync();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			boss.shutdownGracefully();
			worker.shutdownGracefully();
		}

	}

}
