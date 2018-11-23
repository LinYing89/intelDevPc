package com.bairock.intelDevPc.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bairock.intelDevPc.repository.DeviceRepository;
import com.bairock.intelDevPc.service.UserService;
import com.bairock.iot.intelDev.device.DevHaveChild;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.MainCodeHelper;
import com.bairock.iot.intelDev.user.DevGroup;

import de.felixroske.jfxsupport.FXMLController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;

@FXMLController
public class DevicesController {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@FXML
	private TreeView<Device> treeViewDevices;
	@FXML
	private TextField tfDeviceName;
	@FXML
	private Label labelMainCode;
	@FXML
	private Label labelLongCoding;
	@FXML
	private ImageView imageDevice;
	
	private TreeItem<Device> selectedItem;

	@Autowired
	private DeviceRepository deviceRepository;

	public void init() {

		treeViewDevices.getSelectionModel().selectedItemProperty().removeListener(changeListener);
		treeViewDevices.getSelectionModel().selectedItemProperty().addListener(changeListener);
		treeViewDevices.setRoot(null);
//		treeViewDevices.getRoot().getChildren().clear();

		treeViewDevices.setCellFactory((TreeView<Device> tv) -> {
			TreeCell<Device> cell = new TreeCell<Device>() {

				@Override
				protected void updateItem(Device item, boolean empty) {
					// TODO Auto-generated method stub
					super.updateItem(item, empty);
					if (empty) {
						this.setText(null);
						this.setGraphic(null);
					} else {
						Device value = this.getTreeItem().getValue();
						this.setText(value.getName());
					}
				}

			};
			return cell;
		});

		treeViewDevices.setShowRoot(false);
		
		Device devRoot = new Device();
		devRoot.setName("设备");
		TreeItem<Device> root = new TreeItem<>(devRoot);
		treeViewDevices.setRoot(root);

		DevGroup devGroup = UserService.user.getListDevGroup().get(0);
		for(Device dev : devGroup.getListDevice()) {
			initDevTree(dev, root);
		}

	}
	
	private void initDevTree(Device dev, TreeItem<Device> root) {
		TreeItem<Device> item1 = new TreeItem<>(dev);
		root.getChildren().add(item1);
		if(dev instanceof DevHaveChild) {
			for(Device dd : ((DevHaveChild) dev).getListDev()) {
				initDevTree(dd, item1);
			}
		}
	}
	
	@FXML
	private void handlerSave() {
		String newName = tfDeviceName.getText();
		Device dev = selectedItem.getValue();
		dev.setName(newName);
		selectedItem.setValue(null);
		selectedItem.setValue(dev);
		deviceRepository.saveAndFlush(dev);
	}

	private ChangeListener<TreeItem<Device>> changeListener = new ChangeListener<TreeItem<Device>>() {

		@Override
		public void changed(ObservableValue<? extends TreeItem<Device>> observable, TreeItem<Device> oldValue,
				TreeItem<Device> newValue) {
			if (null != newValue) {
				selectedItem = newValue;
				Device dev = newValue.getValue();
				logger.info("changed dev " + dev.getName());
				tfDeviceName.setText(dev.getName());
				labelMainCode.setText(MainCodeHelper.getIns().getMc(dev.getMainCodeId()));
				labelLongCoding.setText(dev.getLongCoding());
			}
		}

	};
}
