package com.bairock.intelDevPc.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.bairock.intelDevPc.repository.DeviceRepository;
import com.bairock.intelDevPc.service.UserService;
import com.bairock.intelDevPc.view.AddDeviceView;
import com.bairock.iot.intelDev.device.DevHaveChild;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.DeviceAssistent;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

@Controller
public class AddDeviceController {

    public static DevHaveChild PARENT_DEVICE;
    
    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    public AddDeviceView addDeviceView;
    @Autowired
    private MainController mainController;
    @Autowired
    private DevicesController devicesController;

    @FXML
    private ListView<String> listViewMainDevices;
    
    @FXML
    private GridPane gridPane;
    @FXML
    private Label labelParent;
    @FXML
    private Label labelMainCode;
    @FXML
    private TextField txtSubCode;
    @FXML
    private TextField txtDeviceName;

    private ObservableList<String> mainDevices = FXCollections.observableArrayList("协调器", "PLC", "一路开关", "两路开关", "三路开关",
            "多路开关", "信号采集控制器", "信号采集器", "呱呱嘴", "虚拟-参数设备", "虚拟-计数器");
    private List<String> mainCodes = new ArrayList<>(Arrays.asList("A1", "Ax", "B1", "B2", "B3", "Bx", "bx", "b1", "R1", "V1", "V2"));
    
    private boolean inited = false;
    
    private void init1() {
        if (!inited) {
            inited = true;
            listViewMainDevices.setItems(mainDevices);
            
            listViewMainDevices.getSelectionModel().selectedIndexProperty().addListener((p1, p2, p3) -> {
                if (null == p3) {
                    return;
                }
                labelMainCode.setText(mainCodes.get(p3.intValue()));
                txtDeviceName.setText(mainDevices.get(p3.intValue()));
            });
        }
    }
    
    public void init() {
        init1();
        labelMainCode.setText("");
        if(null != PARENT_DEVICE) {
            labelParent.setText(PARENT_DEVICE.getName() + " (" + PARENT_DEVICE.getLongCoding() + ")");
//            gridPane.getRowConstraints().get(0).setMaxHeight(Region.USE_COMPUTED_SIZE);
        }else {
            labelParent.setText("");
//            gridPane.getRowConstraints().get(0).setMaxHeight(0);
        }
    }
    
    @FXML
    public void onBtnSaveAction() {
        String mainCode = labelMainCode.getText();
        if(mainCode.trim().isEmpty()) {
            Alert warning = new Alert(Alert.AlertType.WARNING, "请选择设备类型");
            warning.showAndWait();
            return;
        }
        String subCode = txtSubCode.getText();
        if(subCode.trim().isEmpty()) {
            Alert warning = new Alert(Alert.AlertType.WARNING, "次编码不能为空");
            warning.showAndWait();
            return;
        }
        String deviceName = txtDeviceName.getText();
        if(deviceName.trim().isEmpty()) {
            Alert warning = new Alert(Alert.AlertType.WARNING, "设备名不能为空");
            warning.showAndWait();
            return;
        }
        String coding = labelMainCode.getText() + subCode;
        Device devInGroup = UserService.getDevGroup().findDeviceWithCoding(coding);
        if(null != devInGroup) {
            Alert warning = new Alert(Alert.AlertType.WARNING, "设备编码已存在");
            warning.showAndWait();
            return;
        }
        boolean nameHaved = UserService.getDevGroup().deviceNameIsHaved(deviceName);
        if(nameHaved) {
            Alert warning = new Alert(Alert.AlertType.WARNING, "设备名称已存在");
            warning.showAndWait();
            return;
        }
        
        Device dev = DeviceAssistent.createDeviceByCoding(coding);
        if(null == dev) {
            Alert warning = new Alert(Alert.AlertType.WARNING, "不可识别编码");
            warning.showAndWait();
            return;
        }
        
        dev.setName(deviceName);
        dev.setAlias(deviceName);
        if(null != PARENT_DEVICE) {
            PARENT_DEVICE.addChildDev(dev);
        }else {
            UserService.getDevGroup().addDevice(dev);
        }
        deviceRepository.save(dev);
        
        devicesController.init();
        mainController.refreshDevicePane();
        
        PARENT_DEVICE = null;
        addDeviceView.getView().getScene().getWindow().hide();
    }
    
    @FXML
    public void onBtnCancelAction() {
        PARENT_DEVICE = null;
        addDeviceView.getView().getScene().getWindow().hide();
    }
}
