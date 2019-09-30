package com.bairock.intelDevPc.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.bairock.intelDevPc.Util;
import com.bairock.intelDevPc.view.VirtualDeviceInfo;
import com.bairock.iot.intelDev.device.virtual.DevParam;

import de.felixroske.jfxsupport.FXMLController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

@FXMLController
public class VirtualDeviceInfoController {

    @Autowired
    private VirtualDeviceInfo virtualDeviceInfo;
    
    @FXML
    private Label labelName;
    @FXML
    private Label labelLongCoding;
    @FXML
    private Label labelCtrlModel;
    
    @FXML
    private TextField txtValue;
    
    private DevParam device;
    
    public void init(DevParam device) {
        this.device = device;
        labelName.setText(device.getName());
        labelLongCoding.setText(device.getLongCoding());
        labelCtrlModel.setText(Util.getCtrlModelName(device.findSuperParent().getCtrlModel()));
        txtValue.setText(device.getValue());
    }
    
    public void onBtnOk() {
        device.setValue(txtValue.getText());
        virtualDeviceInfo.getView().getScene().getWindow().hide();
    }
    
    public void onBtnCancel() {
        virtualDeviceInfo.getView().getScene().getWindow().hide();
    }
}
