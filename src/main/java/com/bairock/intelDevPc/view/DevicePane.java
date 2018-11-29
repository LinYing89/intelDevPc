package com.bairock.intelDevPc.view;

import java.io.IOException;

import com.bairock.intelDevPc.IntelDevPcApplication;
import com.bairock.intelDevPc.SpringUtil;
import com.bairock.intelDevPc.comm.MyOnGearChangedListener;
import com.bairock.intelDevPc.controller.DevSwitchController;
import com.bairock.iot.intelDev.device.DevStateHelper;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.Gear;
import com.bairock.iot.intelDev.device.IStateDev;
import com.bairock.iot.intelDev.user.MyHome;
import com.bairock.iot.intelDev.user.MyHome.OnNameChangedListener;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;

public class DevicePane extends VBox {
	
	private Device device;
	
	@FXML
	private Label labelState;
	@FXML
	private Label labelName;
	@FXML
	private HBox hboxStateBackground;
	
	private DevSwitchInfo devSwitchInfo = SpringUtil.getBean(DevSwitchInfo.class);

	public DevicePane() {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/devicePane.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	public DevicePane(String name) {
		this();
		labelName.setText(name);
	}
	
	public DevicePane(Device device) {
		this();
		setDevice(device);
	}
	
	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		if(null == device) {
			return;
		}
		this.device = device;
		labelName.setText(device.getName());
		refreshState();
		refreshGear();
//		this.device.setOnStateChanged(new MyOnStateChangedListener());
		this.device.setOnGearChanged(new MyOnGearChangedListener());
		this.device.addOnNameChangedListener(onNameChangedListener);
	}

	public void setName(String name) {
		labelName.setText(name);
	}
	
	public void setGear(Gear gear) {
		String strGear;
		switch(gear) {
		case GUAN:
			strGear = "S";
			break;
		case KAI:
			strGear = "O";
			break;
		default:
			strGear = "A";
			break;
		}
		Platform.runLater(() -> labelState.setText(strGear));
	}
	
	public void refreshGear() {
		setGear(device.getGear());
	}
	
	public void refreshState() {
		if(device.getDevStateId().equals(DevStateHelper.DS_KAI)) {
//			Platform.runLater(()->scheduleLabel.setText("0%"));
			hboxStateBackground.setStyle("-fx-background-color : #008B45;");
		}else if(device.getDevStateId().equals(DevStateHelper.DS_GUAN)) {
			hboxStateBackground.setStyle("-fx-background-color : #8DB6CD;");
		}else if(device.getDevStateId().equals(DevStateHelper.DS_YI_CHANG)) {
			hboxStateBackground.setStyle("-fx-background-color : #CD6839;");
		}
	}

	public void setState(int state) {
		if (state == 0) {
			hboxStateBackground.setStyle("-fx-background-color : #8DB6CD;");
		} else if (state == 1) {
			hboxStateBackground.setStyle("-fx-background-color : #008B45;");
		}
	}

	private OnNameChangedListener onNameChangedListener = new OnNameChangedListener() {

		@Override
		public void onNameChanged(MyHome myHome, String name) {
			setName(name);
		}
		
	};
	
	@FXML
	private void initialize() {
		// Do some work
	}

	@FXML
	private void handleChangeState(MouseEvent event) {
		System.out.println("handleChangeState");
		IStateDev iStateDev = (IStateDev)device;
		if(device.isKaiState()) {
			IntelDevPcApplication.sendOrder(device, iStateDev.getTurnOffOrder(), true);
			device.setGear(Gear.GUAN);
		}else {
			IntelDevPcApplication.sendOrder(device, iStateDev.getTurnOnOrder(), true);
			device.setGear(Gear.KAI);
		}
	}

	@FXML
	private void handleReleased(MouseEvent event) {
		refreshState();
//		hboxStateBackground.setStyle("-fx-background-color : #008B45;");
	}

	@FXML
	private void handlePressed(MouseEvent event) {
//		refreshState();
		hboxStateBackground.setStyle("-fx-background-color : #87CEEB;");
	}
	
	public void handlerNameClicked(MouseEvent event) {
		((DevSwitchController)devSwitchInfo.getPresenter()).init(device);
		IntelDevPcApplication.showView(DevSwitchInfo.class, Modality.WINDOW_MODAL);
	}
}
