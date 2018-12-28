package com.bairock.intelDevPc.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.bairock.intelDevPc.service.UserService;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.devcollect.DevCollect;
import com.bairock.iot.intelDev.user.DevGroup;

import de.felixroske.jfxsupport.FXMLController;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

@FXMLController
public class SortDeviceController {

	@Autowired
	private MainController mainController;
	
	@FXML
	private ListView<Device> listViewStateDevice;
	@FXML
	private ListView<Device> listViewValueDevice;

	private List<Device> listStateDevice;
	private List<DevCollect> listValueDevice;
	
	private Device selectedStateDevice;
	private Device selectedValueDevice;

	private boolean inited = false;

	// 受控设备列表适配器
	private Callback<ListView<Device>, ListCell<Device>> deviceCellFactory = new Callback<ListView<Device>, ListCell<Device>>() {
		@Override
		public ListCell<Device> call(ListView<Device> param) {
			return new ListCell<Device>() {
				@Override
				protected void updateItem(Device item, boolean empty) {
					super.updateItem(item, empty);
					if (null != item && !empty) {
						String name = item.getName();
						this.setText(name);
						setGraphic(null);
					} else {
						this.setText(null);
						setGraphic(null);
					}
				}
			};
		}
	};

	private void init1() {
		listViewStateDevice.setCellFactory(deviceCellFactory);
		listViewValueDevice.setCellFactory(deviceCellFactory);
		
		listViewStateDevice.getSelectionModel().selectedItemProperty().addListener((p1, p2, device) -> {
			if (null == device) {
				return;
			}
			selectedStateDevice = (Device) device;
		});
		listViewValueDevice.getSelectionModel().selectedItemProperty().addListener((p1, p2, device) -> {
			if (null == device) {
				return;
			}
			selectedValueDevice = (Device) device;
		});
	}

	public void init() {
		if (!inited) {
			inited = true;
			init1();
		}
		DevGroup devGroup = UserService.user.getListDevGroup().get(0);
		listStateDevice = devGroup.findListIStateDev(true);
		Collections.sort(listStateDevice);
		for(int i = 0; i < listStateDevice.size(); i++) {
			listStateDevice.get(i).setSortIndex(i);
		}
		listViewStateDevice.getItems().clear();
		listViewStateDevice.getItems().addAll(listStateDevice);
		
		listValueDevice = devGroup.findListCollectDev(true);
		Collections.sort(listValueDevice);
		for(int i = 0; i < listValueDevice.size(); i++) {
			listValueDevice.get(i).setSortIndex(i);
		}
		listViewValueDevice.getItems().clear();
		listViewValueDevice.getItems().addAll(listValueDevice);
	}
	
	private void updateStateList() {
		Collections.sort(listStateDevice);
		listViewStateDevice.getItems().clear();
		listViewStateDevice.getItems().addAll(listStateDevice);
		listViewStateDevice.getSelectionModel().select(selectedStateDevice);
		mainController.refreshStateDevicePane();
	}
	
	private void updateValueList() {
		Collections.sort(listValueDevice);
		listViewValueDevice.getItems().clear();
		listViewValueDevice.getItems().addAll(listValueDevice);
		listViewValueDevice.getSelectionModel().select(selectedValueDevice);
		mainController.refreshValueDevicePane();
	}
	
	@FXML
	public void btnStateDevUp() {
		if(null == selectedStateDevice) {
			return;
		}
		int index = listStateDevice.indexOf(selectedStateDevice);
		if(index > 0) {
			Device devUp = listStateDevice.get(index - 1);
			int sortIndex = selectedStateDevice.getSortIndex();
			selectedStateDevice.setSortIndex(devUp.getSortIndex());
			devUp.setSortIndex(sortIndex);
			updateStateList();
		}
	}
	@FXML
	public void btnStateDevDown() {
		if(null == selectedStateDevice) {
			return;
		}
		int index = listStateDevice.indexOf(selectedStateDevice);
		if(index < listStateDevice.size() - 1) {
			Device devDown = listStateDevice.get(index + 1);
			int sortIndex = selectedStateDevice.getSortIndex();
			selectedStateDevice.setSortIndex(devDown.getSortIndex());
			devDown.setSortIndex(sortIndex);
			updateStateList();
		}
	}
	
	@FXML
	public void btnValueDevUp() {
		if(null == selectedValueDevice) {
			return;
		}
		int index = listValueDevice.indexOf(selectedValueDevice);
		if(index > 0) {
			Device devUp = listValueDevice.get(index - 1);
			int sortIndex = selectedValueDevice.getSortIndex();
			selectedValueDevice.setSortIndex(devUp.getSortIndex());
			devUp.setSortIndex(sortIndex);
			updateValueList();
		}
	}
	@FXML
	public void btnValueDevDown() {
		if(null == selectedValueDevice) {
			return;
		}
		int index = listValueDevice.indexOf(selectedValueDevice);
		if(index < listValueDevice.size() - 1) {
			Device devDown = listValueDevice.get(index + 1);
			int sortIndex = selectedValueDevice.getSortIndex();
			selectedValueDevice.setSortIndex(devDown.getSortIndex());
			devDown.setSortIndex(sortIndex);
			updateValueList();
		}
	}
}
