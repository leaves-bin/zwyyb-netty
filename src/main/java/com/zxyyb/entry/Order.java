package com.zxyyb.entry;

public class Order {
	
	private String code;
	
	private String address;
	
	private float price;
	
	private String userName;
	
	private String phone;

	public String getCode() {
		return code;
	}

	public String getAddress() {
		return address;
	}

	public float getPrice() {
		return price;
	}

	public String getUserName() {
		return userName;
	}

	public String getPhone() {
		return phone;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}
	
	public Order() {
	}

	public Order(String code, String address, float price, String userName, String phone) {
		super();
		this.code = code;
		this.address = address;
		this.price = price;
		this.userName = userName;
		this.phone = phone;
	}
}
