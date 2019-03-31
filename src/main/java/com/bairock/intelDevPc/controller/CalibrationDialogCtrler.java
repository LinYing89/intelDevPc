package com.bairock.intelDevPc.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.bairock.intelDevPc.view.CalibrationDialog;

import de.felixroske.jfxsupport.FXMLController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;

@FXMLController
public class CalibrationDialogCtrler {

	@Autowired
	private CalibrationDialog selfView;
	
	@FXML
	private Label labelMessage;
	@FXML
	private HBox hboxBtns;
	@FXML
	private ProgressIndicator progress;
	
	private CalibrationThread calibrationThread;
	
	public void init() {
		labelMessage.setText("标定中,请稍等...");
		progress.setVisible(true);
		hboxBtns.setDisable(true);
		
		calibrationThread = new CalibrationThread();
		calibrationThread.start();
	}
	
	public void calibrationOk() {
		Platform.runLater(() -> {
			calibrationThread.interrupt();
			labelMessage.setText("标定成功");
			progress.setVisible(false);
			hboxBtns.setDisable(false);
		});
	}
	
	public void handlerOk() {
		selfView.getView().getScene().getWindow().hide();
	}
	
	private class CalibrationThread extends Thread{
        @Override
        public void run() {
            try {
                Thread.sleep(10000);
                Platform.runLater(() -> {
                	labelMessage.setText("标定超时");
                	progress.setVisible(false);
            		hboxBtns.setDisable(false);
                });
            } catch (InterruptedException e) {
//                e.printStackTrace();
            }
        }
    }
}
