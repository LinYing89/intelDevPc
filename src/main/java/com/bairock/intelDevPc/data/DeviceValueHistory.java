package com.bairock.intelDevPc.data;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * 设备值历史纪录
 * @author 44489
 *
 */
@Entity
public class DeviceValueHistory {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long id;
	
	//设备id
	private String deviceId;
	//设备值
	private float value;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date historyTime;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public float getValue() {
		return value;
	}

	public void setValue(float value) {
		this.value = value;
	}

	public Date getHistoryTime() {
		if(null == historyTime) {
			historyTime = new Date();
		}
		return historyTime;
	}

	public void setHistoryTime(Date historyTime) {
		this.historyTime = historyTime;
	}
	
	public String strTimeFormat() {
		SimpleDateFormat sf = new SimpleDateFormat("HH:mm:ss");
		return sf.format(historyTime);
	}
}
