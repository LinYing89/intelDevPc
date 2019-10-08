package com.bairock.intelDevPc.view;

import java.io.IOException;
import java.util.List;

import com.bairock.intelDevPc.IntelDevPcApplication;
import com.bairock.intelDevPc.SpringUtil;
import com.bairock.intelDevPc.controller.VirtualDeviceInfoController;
import com.bairock.intelDevPc.data.MyColor;
import com.bairock.intelDevPc.service.UserService;
import com.bairock.iot.intelDev.device.CtrlModel;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.Device.OnCtrlModelChangedListener;
import com.bairock.iot.intelDev.device.virtual.DevParam;
import com.bairock.iot.intelDev.device.virtual.DevParam.OnValueChangedListener;
import com.bairock.iot.intelDev.user.DevGroup;
import com.bairock.iot.intelDev.user.MyHome;
import com.bairock.iot.intelDev.user.MyHome.OnNameChangedListener;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.util.Callback;

public class VirtualDeviceListView extends VBox {

    private VirtualDeviceInfo virtualDeviceInfo = SpringUtil.getBean(VirtualDeviceInfo.class);
    @FXML
    private ListView<Device> lvDevices;

    private Callback<ListView<Device>, ListCell<Device>> deviceCellFactory;

    public VirtualDeviceListView() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/deviceListPane.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        String urlString = this.getClass().getResource("/css/valueDeviceListView.css").toExternalForm();
        this.getStylesheets().add(urlString);
        deviceCellFactory = new Callback<ListView<Device>, ListCell<Device>>() {
            @Override
            public ListCell<Device> call(ListView<Device> param) {
                return new ListCell<Device>() {
                    @Override
                    protected void updateItem(Device item, boolean empty) {
                        super.updateItem(item, empty);
                        if (null != item && !empty) {
                            this.setPadding(new Insets(1, 0, 0, 0));
                            setGraphic(getItemGridPane(item));
                        } else {
                            this.setText(null);
                            setGraphic(null);
                        }
                    }
                };
            }
        };

        lvDevices.setCellFactory(deviceCellFactory);
        
        AnchorPane.setLeftAnchor(this, 0.0);
        AnchorPane.setTopAnchor(this, 0.0);
        AnchorPane.setRightAnchor(this, 0.0);
        AnchorPane.setBottomAnchor(this, 0.0);
    }

    private GridPane getItemGridPane(Device device) {
        DevParam dc = (DevParam) device;
        GridPane paneRoot = new GridPane();
        ColumnConstraints cc2 = new ColumnConstraints();
        cc2.setPercentWidth(50);
        paneRoot.getColumnConstraints().add(cc2);
        ColumnConstraints cc3 = new ColumnConstraints();
        cc3.setPercentWidth(50);
        paneRoot.getColumnConstraints().add(cc3);
//        ColumnConstraints cc4 = new ColumnConstraints();
//        cc4.setPercentWidth(30);
//        paneRoot.getColumnConstraints().add(cc4);
        
        Label labelName = new Label(device.getName());
        labelName.setId("labelName");
        labelName.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            ((VirtualDeviceInfoController) virtualDeviceInfo.getPresenter()).init((DevParam) device);
            IntelDevPcApplication.showView(VirtualDeviceInfo.class, Modality.WINDOW_MODAL);
        });
        labelName.setCursor(Cursor.HAND);
        paneRoot.addColumn(0, labelName);

        Label labelValue = new Label(dc.getValue());
        labelValue.setId("labelValue");
        paneRoot.addColumn(1, labelValue);
        
//        String ctrlModel = Util.getCtrlModelName(device.findSuperParent().getCtrlModel());
//        Label labelCtrlModel = new Label(ctrlModel);
//        paneRoot.addColumn(2, labelCtrlModel);

        if (!device.isNormal()) {
            paneRoot.setStyle("-fx-background-color : " + MyColor.DANGER);
            labelName.setStyle("-fx-text-fill : white");
//            labelCtrlModel.setStyle("-fx-text-fill : white");
        } else {
            paneRoot.setStyle("-fx-background-color : #00000000;");
            labelName.setStyle("-fx-text-fill : black");
//            labelCtrlModel.setStyle("-fx-text-fill : black");
        }

        paneRoot.setPadding(new Insets(4, 0, 4, 0));
        return paneRoot;
    }

    public void refresh() {
        if (null != lvDevices) {
            lvDevices.refresh();
        }
    }
    
    public void destory() {
        DevGroup devGroup = UserService.user.getListDevGroup().get(0);
        List<DevParam> listDev = devGroup.findListDevParam(true);
        for (Device dev : listDev) {
            dev.removeOnNameChangedListener(onNameChangedListener);
            dev.removeOnCtrlModelChangedListener(onCtrlModelChangedListener);
            ((DevParam) dev).removeOnValueChangedListener(onValueChangedListener);
        }
    }

    public void updateItem() {
        DevGroup devGroup = UserService.user.getListDevGroup().get(0);
        List<DevParam> listDev = devGroup.findListDevParam(true);
        for (Device dev : listDev) {
            dev.addOnNameChangedListener(onNameChangedListener);
            dev.addOnCtrlModelChangedListener(onCtrlModelChangedListener);
            ((DevParam) dev).addOnValueChangedListener(onValueChangedListener);
        }
        lvDevices.getItems().clear();
        lvDevices.getItems().addAll(listDev);
    }

    private OnValueChangedListener onValueChangedListener = new OnValueChangedListener() {

        @Override
        public void onValueChanged(DevParam dev, String value) {
            Platform.runLater(() -> refresh());
        }

    };

    private OnNameChangedListener onNameChangedListener = new OnNameChangedListener() {

        @Override
        public void onNameChanged(MyHome myHome, String name) {
            refresh();
        }

    };
    
    private OnCtrlModelChangedListener onCtrlModelChangedListener = new OnCtrlModelChangedListener() {

        @Override
        public void onCtrlModelChanged(Device dev, CtrlModel ctrlModel) {
            Platform.runLater(() -> refresh());
        }
    };

}
