package com.bairock.intelDevPc.data;

import com.bairock.iot.intelDev.device.CtrlModel;
import com.bairock.iot.intelDev.device.Device;

public class DeviceModelHelper {

	private Device devToSet;
	private CtrlModel ctrlModel;
	private String order;
	public Device getDevToSet() {
		return devToSet;
	}
	public void setDevToSet(Device devToSet) {
		this.devToSet = devToSet;
	}
	public CtrlModel getCtrlModel() {
		return ctrlModel;
	}
	public void setCtrlModel(CtrlModel ctrlModel) {
		this.ctrlModel = ctrlModel;
	}
	public String getOrder() {
		return order;
	}
	public void setOrder(String order) {
		this.order = order;
	}
	
	
}
