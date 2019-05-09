package com.bairock.intelDevPc.data;

import javax.persistence.Column;
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
	//pad连接服务器的端口
	private int padPort = 10002;
	//设备连接服务器的端口
	@Column(columnDefinition="int default 10003",nullable=false)
	private int devPort = 10003;
	
	private String appTitle = "大发科技智能物联网控制平台";
	
	@Column(columnDefinition="boolean default false",nullable=false)
	private boolean autoLogin;
	
	//登录的账号
	private String userid;
	//登录的组名
	private String devGroupName;
	private String devGroupPetname;
	
	private String loginModel;
	
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
	public String getAppTitle() {
		return appTitle;
	}
	public void setAppTitle(String appTitle) {
		this.appTitle = appTitle;
	}
	public boolean isAutoLogin() {
		return autoLogin;
	}
	public void setAutoLogin(boolean autoLogin) {
		this.autoLogin = autoLogin;
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public String getDevGroupName() {
		return devGroupName;
	}
	public void setDevGroupName(String devGroupName) {
		this.devGroupName = devGroupName;
	}
	public String getDevGroupPetname() {
		return devGroupPetname;
	}
	public void setDevGroupPetname(String devGroupPetname) {
		this.devGroupPetname = devGroupPetname;
	}
	public String getLoginModel() {
		return loginModel;
	}
	public void setLoginModel(String loginModel) {
		this.loginModel = loginModel;
	}
}
