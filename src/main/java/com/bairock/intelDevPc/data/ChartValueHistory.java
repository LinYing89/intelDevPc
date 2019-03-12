package com.bairock.intelDevPc.data;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class ChartValueHistory {

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
	private Date historyTime;
	
	private float value;
	
	public ChartValueHistory() {}
	
	public ChartValueHistory(Date historyTime, float value) {
		this.historyTime = historyTime;
		this.value = value;
	}

	public Date getHistoryTime() {
		return historyTime;
	}

	public void setHistoryTime(Date historyTime) {
		this.historyTime = historyTime;
	}

	public float getValue() {
		return value;
	}

	public void setValue(float value) {
		this.value = value;
	}
	
	
}
