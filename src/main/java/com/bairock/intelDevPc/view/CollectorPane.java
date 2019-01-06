package com.bairock.intelDevPc.view;

import java.io.IOException;

import com.bairock.intelDevPc.IntelDevPcApplication;
import com.bairock.intelDevPc.SpringUtil;
import com.bairock.intelDevPc.controller.DevCollectorInfoController;
import com.bairock.intelDevPc.data.MyColor;
import com.bairock.iot.intelDev.device.DevStateHelper;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.Device.OnStateChangedListener;
import com.bairock.iot.intelDev.device.devcollect.CollectProperty.OnCurrentValueChangedListener;
import com.bairock.iot.intelDev.device.devcollect.DevCollect;
import com.bairock.iot.intelDev.user.MyHome;
import com.bairock.iot.intelDev.user.MyHome.OnNameChangedListener;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;

public class CollectorPane extends VBox{
	
	private DevCollect device;
	
	@FXML
	private Label labelValue;
	@FXML
	private Label labelName;
	@FXML
	private HBox hboxStateBackground;
	
	private DevCollectorInfo devCollectorInfo = SpringUtil.getBean(DevCollectorInfo.class);

	public CollectorPane() {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/collectorPane.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	public CollectorPane(String name) {
		this();
		labelName.setText(name);
	}
	
	public CollectorPane(DevCollect device) {
		this();
		setDevice(device);
	}
	
	public DevCollect getDevice() {
		return device;
	}

	public void setDevice(DevCollect device) {
		if(null == device) {
			return;
		}
		this.device = device;
		labelName.setText(device.getName());
		labelValue.setText(String.valueOf(device.getCollectProperty().getCurrentValue()));
		refreshState();
		this.device.addOnStateChangedListener(onStateChangedListener);
		this.device.addOnNameChangedListener(onNameChangedListener);
		this.device.getCollectProperty().addOnCurrentValueChangedListener(onCurrentValueChangedListener);
	}

	public void setName(String name) {
		labelName.setText(name);
	}
	
	public void refreshState() {
		if(device.getDevStateId().equals(DevStateHelper.DS_YI_CHANG)) {
			hboxStateBackground.setStyle("-fx-background-color : " + MyColor.DANGER);
		}else {
			hboxStateBackground.setStyle("-fx-background-color : " + MyColor.INFO);
		}
	}

	private OnCurrentValueChangedListener onCurrentValueChangedListener = new OnCurrentValueChangedListener() {

		@Override
		public void onCurrentValueChanged(DevCollect dev, Float value) {
			Platform.runLater(()->labelValue.setText(String.valueOf(value)));
		}
		
	};
	
	private OnNameChangedListener onNameChangedListener = new OnNameChangedListener() {

		@Override
		public void onNameChanged(MyHome myHome, String name) {
			setName(name);
		}
		
	};
	
	private OnStateChangedListener onStateChangedListener = new OnStateChangedListener() {

		@Override
		public void onStateChanged(Device dev, String stateId) {
			refreshState();
		}

		@Override
		public void onNormalToAbnormal(Device dev) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onAbnormalToNormal(Device dev) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onNoResponse(Device dev) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	public void destory() {
		this.device.removeOnStateChangedListener(onStateChangedListener);
		this.device.removeOnNameChangedListener(onNameChangedListener);
		this.device.getCollectProperty().removeOnCurrentValueChangedListener(onCurrentValueChangedListener);
	}
	
	@FXML
	private void initialize() {
		// Do some work
	}
	
	public void handlerNameClicked(){
		((DevCollectorInfoController)devCollectorInfo.getPresenter()).init(device);
		IntelDevPcApplication.showView(DevCollectorInfo.class, Modality.WINDOW_MODAL);
	}
}
