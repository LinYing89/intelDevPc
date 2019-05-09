package com.bairock.intelDevPc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.bairock.intelDevPc.view.ChoiceIconView;

import javafx.fxml.FXML;

@Controller
public class ChoiceIconController {

    @Autowired
    public ChoiceIconView choiceIconView;
    
    public static String iconPath;
    
    @FXML
    public void onSwitchAction() {
        iconPath = "img/switch.png";
        choiceIconView.getView().getScene().getWindow().hide();
    }
    @FXML
    public void onPressureAction() {
        iconPath = "img/yeweiji.png";
        choiceIconView.getView().getScene().getWindow().hide();
    }
    @FXML
    public void onDeviceAction() {
        iconPath = "img/device.png";
        choiceIconView.getView().getScene().getWindow().hide();
    }
    @FXML
    public void onConfigurationAction() {
        iconPath = "img/configuration.png";
        choiceIconView.getView().getScene().getWindow().hide();
    }
    @FXML
    public void onCancelAction() {
        iconPath = null;
        choiceIconView.getView().getScene().getWindow().hide();
    }
}
