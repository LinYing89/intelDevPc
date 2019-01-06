package com.bairock.intelDevPc.view;

import java.io.IOException;
import java.util.List;

import com.bairock.intelDevPc.IntelDevPcApplication;
import com.bairock.intelDevPc.SpringUtil;
import com.bairock.intelDevPc.controller.DevSwitchController;
import com.bairock.intelDevPc.data.MyColor;
import com.bairock.intelDevPc.service.UserService;
import com.bairock.iot.intelDev.device.DevStateHelper;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.Device.OnGearChangedListener;
import com.bairock.iot.intelDev.device.Device.OnStateChangedListener;
import com.bairock.iot.intelDev.device.Gear;
import com.bairock.iot.intelDev.device.IStateDev;
import com.bairock.iot.intelDev.user.DevGroup;
import com.bairock.iot.intelDev.user.MyHome;
import com.bairock.iot.intelDev.user.MyHome.OnNameChangedListener;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.util.Callback;

public class StateDeviceListView extends VBox {

	private DevSwitchInfo devSwitchInfo = SpringUtil.getBean(DevSwitchInfo.class);

	@FXML
	private ListView<Device> lvDevices;

	private Callback<ListView<Device>, ListCell<Device>> deviceCellFactory;

	public StateDeviceListView() {
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
//							this.setText(item.getName());
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

	@SuppressWarnings("unused")
	private AnchorPane getItemPane(Device device) {
		AnchorPane paneRoot = new AnchorPane();

		Label labelName = new Label(device.getName());
		labelName.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
			((DevSwitchController) devSwitchInfo.getPresenter()).init(device);
			IntelDevPcApplication.showView(DevSwitchInfo.class, Modality.WINDOW_MODAL);
		});
		labelName.setCursor(Cursor.HAND);
		AnchorPane.setLeftAnchor(labelName, 10.0);
		AnchorPane.setTopAnchor(labelName, 3.0);

		HBox hbox = new HBox();
		hbox.setSpacing(10);
		ToggleButton btnOn = new ToggleButton("开");
		ToggleButton btnAuto = new ToggleButton("自动");
		ToggleButton btnOff = new ToggleButton("关");
		ToggleGroup group = new ToggleGroup();
		group.getToggles().addAll(btnOn, btnAuto, btnOff);
		hbox.getChildren().addAll(btnOn, btnAuto, btnOff);
		hbox.setAlignment(Pos.CENTER);
		AnchorPane.setTopAnchor(hbox, 0.0);
		AnchorPane.setRightAnchor(hbox, 10.0);
		AnchorPane.setBottomAnchor(hbox, 0.0);

		paneRoot.getChildren().addAll(labelName, hbox);

		if (device.getDevStateId().equals(DevStateHelper.DS_KAI)) {
			paneRoot.setStyle("-fx-background-color : " + MyColor.SUCCESS);
		} else if (device.getDevStateId().equals(DevStateHelper.DS_GUAN)) {
			paneRoot.setStyle("-fx-background-color : " + MyColor.SECONDARY);
		} else if (device.getDevStateId().equals(DevStateHelper.DS_YI_CHANG)) {
			paneRoot.setStyle("-fx-background-color : " + MyColor.DANGER);
		}

		switch (device.getGear()) {
		case GUAN:
			group.selectToggle(btnOff);
			break;
		case KAI:
			group.selectToggle(btnOn);
			break;
		default:
			group.selectToggle(btnAuto);
			break;
		}

		group.selectedToggleProperty().addListener((p0, p1, p2) -> {
			if (p2 == btnOn) {
				IntelDevPcApplication.sendOrder(device, ((IStateDev) device).getTurnOnOrder(), true);
				device.setGear(Gear.KAI);
			} else if (p2 == btnAuto) {
				device.setGear(Gear.ZIDONG);
			} else {
				IntelDevPcApplication.sendOrder(device, ((IStateDev) device).getTurnOffOrder(), true);
				device.setGear(Gear.GUAN);
			}
		});

