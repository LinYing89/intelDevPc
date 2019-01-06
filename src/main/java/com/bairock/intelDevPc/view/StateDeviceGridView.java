package com.bairock.intelDevPc.view;

import java.io.IOException;
import java.util.List;

import com.bairock.intelDevPc.service.UserService;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.user.DevGroup;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;

public class StateDeviceGridView extends ScrollPane {

	@FXML
	private FlowPane fpDevices;

	public StateDeviceGridView() {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/deviceGridView.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}

		AnchorPane.setLeftAnchor(this, 0.0);
		AnchorPane.setTopAnchor(this, 0.0);
		AnchorPane.setRightAnchor(this, 0.0);
		AnchorPane.setBottomAnchor(this, 0.0);
	}

	public void destory() {
		if (null != fpDevices) {
			for (Node node : fpDevices.getChildren()) {
				DevicePane dp = (DevicePane) node;
				dp.destory();
			}
		}
	}

	public void updateItem() {
		DevGroup devGroup = UserService.user.getListDevGroup().get(0);
		List<Device> listIStateDev = devGroup.findListIStateDev(true);
//		destory();
		fpDevices.getChildren().clear();
		for (Device dev : listIStateDev) {
			DevicePane dp = new DevicePane(dev);
			fpDevices.getChildren().add(dp);
		}
	}

	public void refreshDevicePaneState(Device dev) {
		for (Node node : fpDevices.getChildren()) {
			DevicePane dp = (DevicePane) node;
			if (dp.getDevice() == dev) {
				dp.refreshState();
				return;
			}
		}
	}
}
