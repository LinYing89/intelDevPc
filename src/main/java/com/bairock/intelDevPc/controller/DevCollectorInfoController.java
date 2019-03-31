package com.bairock.intelDevPc.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.bairock.intelDevPc.IntelDevPcApplication;
import com.bairock.intelDevPc.Util;
import com.bairock.intelDevPc.repository.CollectPropertyRepository;
import com.bairock.intelDevPc.view.CalibrationDialog;
import com.bairock.intelDevPc.view.DevCollectorInfo;
import com.bairock.iot.intelDev.communication.DevChannelBridgeHelper;
import com.bairock.iot.intelDev.device.devcollect.CollectProperty;
import com.bairock.iot.intelDev.device.devcollect.CollectSignalSource;
import com.bairock.iot.intelDev.device.devcollect.DevCollect;

import de.felixroske.jfxsupport.FXMLController;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;

@FXMLController
public class DevCollectorInfoController {

	@Autowired
	private DevCollectorInfo devCollectorInfo;
	@Autowired
	private CollectPropertyRepository collectPropertyRepository;
	@Autowired
	private CalibrationDialog calibrationDialog;
	
	private DevCollect devCollect;
	
	@FXML
	private Label labelName;
	@FXML
	private Label labelLongCoding;
	@FXML
	private Label labelCtrlModel;
	@FXML
	private TextField txtUnit;
	@FXML
	private TextField txtAa;
	@FXML
	private TextField txtAb;
	@FXML
	private TextField txta;
	@FXML
	private TextField txtb;
	@FXML
	private TextField txtCalibration;
	@FXML
	private ChoiceBox<String> comboBoxSignalSource;
	@FXML
	private HBox hboxAValue;
	@FXML
	private HBox hboxSignValue;
	
	private boolean inited;
	
	private void init1() {
		comboBoxSignalSource.getItems().clear();
		comboBoxSignalSource.getItems().addAll("数字", "4-20mA", "a-bV", "开关");
		
		comboBoxSignalSource.getSelectionModel().selectedIndexProperty().addListener((p0, p1, p2) -> {
			initSignalSourceHBox(CollectSignalSource.values()[p2.intValue()]);
		});
		
	}
	
	public void init(DevCollect device) {
		if(!inited) {
			inited = true;
			init1();
		}
		
		devCollect = device;
		CollectProperty cp = device.getCollectProperty();
		labelName.setText(device.getName());
		labelLongCoding.setText(device.getLongCoding());
		labelCtrlModel.setText(Util.getCtrlModelName(device.findSuperParent().getCtrlModel()));
		txtUnit.setText(cp.getUnitSymbol());

		switch(cp.getCollectSrc()) {
		case DIGIT:
			comboBoxSignalSource.getSelectionModel().select(0);
			break;
		case VOLTAGE:
			comboBoxSignalSource.getSelectionModel().select(1);
			break;
		case ELECTRIC_CURRENT:
			comboBoxSignalSource.getSelectionModel().select(2);
			break;
		case SWITCH:
			comboBoxSignalSource.getSelectionModel().select(3);
			break;
		}
		initSignalSourceHBox(cp.getCollectSrc());
		
		txtAa.setText(String.valueOf(cp.getLeastReferValue()));
		txtAb.setText(String.valueOf(cp.getCrestReferValue()));
		txta.setText(String.valueOf(cp.getLeastValue()));
		txtb.setText(String.valueOf(cp.getCrestValue()));
		
	}
	
	private void initSignalSourceHBox(CollectSignalSource src) {
		switch(src) {
		case DIGIT:
			hboxAValue.setVisible(true);
			hboxSignValue.setVisible(false);
			break;
		case VOLTAGE:
			hboxAValue.setVisible(true);
			hboxSignValue.setVisible(true);
			break;
		case ELECTRIC_CURRENT:
			hboxAValue.setVisible(true);
			hboxSignValue.setVisible(false);
			break;
		case SWITCH:
			hboxAValue.setVisible(false);
			hboxSignValue.setVisible(false);
			break;
		}
	}
	
	//标定按钮点击
	@FXML
	public void onCalibrationAction() {
		if(!devCollect.isNormal()) {
			Alert warning = new Alert(Alert.AlertType.WARNING,"设备异常");
			warning.showAndWait();
			return;
		}
		String strValue = txtCalibration.getText();
		try {
			int iValue = Integer.parseInt(strValue);
			if(iValue >=0 && iValue <= 10) {
				String order = devCollect.createCalibrationOrder(iValue);
				
				devCollect.setCalibrationnListener(res ->{
					devCollect.getCollectProperty().setCalibrationValue((float) iValue);
					collectPropertyRepository.saveAndFlush(devCollect.getCollectProperty());
					((CalibrationDialogCtrler) calibrationDialog.getPresenter()).calibrationOk();
				});
				
				((CalibrationDialogCtrler) calibrationDialog.getPresenter()).init();
				DevChannelBridgeHelper.getIns().sendDevOrder(devCollect, order, true);
				IntelDevPcApplication.showView(CalibrationDialog.class, Modality.WINDOW_MODAL);
			}else {
				Alert warning = new Alert(Alert.AlertType.WARNING,"输入范围为0-10");
				warning.showAndWait();
			}
		}catch (Exception e) {
			e.printStackTrace();
			Alert warning = new Alert(Alert.AlertType.WARNING,"格式错误");
			warning.showAndWait();
		}
	}
	
	public void onBtnOk() {
		CollectProperty cp = devCollect.getCollectProperty();
		cp.setUnitSymbol(txtUnit.getText());
		int srcIndex = comboBoxSignalSource.getSelectionModel().getSelectedIndex();
		cp.setCollectSrc(CollectSignalSource.values()[srcIndex]);
		float Aa = Float.valueOf(txtAa.getText());
		float Ab = Float.valueOf(txtAb.getText());
		float a = Float.valueOf(txta.getText());
		float b = Float.valueOf(txtb.getText());
		cp.setLeastReferValue(Aa);
		cp.setCrestReferValue(Ab);
		cp.setLeastValue(a);
		cp.setCrestValue(b);
		collectPropertyRepository.saveAndFlush(cp);
		devCollectorInfo.getView().getScene().getWindow().hide();
	}
	
	public void onBtnCancel() {
		devCollectorInfo.getView().getScene().getWindow().hide();
	}
}
