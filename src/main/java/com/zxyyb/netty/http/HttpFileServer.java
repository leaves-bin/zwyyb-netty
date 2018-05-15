package com.zxyyb.netty.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

public class HttpFileServer {

	private static final int port = 8088;

	private static final String host = "192.168.48.1";

	private static final String rootpath = "E://ace-master//";
	
	private static final int maxContentLength = 65536;

	public void run() {

		EventLoopGroup boss = new NioEventLoopGroup();
		EventLoopGroup worker = new NioEventLoopGroup();

		try {

			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(boss, worker).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {

				@Override
				protected void initChannel(SocketChannel ch) throws Exception {

					//http请求消息解码器,它负责把字节解码成Http请求
					ch.pipeline().addLast("http-decoder",new HttpRequestDecoder());
					//将消息转化为单一的请求, 把多个HttpMessage组装成一个完整的Http请求或者响应
					ch.pipeline().addLast("http-aggregator",new HttpObjectAggregator(maxContentLength));
					//http请求响应编码器,当Server处理完消息后，需要向Client发送响应。那么需要把响应编码成字节，再发送出去
					ch.pipeline().addLast("http-encoder",new HttpResponseEncoder());
					//支持异步发送大的码流，但是不占用过多内存，防止发生内存溢出错误
					ch.pipeline().addLast("http-chunked",new ChunkedWriteHandler());
					
					ch.pipeline().addLast("fileServerHandler", new FileServerHandler(rootpath));
				}

			});

			ChannelFuture cf = bootstrap.bind(host, port).sync();
			System.out.println("--------------------文件服务启动----------------------");

			cf.channel().closeFuture().sync();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			boss.shutdownGracefully();
			worker.shutdownGracefully();
		}

	}

	public static void main(String[] args) {
		new HttpFileServer().run();
	}

}
