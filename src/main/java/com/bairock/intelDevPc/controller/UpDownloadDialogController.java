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
	
	public static final int UPLOAD = 0;
	public static final int DOWNLOAD = 1;
	//1下载, 0上传
	private int which;
	
	public void init(int which) {
		btnOk.setDisable(true);
		this.which = which;
		if(which == UPLOAD) {
			labelMessage.setText("正在上传,请稍等...");
		}else {
			labelMessage.setText("正在下载,请稍等...");
		}
	}
	
	public void handlerOk() {
		upDownloadDialog.getView().getScene().getWindow().hide();
	}
	
	public void loadResult(boolean success) {
		Platform.runLater(()->{btnOk.setDisable(false);});
		String str;
		if(success) {
			if(which == UPLOAD) {
				str = "上传成功";
			}else {
				str = "下载成功";
			}
		}else {
			if(which == UPLOAD) {
				str = "上传失败";
			}else {
				str = "下载失败";
			}
		}
		setMessage(str);
	}
	
	private void setMessage(String message) {
		Platform.runLater(()->{labelMessage.setText(message);});
	}
	
	
}
