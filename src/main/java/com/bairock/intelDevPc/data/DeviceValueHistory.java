package com.bairock.intelDevPc.data;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonFormat;

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
	private String deviceName;
	private String longCoding;
	private boolean abnormal;
	//设备值
	private float value;
	
	@Temporal(TemporalType.TIMESTAMP)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
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

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getLongCoding() {
		return longCoding;
	}

	public void setLongCoding(String longCoding) {
		this.longCoding = longCoding;
	}

	public boolean isAbnormal() {
		return abnormal;
	}

	public void setAbnormal(boolean abnormal) {
		this.abnormal = abnormal;
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
	
	public String strTimeChartFormat() {
		SimpleDateFormat sf = new SimpleDateFormat("YYYY/MM/dd\nHH:mm:ss");
		return sf.format(historyTime);
	}
	
	public String strTimeFileNameFormat() {
		SimpleDateFormat sf = new SimpleDateFormat("YYYYMMddHHmmss");
		return sf.format(historyTime);
	}
	
	public String strTimeFormat() {
		SimpleDateFormat sf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
		return sf.format(historyTime);
	}
}
