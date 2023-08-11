package com.smart.helper;

public class Message {
	public String content;
	public String type;
	public Message(String type, String content) {
		super();
		this.content = content;
		this.type = type;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	
}
