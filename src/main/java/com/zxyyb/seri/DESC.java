package com.zxyyb.seri;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class DESC {

	/*
	 * Java 序列化：实现java.io.Serializable 并生成序列号id即可。
	 * 
	 * 缺点：
	 * 
	 * 1. 无法跨语言 。 
	 * 
	 * 2. 序列化后的码流太大。
	 * 
	 * 3. 序列化性能低.
	 * 
	 * 主流编码框架： Google 的 Protobuf , Facebook 的 Thrift 
	 */

	public static void main(String[] args) throws IOException {

		User user = new User("斯琴格日乐");

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(bos);
		os.writeObject(user);
		os.flush();
		os.close();
		byte[] sb = bos.toByteArray();

		System.out.println("Serializable length : " + sb.length);

		System.out.println("斯琴格日乐".getBytes().length);

	}

}

class User implements Serializable {

	private static final long serialVersionUID = 1L;
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public User(String name) {
		this.name = name;
	}
}
