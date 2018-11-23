package com.bairock.intelDevPc.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.bairock.intelDevPc.view.UpDownloadDialog;

import de.felixroske.jfxsupport.FXMLController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

@FXMLController
public class UpDownloadDialogController {

	@Autowired
	private UpDownloadDialog upDownloadDialog;
	
	@FXML
	private Label labelMessage;
	@FXML
	private Button btnOk;
	
	public void init() {
		btnOk.setDisable(true);
	}
	
	public void handlerOk() {
		upDownloadDialog.getView().getScene().getWindow().hide();
	}
	
	public void downLoadResult(boolean success) {
		Platform.runLater(()->{btnOk.setDisable(false);});
		if(success) {
			setMessage("下载成功");
		}else {
			setMessage("下载失败");
		}
	}
	
	private void setMessage(String message) {
		Platform.runLater(()->{labelMessage.setText(message);});
	}
	
	
}
