package com.bairock.intelDevPc.data;

public class LoginResult {

	private int code;
	private String msg;
	private DevGroupLoginResult data;
	
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public DevGroupLoginResult getData() {
		return data;
	}
	public void setData(DevGroupLoginResult data) {
		this.data = data;
	}
	
}
