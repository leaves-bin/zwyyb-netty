package com.zxyyb.nio;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;

public class TestServer {

	/*
	 * NIO 服务端通信序列
	 * 
	 * 1 打开ServerSockChannel
	 * 
	 * 2 绑定监听地址InetSocketAddress
	 * 
	 * 3 创建Selector ,启动线程
	 * 
	 * 4 将ServerSocketChannel 注册到Selector 监听ACCEPT事件
	 * 
	 * 5 Selector 轮询就绪的 Key
	 * 
	 * 6 handleAccept() 处理新的客户端接入
	 * 
	 * 7 设置新建客户端连接的Socket 参数
	 * 
	 * 8 向Selector 注册监听读操作 Selection.OP_READ
	 * 
	 * 9 handleRead() 异步读请求消息到ByteBuffer
	 * 
	 * 10 decode 请求消息
	 * 
	 * 11 异步写ByteBuffer 到SocketChannel
	 */

	public static void main(String[] args) {

		new Thread(new TestTask(), "nio-服务").start();

	}

}

class TestTask implements Runnable {

	private static final int port = 8080;

	private static final String host = "127.0.0.1";

	private ServerSocketChannel serverSocketChannel;

	private Selector selector;

	private volatile boolean stop = false;

	public TestTask() {

		try {

			// 1 打开ServerSockChannel
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);

			// 2 绑定监听地址InetSocketAddress
			serverSocketChannel.socket().bind(new InetSocketAddress(InetAddress.getByName(host), port));

			// 3 创建Selector ,启动线程
			selector = Selector.open();

			// 4 将ServerSocketChannel 注册到Selector 监听ACCEPT事件
			SelectionKey key = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

			System.out.println("server start");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void stop() {
		this.stop = true;
	}

	public void run() {

		while (!stop) {

			try {

				selector.select(1000);

				// 5 Selector 轮询就绪的 Key
				Iterator<SelectionKey> it = selector.selectedKeys().iterator();

				SelectionKey key = null;
				while (it.hasNext()) {
					key = (SelectionKey) it.next();

					it.remove();

					try {

						handleInput(key);

					} catch (Exception e) {
						if (key != null) {
							key.cancel();
							if (key.channel() != null)
								key.channel().close();
						}
					}

				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		if (selector != null) {
			try {
				selector.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public void handleInput(SelectionKey key) throws IOException {

		if (key.isValid()) {

			// 6 handleAccept() 处理新的客户端接入
			if (key.isAcceptable()) {

				// 7 设置新建客户端连接的Socket 参数
				ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
				SocketChannel sc = ssc.accept();
				sc.configureBlocking(false);

				// 8 向Selector 注册监听读操作 Selection.OP_READ
				sc.register(selector, SelectionKey.OP_READ);

			}

			// 9 handleRead() 异步读请求消息到ByteBuffer
			if (key.isReadable()) {

				// 10 decode 请求消息
				SocketChannel sc = (SocketChannel) key.channel();

				// 11 异步写ByteBuffer 到SocketChannel
				ByteBuffer readBuffer = ByteBuffer.allocate(1024);
				int readBytes = sc.read(readBuffer);

				if (readBytes > 0) {

					readBuffer.flip();
					byte[] bytes = new byte[readBuffer.remaining()];

					readBuffer.get(bytes);

					String body = new String(bytes, "UTF-8");
					System.out.println("Server receive : " + body);

					String response = new Date().toString();
					doWrite(sc, response);

				} else if (readBytes < 0) {
					// 对端链路关闭
					key.cancel();
					sc.close();
				} else
					; // 0 字节，忽略

			}

		}

	}

	public void doWrite(SocketChannel sc, String response) throws IOException {

		try {

			byte[] bytes = response.getBytes();
			ByteBuffer buffer = ByteBuffer.allocate(bytes.length);

			buffer.put(bytes);
			buffer.flip();

			sc.write(buffer);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
