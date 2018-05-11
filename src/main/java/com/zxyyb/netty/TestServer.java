package com.zxyyb.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class TestServer {

	private static final int port = 8090;

	public static void main(String[] args) {
		new TestServer().bind();
	}

	public void bind() {

		/*
		 * NioEventLoopGroup
		 * 是用来处理I/O操作的多线程事件循环器，Netty提供了许多不同的EventLoopGroup的实现用来处理不同传输协议。
		 * 
		 * 第一个经常被叫做‘boss’，用来接收进来的连接。 第二个经常被叫做‘worker’，用来处理已经被接收的连接。
		 * 一旦‘boss’接收到连接，就会把连接信息注册到‘worker’上。
		 * 如何知道多少个线程已经被使用，如何映射到已经创建的Channels上都需要依赖于EventLoopGroup的实现，并且可以通过构造函数来配置他们的关系。
		 */
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();

		try {

			/*
			 * ServerBootstrap 是一个启动NIO服务的辅助启动类
			 */
			ServerBootstrap sb = new ServerBootstrap();
			sb = sb.group(bossGroup, workerGroup);
			sb = sb.channel(NioServerSocketChannel.class);
			sb = sb.option(ChannelOption.SO_BACKLOG, 1024);
			sb = sb.handler(new LoggingHandler(LogLevel.INFO));
			/*
			 * 当一个链接建立时，我们需要知道怎么来接收或者发送数据，当然，我们有各种各样的Handler实现来处理它。
			 * 那么ChannelInitializer便是用来配置这些Handler，它会提供一个ChannelPipeline，并把Handler加入到ChannelPipeline
			 */
			sb = sb.childHandler(new ChannelInitializer<SocketChannel>() {

				@Override
				protected void initChannel(SocketChannel arg0) throws Exception {
					System.out.println("报告");
					System.out.println("信息：有一客户端链接到本服务端");
					System.out.println("IP:" + arg0.localAddress().getHostName());
					System.out.println("Port:" + arg0.localAddress().getPort());
					System.out.println("报告完毕");

					arg0.pipeline().addLast(new TestServerHandle());
				}

			});
			
			sb = sb.childOption(ChannelOption.SO_KEEPALIVE, true);

			// 绑定端口,同步等待成功
			ChannelFuture cf = sb.bind(port).sync();

			// 等待服务端监听端口关闭
			cf.channel().closeFuture().sync();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 退出
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}

	}

}
