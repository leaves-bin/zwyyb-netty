package com.zxyyb.seri;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.msgpack.MessagePack;
import org.msgpack.template.Templates;

public class MessagePackTest {
	
	
	/*
	 * MessagePack 是一个高效的二进制序列化框架。它的特点： 编解码高效，性能高； 序列化后的码流小； 支持跨语言。
	 */
	
	public static void main(String[] args) throws IOException {
		List<String> list = new ArrayList<String>();
		list.add("12345");
		list.add("上山打老虎");
		list.add("老虎没打着");
		
		MessagePack msp = new MessagePack();
		byte[] bytes = msp.write(list);
		
		
		List<String> delist = msp.read(bytes, Templates.tList(Templates.tString()));
		System.out.println(delist.get(0));
		System.out.println(delist.get(1));
		System.out.println(delist.get(2));
	}
	
}
