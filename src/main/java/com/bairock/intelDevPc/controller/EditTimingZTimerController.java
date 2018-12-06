package com.bairock.intelDevPc.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.bairock.intelDevPc.view.EditTimingZTimerView;
import com.bairock.iot.intelDev.linkage.timing.ZTimer;

import de.felixroske.jfxsupport.FXMLController;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

@FXMLController
public class EditTimingZTimerController {

	@Autowired
	private EditTimingZTimerView editTimingZTimerView;
	
	@FXML
	private TextField txtOnHour;
	@FXML
	private TextField txtOnMinute;
	@FXML
	private TextField txtOnSecond;
	@FXML
	private TextField txtOffHour;
	@FXML
	private TextField txtOffMinute;
	@FXML
	private TextField txtOffSecond;
	
	@FXML
	private CheckBox cbSun;
	@FXML
	private CheckBox cbMon;
	@FXML
	private CheckBox cbTues;
	@FXML
	private CheckBox cbWed;
	@FXML
	private CheckBox cbThur;
	@FXML
	private CheckBox cbFri;
	@FXML
	private CheckBox cbSat;
	
	public ZTimer timer;
	
	public boolean edit;
	public boolean result = false;
	
	public void init(ZTimer timer) {
		result = false;
		if(null == timer) {
			edit = false;
			this.timer = new ZTimer();
		}else {
			edit = true;
			this.timer = timer;
			txtOnHour.setText(String.valueOf(timer.getOnTime().getHour()));
			txtOnMinute.setText(String.valueOf(timer.getOnTime().getMinute()));
			txtOnSecond.setText(String.valueOf(timer.getOnTime().getSecond()));
			txtOffHour.setText(String.valueOf(timer.getOffTime().getHour()));
			txtOffMinute.setText(String.valueOf(timer.getOffTime().getMinute()));
			txtOffSecond.setText(String.valueOf(timer.getOnTime().getSecond()));
			
			cbSun.setSelected(timer.getWeekHelper().isSun());
			cbMon.setSelected(timer.getWeekHelper().isMon());
			cbTues.setSelected(timer.getWeekHelper().isTues());
			cbWed.setSelected(timer.getWeekHelper().isWed());
			cbThur.setSelected(timer.getWeekHelper().isThur());
			cbFri.setSelected(timer.getWeekHelper().isFri());
			cbSat.setSelected(timer.getWeekHelper().isSat());
		}
	}
	
	//确定按钮
	@FXML
	public void btnOkAction() {
		try {
			timer.getOnTime().setHour(Integer.parseInt(txtOnHour.getText()));
			timer.getOnTime().setMinute(Integer.parseInt(txtOnMinute.getText()));
			timer.getOnTime().setSecond(Integer.parseInt(txtOnSecond.getText()));
			timer.getOffTime().setHour(Integer.parseInt(txtOffHour.getText()));
			timer.getOffTime().setMinute(Integer.parseInt(txtOffMinute.getText()));
			timer.getOffTime().setSecond(Integer.parseInt(txtOffSecond.getText()));
			timer.getWeekHelper().setSun(cbSun.isSelected());
			timer.getWeekHelper().setMon(cbMon.isSelected());
			timer.getWeekHelper().setTues(cbTues.isSelected());
			timer.getWeekHelper().setWed(cbWed.isSelected());
			timer.getWeekHelper().setThur(cbThur.isSelected());
			timer.getWeekHelper().setFri(cbFri.isSelected());
			timer.getWeekHelper().setSat(cbSat.isSelected());
			result = true;
			editTimingZTimerView.getView().getScene().getWindow().hide();
		}catch(Exception e) {
			e.printStackTrace();
			result = false;
		}
	}
	
	//取消按钮
	@FXML
	public void btnCancelAction() {
		result = false;
		editTimingZTimerView.getView().getScene().getWindow().hide();
	}
}
