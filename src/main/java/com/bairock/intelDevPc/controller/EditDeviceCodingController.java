package com.bairock.intelDevPc.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.bairock.intelDevPc.service.UserService;
import com.bairock.intelDevPc.view.EditDeviceCoding;
import com.bairock.iot.intelDev.device.Device;

import de.felixroske.jfxsupport.FXMLController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

@FXMLController
public class EditDeviceCodingController {

    public static String newSubCoding = null;
    
    @Autowired
    private EditDeviceCoding editDeviceCoding;
    
    @FXML
    public TextField txtSubCode;
    @FXML
    public Label labelMainCode;
    @FXML
    public Label labelWarning;
    
    public String oldSubCode;
    
    public void init(String mainCode, String subCode) {
        labelWarning.setVisible(false);
        oldSubCode = subCode;
        labelMainCode.setText(mainCode);
        txtSubCode.setText(subCode);
        newSubCoding = null;
    }
    
    @FXML
    private void onSaveAction() {
        String subCode = txtSubCode.getText();
        if(subCode.isEmpty()) {
            labelWarning.setText("设备编码不能为空!");
            labelWarning.setVisible(true);
            return;
        }
        if(oldSubCode.equals(subCode)) {
            labelWarning.setText("编码未更改!");
            labelWarning.setVisible(true);
            return;
        }
        String longCoding = labelMainCode.getText() + subCode;
        Device device = UserService.getDevGroup().findDeviceWithCoding(longCoding);
        if(null != device) {
            labelWarning.setText("设备编码在组内已存在!");
            labelWarning.setVisible(true);
            return;
        }
        newSubCoding = subCode;
        editDeviceCoding.getView().getScene().getWindow().hide();
    }
    
    @FXML
    private void onCancelAction() {
        newSubCoding = null;
        editDeviceCoding.getView().getScene().getWindow().hide();
    }
}
