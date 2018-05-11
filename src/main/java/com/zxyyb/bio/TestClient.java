package com.zxyyb.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TestClient {

	public static void main(String[] args) {

		for (;;) {
			(new Client()).start();
		}

	}

}

class Client extends Thread {

	private static final int port = 8080;

	private static final String host = "127.0.0.1";

	public void run() {
		Socket socket = null;
		BufferedReader in = null;
		PrintWriter out = null;

		try {

			socket = new Socket(host, port);

			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);

			out.println("QUERY TIME");
			System.out.println("send success");

			String resp = in.readLine();

			System.out.println("call back time : " + resp);

		} catch (Exception e) {

		} finally {

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

			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				socket = null;
			}
		}
	}

}
