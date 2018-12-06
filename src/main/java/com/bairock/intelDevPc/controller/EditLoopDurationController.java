package com.bairock.intelDevPc.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import com.bairock.intelDevPc.view.EditLoopDurationView;
import com.bairock.iot.intelDev.linkage.loop.LoopDuration;

import de.felixroske.jfxsupport.FXMLController;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

@FXMLController
public class EditLoopDurationController {

	@Autowired
	private EditLoopDurationView editLoopDurationView;

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

	public LoopDuration loopDuration;

	public boolean edit;
	public boolean result = false;

	public void init(LoopDuration loopDuration) {
		result = false;
		if (null == loopDuration) {
			edit = false;
			this.loopDuration = new LoopDuration();
			this.loopDuration.setId(UUID.randomUUID().toString());
		} else {
			edit = true;
			this.loopDuration = loopDuration;
			txtOnHour.setText(String.valueOf(loopDuration.getOnKeepTime().getHour()));
			txtOnMinute.setText(String.valueOf(loopDuration.getOnKeepTime().getMinute()));
			txtOnSecond.setText(String.valueOf(loopDuration.getOnKeepTime().getSecond()));
			txtOffHour.setText(String.valueOf(loopDuration.getOffKeepTime().getHour()));
			txtOffMinute.setText(String.valueOf(loopDuration.getOffKeepTime().getMinute()));
			txtOffSecond.setText(String.valueOf(loopDuration.getOffKeepTime().getSecond()));
		}
	}

	// 确定按钮
	@FXML
	public void btnOkAction() {
		try {
			loopDuration.getOnKeepTime().setHour(Integer.parseInt(txtOnHour.getText()));
			loopDuration.getOnKeepTime().setMinute(Integer.parseInt(txtOnMinute.getText()));
			loopDuration.getOnKeepTime().setSecond(Integer.parseInt(txtOnSecond.getText()));
			loopDuration.getOffKeepTime().setHour(Integer.parseInt(txtOffHour.getText()));
			loopDuration.getOffKeepTime().setMinute(Integer.parseInt(txtOffMinute.getText()));
			loopDuration.getOffKeepTime().setSecond(Integer.parseInt(txtOffSecond.getText()));
			result = true;
			editLoopDurationView.getView().getScene().getWindow().hide();
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		}
	}

	// 取消按钮
	@FXML
	public void btnCancelAction() {
		result = false;
		editLoopDurationView.getView().getScene().getWindow().hide();
	}
}
