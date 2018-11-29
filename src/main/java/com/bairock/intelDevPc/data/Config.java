package com.bairock.intelDevPc.data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Config {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long id;
	
	private String serverName = "051801.cn";
	private int upDownloadPort = 10004;
	private int padPort = 10002;
	private String appTitle = "大发科技智能物联网控制平台";
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getServerName() {
		return serverName;
	}
	public void setServerName(String serverName) {
		this.serverName = serverName;
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
	public String getAppTitle() {
		return appTitle;
	}
	public void setAppTitle(String appTitle) {
		this.appTitle = appTitle;
	}
	
	
}
