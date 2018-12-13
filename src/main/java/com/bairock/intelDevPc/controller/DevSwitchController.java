package com.bairock.intelDevPc.controller;

import com.bairock.intelDevPc.Util;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.Gear;

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
	
	public void init(Device device) {
		labelName.setText(device.getName());
		labelLongCoding.setText(device.getLongCoding());
		labelCtrlModel.setText(Util.getCtrlModelName(device.findSuperParent().getCtrlModel()));
		switch(device.getGear()) {
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
				device.setGear(Gear.KAI);
			}else if(newBtn == radioGuan) {
				device.setGear(Gear.GUAN);
			}else {
				device.setGear(Gear.ZIDONG);
			}
		});
	}
}
