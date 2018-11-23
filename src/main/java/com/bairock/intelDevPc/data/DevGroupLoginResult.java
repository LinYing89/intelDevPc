package com.bairock.intelDevPc.data;

public class DevGroupLoginResult {

	private int stateCode;
	private String petName = "";
	private int padPort;
	private int devPort;
	private int upDownloadPort;
	
	
	public int getStateCode() {
		return stateCode;
	}
	public void setStateCode(int stateCode) {
		this.stateCode = stateCode;
	}
	public String getPetName() {
		return petName;
	}
	public void setPetName(String petName) {
		this.petName = petName;
	}
	public int getUpDownloadPort() {
		return upDownloadPort;
	}
	public void setUpDownloadPort(int upDownloadPort) {
		this.upDownloadPort = upDownloadPort;
	}
	public int getPadPort() {
		return padPort;
	}
	public void setPadPort(int padPort) {
		this.padPort = padPort;
	}
	public int getDevPort() {
		return devPort;
	}
	public void setDevPort(int devPort) {
		this.devPort = devPort;
	}
	
}
