package com.bairock.intelDevPc.controller;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import com.bairock.intelDevPc.view.ExportExcelDialog;

import de.felixroske.jfxsupport.FXMLController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;

@FXMLController
public class ExportExcelDialogCtrler {

	@Autowired
	private ExportExcelDialog exportExcelDialog;
	
	@FXML
	private Label labelMessage;
	@FXML
	private HBox hboxBtns;
	@FXML
	private ProgressIndicator progress;
	
	private String filePath;
	
	public void init(String filePath) {
		this.filePath = filePath;
		labelMessage.setText("正在导出,请稍等...");
		System.out.println(filePath);
		progress.setVisible(true);
		hboxBtns.setDisable(true);
	}
	
	public void exportOk() {
		labelMessage.setText("导出成功");
		progress.setVisible(false);
		hboxBtns.setDisable(false);
	}
	
	public void handlerOk() {
		exportExcelDialog.getView().getScene().getWindow().hide();
	}
	//打开文件按钮
	@FXML
	public void onBtnOpenFileAction() {
		try {
			Desktop.getDesktop().open(new File(filePath));
			handlerOk();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
