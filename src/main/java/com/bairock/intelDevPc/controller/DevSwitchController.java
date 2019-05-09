package com.bairock.intelDevPc.controller;

import com.bairock.intelDevPc.IntelDevPcApplication;
import com.bairock.intelDevPc.Util;
import com.bairock.intelDevPc.comm.PadClient;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.Gear;
import com.bairock.iot.intelDev.order.OrderType;

import de.felixroske.jfxsupport.FXMLController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;

@FXMLController
public class DevSwitchController {

	@FXML
	private Label labelName;
	@FXML
	private Label labelLongCoding;
	@FXML
	private Label labelCtrlModel;
	
	@FXML
	private ToggleGroup toogleGroupGear;
	
	@FXML
	private RadioButton radioKai;
	@FXML
	private RadioButton radioAuto;
	@FXML
	private RadioButton radioGuan;
	
	private Device device;
	
	public void init(Device device) {
	    this.device = device;
		labelName.setText(device.getName());
		labelLongCoding.setText(device.getLongCoding());
		labelCtrlModel.setText(Util.getCtrlModelName(device.findSuperParent().getCtrlModel()));
		switch(this.device.getGear()) {
		case KAI:
			radioKai.setSelected(true);
			break;
		case GUAN:
			radioGuan.setSelected(true);
			break;
		default:
			radioAuto.setSelected(true);
			break;
		}
		toogleGroupGear.selectedToggleProperty().addListener((o, oldBtn, newBtn) -> {
			
			if(newBtn == radioKai) {
			    this.device.setGear(Gear.KAI);
			}else if(newBtn == radioGuan) {
			    this.device.setGear(Gear.GUAN);
			}else {
			    this.device.setGear(Gear.ZIDONG);
			}
			//向服务器发送档位, 不能在监听器中发送, 因为如果是远程登录, 当收到服务器档位改变后不可以再向服务器发送
			//宫格中, 列表中, 属性界面中三个地方一致处理
			String gearOrder = IntelDevPcApplication.createDeviceOrder(this.device, OrderType.GEAR, this.device.getGear().toString());
			PadClient.getIns().send(gearOrder);
		});
	}
}
