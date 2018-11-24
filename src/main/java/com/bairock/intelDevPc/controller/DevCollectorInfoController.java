package com.bairock.intelDevPc.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.bairock.intelDevPc.repository.CollectPropertyRepository;
import com.bairock.intelDevPc.view.DevCollectorInfo;
import com.bairock.iot.intelDev.device.devcollect.CollectProperty;
import com.bairock.iot.intelDev.device.devcollect.CollectSignalSource;
import com.bairock.iot.intelDev.device.devcollect.DevCollect;

import de.felixroske.jfxsupport.FXMLController;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

@FXMLController
public class DevCollectorInfoController {

	@Autowired
	private DevCollectorInfo devCollectorInfo;
	@Autowired
	private CollectPropertyRepository collectPropertyRepository;
	
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
	private ChoiceBox<String> comboBoxSignalSource;
	@FXML
	private HBox hboxAValue;
	@FXML
	private HBox hboxSignValue;
	
	public void init(DevCollect device) {
		devCollect = device;
		CollectProperty cp = device.getCollectProperty();
		labelName.setText(device.getName());
		labelLongCoding.setText(device.getLongCoding());
		labelCtrlModel.setText(device.findSuperParent().getCtrlModel().toString());
		txtUnit.setText(cp.getUnitSymbol());
		comboBoxSignalSource.getItems().clear();
		comboBoxSignalSource.getItems().addAll("数字", "4-20mA", "a-bV", "开关");
		hboxSignValue.setVisible(true);
		hboxAValue.setVisible(true);
		switch(cp.getCollectSrc()) {
		case DIGIT:
			comboBoxSignalSource.getSelectionModel().select(0);
			hboxSignValue.setVisible(false);
			break;
		case VOLTAGE:
			comboBoxSignalSource.getSelectionModel().select(1);
			break;
		case ELECTRIC_CURRENT:
			comboBoxSignalSource.getSelectionModel().select(2);
			hboxSignValue.setVisible(false);
			break;
		case SWITCH:
			comboBoxSignalSource.getSelectionModel().select(3);
			hboxAValue.setVisible(false);
			hboxSignValue.setVisible(false);
			break;
		}
		
		txtAa.setText(String.valueOf(cp.getLeastReferValue()));
		txtAb.setText(String.valueOf(cp.getCrestReferValue()));
		txta.setText(String.valueOf(cp.getLeastValue()));
		txtb.setText(String.valueOf(cp.getCrestValue()));
		
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