		paneRoot.setPadding(new Insets(4, 0, 4, 0));
		return paneRoot;
	}
	
	private GridPane getItemGridPane(Device device) {
		GridPane paneRoot = new GridPane();
		ColumnConstraints cc2 = new ColumnConstraints();
		cc2.setPercentWidth(50);
		paneRoot.getColumnConstraints().add(cc2);
		
		Label labelName = new Label(device.getName());
		labelName.setId("labelName");
		labelName.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
			((DevSwitchController) devSwitchInfo.getPresenter()).init(device);
			IntelDevPcApplication.showView(DevSwitchInfo.class, Modality.WINDOW_MODAL);
		});
		labelName.setCursor(Cursor.HAND);
//		labelName.setPadding(new Insets(0, 0, 0, 8));
		paneRoot.addColumn(0, labelName);

		HBox hbox = new HBox();
		hbox.setSpacing(10);
		ToggleButton btnOn = new ToggleButton("开");
		ToggleButton btnAuto = new ToggleButton("自动");
		ToggleButton btnOff = new ToggleButton("关");
		ToggleGroup group = new ToggleGroup();
		group.getToggles().addAll(btnOn, btnAuto, btnOff);
		hbox.getChildren().addAll(btnOn, btnAuto, btnOff);
		hbox.setAlignment(Pos.CENTER);
		paneRoot.addColumn(1, hbox);

		if (device.getDevStateId().equals(DevStateHelper.DS_KAI)) {
			paneRoot.setStyle("-fx-background-color : " + MyColor.SUCCESS);
		} else if (device.getDevStateId().equals(DevStateHelper.DS_GUAN)) {
			paneRoot.setStyle("-fx-background-color : " + MyColor.SECONDARY);
		} else if (device.getDevStateId().equals(DevStateHelper.DS_YI_CHANG)) {
			paneRoot.setStyle("-fx-background-color : " + MyColor.DANGER);
		}

		switch (device.getGear()) {
		case GUAN:
			group.selectToggle(btnOff);
			break;
		case KAI:
			group.selectToggle(btnOn);
			break;
		default:
			group.selectToggle(btnAuto);
			break;
		}

		group.selectedToggleProperty().addListener((p0, p1, p2) -> {
			if (p2 == btnOn) {
				IntelDevPcApplication.sendOrder(device, ((IStateDev) device).getTurnOnOrder(), true);
				device.setGear(Gear.KAI);
			} else if (p2 == btnAuto) {
				device.setGear(Gear.ZIDONG);
			} else {
				IntelDevPcApplication.sendOrder(device, ((IStateDev) device).getTurnOffOrder(), true);
				device.setGear(Gear.GUAN);
			}
		});

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
		List<Device> listDev = devGroup.findListIStateDev(true);
		for (Device dev : listDev) {
			dev.removeOnStateChangedListener(onStateChangedListener);
			dev.removeOnGearChangedListener(onGearChangedListener);
			dev.removeOnNameChangedListener(onNameChangedListener);
		}
	}

	public void updateItem() {
		DevGroup devGroup = UserService.user.getListDevGroup().get(0);
		List<Device> listDev = devGroup.findListIStateDev(true);
		for (Device dev : listDev) {
			dev.addOnStateChangedListener(onStateChangedListener);
			dev.addOnGearChangedListener(onGearChangedListener);
			dev.addOnNameChangedListener(onNameChangedListener);
		}
		lvDevices.getItems().clear();
		lvDevices.getItems().addAll(listDev);
	}

	private OnNameChangedListener onNameChangedListener = new OnNameChangedListener() {

		@Override
		public void onNameChanged(MyHome myHome, String name) {
			refresh();
		}

	};

	private OnGearChangedListener onGearChangedListener = new OnGearChangedListener() {
		@Override
		public void onGearChanged(Device dev, Gear gear) {
			refresh();
		}
	};

	private OnStateChangedListener onStateChangedListener = new OnStateChangedListener() {

		@Override
		public void onStateChanged(Device dev, String stateId) {
			refresh();
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
}