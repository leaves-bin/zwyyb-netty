package com.zxyyb.nio;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class TestClient {

	/*
	 * NIO 客客户端创建序列
	 * 
	 * 1. 打开SocketChannel
	 * 
	 * 2. 设置SocketChannel为非阻塞模式，同时设置TCP参数
	 * 
	 * 3. 异步连接服务器
	 * 
	 * 4. 判断连接结果，如果连接成功，调到步骤 10 ，否则执行步骤 5
	 * 
	 * 5. 向 Reactor 线程的多路复用器注册 OP_CONNECT 事件
	 * 
	 * 6. 创建Selector ,启动线程
	 * 
	 * 7. Selector 轮询就绪的Key
	 * 
	 * 8. hangerConnect()
	 * 
	 * 9. 判断连接是否完成，完成执行步骤 10
	 * 
	 * 10. 向多路复用器注册读事件 OP_READ
	 * 
	 * 11. handleRead() 异步读请求消息到 ByteBuffer
	 * 
	 * 12. decode 请求消息
	 * 
	 * 13. 异步写ByteBuffer 到 SocketChannel
	 */

	public static void main(String[] args) {

		for (;;) {
			new Thread(new TestCall(), "nio-客户端").start();
		}

	}
}

class TestCall implements Runnable {

	private static final int port = 8080;

	private static final String host = "127.0.0.1";

	private SocketChannel socketChannel;

	private Selector selector;

	private volatile boolean stop = false;

	public TestCall() {
		try {

			// 1. 打开SocketChannel
			socketChannel = SocketChannel.open();
			// 2. 设置SocketChannel为非阻塞模式，同时设置TCP参数
			socketChannel.configureBlocking(false);
			// 6. 创建Selector ,启动线程
			selector = Selector.open();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {

		try {
			// 3. 异步连接服务器
			doConnect();

		} catch (Exception e) {
			e.printStackTrace();
		}

		while (!stop) {

			// 7. Selector 轮询就绪的Key
			try {

				selector.select(1000);
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
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public void doConnect() throws UnknownHostException, IOException {
		// 4. 判断连接结果，如果连接成功，调到步骤 10 ，否则执行步骤 5
		if (socketChannel.connect(new InetSocketAddress(InetAddress.getByName(host), port))) {

			// 10. 向多路复用器注册读事件 OP_READ
			socketChannel.register(selector, SelectionKey.OP_READ);

			doWrite(socketChannel);

		} else
			// 5. 向 Reactor 线程的多路复用器注册 OP_CONNECT 事件
			socketChannel.register(selector, SelectionKey.OP_CONNECT);

	}

	public void doWrite(SocketChannel sc) throws IOException {

		byte[] req = "QUITE TIME".getBytes();
		ByteBuffer writeBuffer = ByteBuffer.allocate(req.length);
		writeBuffer.put(req);
		writeBuffer.flip();
		sc.write(writeBuffer);

		if (!writeBuffer.hasRemaining()) {
			System.out.println("send call success");
		}

	}

	public void handleInput(SelectionKey key) throws ClosedChannelException, IOException {

		if (key.isValid()) {
			SocketChannel sc = (SocketChannel) key.channel();

			if (key.isConnectable()) {
				if (sc.finishConnect()) {
					sc.register(selector, SelectionKey.OP_READ);
					doWrite(sc);
				} else
					System.exit(1);// 连接失败，进程退出
			}

			if (key.isReadable()) {

				ByteBuffer readBuffer = ByteBuffer.allocate(1024);
				int readBytes = sc.read(readBuffer);

				if (readBytes > 0) {
					readBuffer.flip();

					byte[] bytes = new byte[readBuffer.remaining()];
					readBuffer.get(bytes);

					String response = new String(bytes, "UTF-8");
					System.out.println("Now is " + response);
					this.stop = true;
				} else if (readBytes < 0) {
					// 对端链路关闭
					key.cancel();
					sc.close();
				} else
					; // 0 字节，忽略

			}

		}
	}

}
