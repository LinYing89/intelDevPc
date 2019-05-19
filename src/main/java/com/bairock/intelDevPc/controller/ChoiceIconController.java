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
        choicedIcon("switch");
    }
    @FXML
    public void onPressureAction() {
        choicedIcon("yeweiji");
    }
    @FXML
    public void onDeviceAction() {
        choicedIcon("device");
    }
    @FXML
    public void onConfigurationAction() {
        choicedIcon("configuration");
    }
    
    @FXML
    public void onDevice1Action() {
        choicedIcon("device1");
    }
    @FXML
    public void onDevice2Action() {
        choicedIcon("device2");
    }
    @FXML
    public void onDevice3Action() {
        choicedIcon("device3");
    }
    @FXML
    public void onDevice4Action() {
        choicedIcon("device4");
    }
    @FXML
    public void onDevice5Action() {
        choicedIcon("device5");
    }
    @FXML
    public void onDevice6Action() {
        choicedIcon("device6");
    }
    @FXML
    public void onDevice7Action() {
        choicedIcon("device7");
    }
    @FXML
    public void onDevice8Action() {
        choicedIcon("device8");
    }
    @FXML
    public void onDevice9Action() {
        choicedIcon("device9");
    }
    @FXML
    public void onDevice10Action() {
        choicedIcon("device10");
    }
    @FXML
    public void onDevice11Action() {
        choicedIcon("device11");
    }
    @FXML
    public void onDevice12Action() {
        choicedIcon("device12");
    }
    @FXML
    public void onDevice13Action() {
        choicedIcon("device13");
    }
    @FXML
    public void onDevice14Action() {
        choicedIcon("device14");
    }
    @FXML
    public void onDevice15Action() {
        choicedIcon("device15");
    }
    
    private void choicedIcon(String name) {
        iconPath = "img/" + name + ".png";
        choiceIconView.getView().getScene().getWindow().hide();
    }
    
    @FXML
    public void onCancelAction() {
        iconPath = null;
        choiceIconView.getView().getScene().getWindow().hide();
    }
}
