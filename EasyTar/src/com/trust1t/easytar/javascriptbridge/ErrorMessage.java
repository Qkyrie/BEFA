package com.trust1t.easytar.javascriptbridge;

public class ErrorMessage {

	private long code;
	private String message;
	
	
	public ErrorMessage(long code, String name) {
		super();
		this.code = code;
		this.message = name;
	}
	public long getCode() {
		return code;
	}
	public void setCode(long code) {
		this.code = code;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	@Override
	public String toString() {
		return "ErrorMessage [code=" + code + ", name=" + message + "]";
	}
	
	
	
	
}
