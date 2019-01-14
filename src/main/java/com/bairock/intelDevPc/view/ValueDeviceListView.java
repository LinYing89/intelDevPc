package com.bairock.intelDevPc.view;

import java.io.IOException;
import java.util.List;

import com.bairock.intelDevPc.IntelDevPcApplication;
import com.bairock.intelDevPc.SpringUtil;
import com.bairock.intelDevPc.Util;
import com.bairock.intelDevPc.controller.DevCollectorInfoController;
import com.bairock.intelDevPc.data.MyColor;
import com.bairock.intelDevPc.service.UserService;
import com.bairock.iot.intelDev.device.CtrlModel;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.Device.OnCtrlModelChangedListener;
import com.bairock.iot.intelDev.device.Device.OnStateChangedListener;
import com.bairock.iot.intelDev.device.devcollect.DevCollect;
import com.bairock.iot.intelDev.device.devcollect.CollectProperty.OnCurrentValueChangedListener;
import com.bairock.iot.intelDev.user.DevGroup;
import com.bairock.iot.intelDev.user.MyHome;
import com.bairock.iot.intelDev.user.MyHome.OnNameChangedListener;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.util.Callback;

/**
 * 数值设备列表布局
 * 
 * @author 44489
 *
 */
public class ValueDeviceListView extends VBox {

	private DevCollectorInfo devCollectorInfo = SpringUtil.getBean(DevCollectorInfo.class);

	@FXML
	private ListView<Device> lvDevices;

	private Callback<ListView<Device>, ListCell<Device>> deviceCellFactory;

	public ValueDeviceListView() {
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
//							setGraphic(getItemPane(item));
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
		DevCollect dc = (DevCollect) device;
		AnchorPane paneRoot = new AnchorPane();

		Label labelName = new Label(device.getName());
		labelName.setId("labelName");
		labelName.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
			((DevCollectorInfoController) devCollectorInfo.getPresenter()).init((DevCollect) device);
			IntelDevPcApplication.showView(DevCollectorInfo.class, Modality.WINDOW_MODAL);
		});
		labelName.setCursor(Cursor.HAND);
		AnchorPane.setLeftAnchor(labelName, 10.0);
		AnchorPane.setTopAnchor(labelName, 3.0);

		HBox hbox = new HBox();
		hbox.setSpacing(10);
		Label labelValue = new Label(dc.getCollectProperty().getValueWithSymbol());
		labelValue.setId("labelValue");
		hbox.getChildren().addAll(labelValue);
		hbox.setAlignment(Pos.CENTER);
		AnchorPane.setTopAnchor(hbox, 0.0);
		AnchorPane.setRightAnchor(hbox, 10.0);
		AnchorPane.setBottomAnchor(hbox, 0.0);

		paneRoot.getChildren().addAll(labelName, hbox);
		if (!device.isNormal()) {
			paneRoot.setStyle("-fx-background-color : " + MyColor.DANGER);
			labelName.setStyle("-fx-text-fill : white");
		} else {
//			paneRoot.setStyle("-fx-background-color : white;");
			paneRoot.setStyle("-fx-background-color : #00000000;");
			labelName.setStyle("-fx-text-fill : black");
		}

		paneRoot.setPadding(new Insets(4, 0, 4, 0));
		return paneRoot;
	}
	
	private GridPane getItemGridPane(Device device) {
		DevCollect dc = (DevCollect) device;
		GridPane paneRoot = new GridPane();
		ColumnConstraints cc2 = new ColumnConstraints();
		cc2.setPercentWidth(40);
		paneRoot.getColumnConstraints().add(cc2);
		ColumnConstraints cc3 = new ColumnConstraints();
		cc3.setPercentWidth(40);
		paneRoot.getColumnConstraints().add(cc3);
		
		Label labelName = new Label(device.getName());
		labelName.setId("labelName");
		labelName.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
			((DevCollectorInfoController) devCollectorInfo.getPresenter()).init((DevCollect) device);
			IntelDevPcApplication.showView(DevCollectorInfo.class, Modality.WINDOW_MODAL);
		});
		labelName.setCursor(Cursor.HAND);
		paneRoot.addColumn(0, labelName);

		Label labelValue = new Label(dc.getCollectProperty().getValueWithSymbol());
		labelValue.setId("labelValue");
		paneRoot.addColumn(1, labelValue);
		
		String ctrlModel = Util.getCtrlModelName(device.findSuperParent().getCtrlModel());
		Label labelCtrlModel = new Label(ctrlModel);
		paneRoot.addColumn(2, labelCtrlModel);

		if (!device.isNormal()) {
			paneRoot.setStyle("-fx-background-color : " + MyColor.DANGER);
			labelName.setStyle("-fx-text-fill : white");
			labelCtrlModel.setStyle("-fx-text-fill : white");
		} else {
//			paneRoot.setStyle("-fx-background-color : white;");
			paneRoot.setStyle("-fx-background-color : #00000000;");
			labelName.setStyle("-fx-text-fill : black");
			labelCtrlModel.setStyle("-fx-text-fill : black");
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
		List<DevCollect> listDev = devGroup.findListCollectDev(true);
		for (Device dev : listDev) {
			dev.removeOnStateChangedListener(onStateChangedListener);
			dev.removeOnNameChangedListener(onNameChangedListener);
			dev.removeOnCtrlModelChangedListener(onCtrlModelChangedListener);
		}
	}

	public void updateItem() {
		DevGroup devGroup = UserService.user.getListDevGroup().get(0);
		List<DevCollect> listDev = devGroup.findListCollectDev(true);
		for (Device dev : listDev) {
			dev.addOnStateChangedListener(onStateChangedListener);
			dev.addOnNameChangedListener(onNameChangedListener);
			dev.addOnCtrlModelChangedListener(onCtrlModelChangedListener);
			((DevCollect) dev).getCollectProperty().addOnCurrentValueChangedListener(onCurrentValueChangedListener);
		}
		lvDevices.getItems().clear();
		lvDevices.getItems().addAll(listDev);
	}

	private OnCurrentValueChangedListener onCurrentValueChangedListener = new OnCurrentValueChangedListener() {

		@Override
		public void onCurrentValueChanged(DevCollect dev, Float value) {
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

	private OnStateChangedListener onStateChangedListener = new OnStateChangedListener() {

		@Override
		public void onStateChanged(Device dev, String stateId) {
			Platform.runLater(() -> refresh());
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
