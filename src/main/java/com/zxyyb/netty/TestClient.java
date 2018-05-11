package com.zxyyb.netty;

import java.net.InetAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class TestClient {

	private static final int port = 8090;

	private static final String host = "192.168.48.1";

	public static void main(String[] args) {

		new TestClient().connect();

	}

	public void connect() {

		// 配置客户端NIO线程组
		EventLoopGroup group = new NioEventLoopGroup();

		try {

			Bootstrap bt = new Bootstrap();
			bt.group(group);
			bt.channel(NioSocketChannel.class);
			bt.option(ChannelOption.SO_KEEPALIVE, true);
			bt.handler(new ChannelInitializer<SocketChannel>() {

				@Override
				protected void initChannel(SocketChannel arg0) throws Exception {

					System.out.println("准备");

					arg0.pipeline().addLast(new TestClientHandle());
				}
			});

			// 发起异步连接操作
			ChannelFuture cf = bt.connect(InetAddress.getByName(host), port).sync();

			// 等待客户端链路关闭
			cf.channel().closeFuture().sync();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 退出，释放NIO线程组
			group.shutdownGracefully();

		}
	}

}
