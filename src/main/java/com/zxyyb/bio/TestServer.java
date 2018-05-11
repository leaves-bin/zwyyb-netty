package com.zxyyb.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class TestServer {

	private static final int port = 8080;

	public static void main(String[] args) {

		ServerSocket server = null;

		try {

			server = new ServerSocket(port);
			System.out.println("ServerSocket is start");

			Socket socket = null;

			while (true) {
				socket = server.accept();

				new Thread(new TestHandler(socket)).start();
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (server != null) {
				try {
					server.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

}

class TestHandler implements Runnable {

	private Socket socket;

	public TestHandler(Socket socket) {
		this.socket = socket;
	}

	public void run() {

		BufferedReader in = null;

		PrintWriter out = null;

		try {

			in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			out = new PrintWriter(this.socket.getOutputStream(), true);

			String curTime = null;
			String body = null;

			while (true) {

				body = in.readLine();
				if (body == null)
					break;

				System.out.println(" Server receive :" + body);
				curTime = "QUERY TIME".equalsIgnoreCase(body) ? new Date().toString() : "BAN";

				out.println(curTime);

			}

		} catch (Exception e) {

			if (in != null) {
				try {
					in.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}

			if (out != null) {
				out.close();
				out = null;
			}

			if (this.socket != null) {
				try {
					this.socket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				this.socket = null;
			}

		}

	}

}
