package com.bairock.intelDevPc.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bairock.intelDevPc.IntelDevPcApplication;
import com.bairock.intelDevPc.Util;
import com.bairock.intelDevPc.repository.DeviceRepository;
import com.bairock.intelDevPc.service.UserService;
import com.bairock.intelDevPc.view.CtrlModelDialogView;
import com.bairock.iot.intelDev.device.CtrlModel;
import com.bairock.iot.intelDev.device.DevHaveChild;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.MainCodeHelper;
import com.bairock.iot.intelDev.user.DevGroup;

import de.felixroske.jfxsupport.FXMLController;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;

@FXMLController
public class DevicesController {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CtrlModelDialogView ctrlModelDialogView;

	@FXML
	private TreeView<Device> treeViewDevices;
	@FXML
	private TextField tfDeviceName;
	@FXML
	private Label labelMainCode;
	@FXML
	private Label labelLongCoding;
	@FXML
	private Button btnCtrlModel;
	@FXML
	private ImageView imageDevice;

	private Device selectedDevice;

	@Autowired
	private DeviceRepository deviceRepository;

	private boolean inited;

	public void init1() {
		treeViewDevices.getSelectionModel().selectedItemProperty().addListener(changeListener);

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
						String ctrlModel = Util.getCtrlModelName(value.getCtrlModel());
						this.setText(value.getName() + " (" + ctrlModel + ")");
					}
				}

			};
			return cell;
		});
	}

	public void init() {

		if (!inited) {
			inited = true;
			init1();
		}

		treeViewDevices.setRoot(null);
		treeViewDevices.setShowRoot(false);

		Device devRoot = new Device();
		devRoot.setName("设备");
		TreeItem<Device> root = new TreeItem<>(devRoot);
		treeViewDevices.setRoot(root);

		DevGroup devGroup = UserService.user.getListDevGroup().get(0);
		for (Device dev : devGroup.getListDevice()) {
			initDevTree(dev, root);
		}

	}

	private void initDevTree(Device dev, TreeItem<Device> root) {
		TreeItem<Device> item1 = new TreeItem<>(dev);
		root.getChildren().add(item1);
		if (dev instanceof DevHaveChild) {
			for (Device dd : ((DevHaveChild) dev).getListDev()) {
				initDevTree(dd, item1);
			}
		}
	}
	
	public void refresh() {
		treeViewDevices.refresh();
		String model;
		if (selectedDevice.getCtrlModel() == CtrlModel.LOCAL) {
			model = "设为远程";
		} else {
			model = "设为本地";
		}
		Platform.runLater(() -> btnCtrlModel.setText(model));
	}

	@FXML
	private void btnCtrlModelAction() {
		CtrlModelDialogController ctrler = (CtrlModelDialogController) ctrlModelDialogView.getPresenter();
		ctrler.init(selectedDevice);
		IntelDevPcApplication.showView(CtrlModelDialogView.class, Modality.APPLICATION_MODAL, ctrler.onStageCreatedListener);
	}

	@FXML
	private void handlerSave() {
		String newName = tfDeviceName.getText();
		selectedDevice.setName(newName);
		treeViewDevices.refresh();
		deviceRepository.saveAndFlush(selectedDevice);
	}

	private ChangeListener<TreeItem<Device>> changeListener = new ChangeListener<TreeItem<Device>>() {

		@Override
		public void changed(ObservableValue<? extends TreeItem<Device>> observable, TreeItem<Device> oldValue,
				TreeItem<Device> newValue) {
			if (null != newValue) {
				selectedDevice = newValue.getValue();
				logger.info("changed dev " + selectedDevice.getName());
				tfDeviceName.setText(selectedDevice.getName());
				labelMainCode.setText(MainCodeHelper.getIns().getMc(selectedDevice.getMainCodeId()));
				labelLongCoding.setText(selectedDevice.getLongCoding());
				if (selectedDevice.getParent() == null) {
					btnCtrlModel.setVisible(true);
					if (selectedDevice.getCtrlModel() == CtrlModel.LOCAL) {
						btnCtrlModel.setText("设为远程");
					} else {
						btnCtrlModel.setText("设为本地");
					}
				} else {
					btnCtrlModel.setVisible(false);
				}
			}
		}
	};
}
