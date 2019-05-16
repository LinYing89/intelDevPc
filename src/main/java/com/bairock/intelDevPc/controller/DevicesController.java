package com.bairock.intelDevPc.controller;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.bairock.intelDevPc.IntelDevPcApplication;
import com.bairock.intelDevPc.SpringUtil;
import com.bairock.intelDevPc.Util;
import com.bairock.intelDevPc.repository.DeviceImgRepo;
import com.bairock.intelDevPc.repository.DeviceRepository;
import com.bairock.intelDevPc.service.UserService;
import com.bairock.intelDevPc.view.CtrlModelDialogView;
import com.bairock.intelDevPc.view.EditDeviceCoding;
import com.bairock.iot.intelDev.data.DeviceImg;
import com.bairock.iot.intelDev.device.CtrlModel;
import com.bairock.iot.intelDev.device.DevHaveChild;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.MainCodeHelper;
import com.bairock.iot.intelDev.device.devswitch.DevSwitch;
import com.bairock.iot.intelDev.device.devswitch.SubDev;
import com.bairock.iot.intelDev.http.HttpDownloadDeviceImgTask;
import com.bairock.iot.intelDev.user.DevGroup;

import de.felixroske.jfxsupport.FXMLController;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;

@FXMLController
public class DevicesController {

//	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CtrlModelDialogView ctrlModelDialogView;
	@Autowired
	private MainController mainController;
	@Autowired
	private DeviceImgRepo deviceImgRepo;
	private List<DeviceImg> listDeviceImg;
	
	@FXML
	private TreeView<Device> treeViewDevices;
	@FXML
	private TextField tfDeviceName;
	@FXML
	private CheckBox cbVisibility;
	@FXML
	private Label labelMainCode;
	@FXML
	private Label labelLongCoding;
	@FXML
	private Button btnEditCoding;
	@FXML
	private Button btnCtrlModel;
	@FXML
	private ImageView imageDevice;

	private Device selectedDevice;

	@Autowired
	private DeviceRepository deviceRepository;

	private boolean inited;

	public void init1() {
		listDeviceImg = deviceImgRepo.findAll();
		
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
		cbVisibility.selectedProperty().addListener((p1, p2, p3) -> {
			if (selectedDevice.isVisibility() == p3) {
				selectedDevice.setVisibility(!p3);
				deviceRepository.saveAndFlush(selectedDevice);
				mainController.refreshDevicePane();
			}
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
		List<Device> listDevice = devGroup.getListDevice();
		sortListDevice(listDevice);
		for (Device dev : listDevice) {
			initDevTree(dev, root);
		}

	}

	//按设备编码排序
	private void sortListDevice(List<Device> listDevice) {
		listDevice.sort((a, b) -> a.getCoding().compareTo(b.getCoding()));
		for (Device dev : listDevice) {
			if (dev instanceof DevHaveChild) {
				if (dev instanceof DevSwitch) {
					//开关子设备按次设备号排序
					try {
						((DevHaveChild) dev).getListDev()
								.sort((a, b) -> Integer.parseInt(a.getSubCode()) - Integer.parseInt(b.getSubCode()));
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					sortListDevice(((DevHaveChild) dev).getListDev());
				}
			}
		}
	}

	private void initDevTree(Device dev, TreeItem<Device> root) {
		TreeItem<Device> item1 = new TreeItem<>(dev);
		root.getChildren().add(item1);
		if (dev instanceof DevHaveChild) {
			List<Device> list = ((DevHaveChild) dev).getListDev();
			for (Device dd : list) {
				initDevTree(dd, item1);
			}
		}
	}

	public void refresh() {
		if (null == treeViewDevices) {
			return;
		}
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
		IntelDevPcApplication.showView(CtrlModelDialogView.class, Modality.APPLICATION_MODAL,
				ctrler.onStageCreatedListener);
	}
	
	/**
	 * 编辑设备编码
	 */
	@FXML
	private void onEditCodingAction() {
	    String mainCode = MainCodeHelper.getIns().getMc(selectedDevice.getMainCodeId());
	    EditDeviceCoding view = SpringUtil.getBean(EditDeviceCoding.class);
	    ((EditDeviceCodingController) view.getPresenter()).init(mainCode, selectedDevice.getSubCode());
	    IntelDevPcApplication.showView(EditDeviceCoding.class, Modality.APPLICATION_MODAL);
	    if(EditDeviceCodingController.newSubCoding != null) {
	        selectedDevice.setSubCode(EditDeviceCodingController.newSubCoding);
	        deviceRepository.saveAndFlush(selectedDevice);
	        labelLongCoding.setText(selectedDevice.getLongCoding());
	    }
	}

	@FXML
	private void handlerSave() {
		String newName = tfDeviceName.getText();
		selectedDevice.setName(newName);
		treeViewDevices.refresh();
		deviceRepository.saveAndFlush(selectedDevice);
	}
	@FXML
	private void onMouseClicked() {
		String path = HttpDownloadDeviceImgTask.imgSavePath + MainCodeHelper.getIns().getMc(selectedDevice.getMainCodeId()) + ".png";
		try {
			Desktop.getDesktop().open(new File(path));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private ChangeListener<TreeItem<Device>> changeListener = new ChangeListener<TreeItem<Device>>() {

		@Override
		public void changed(ObservableValue<? extends TreeItem<Device>> observable, TreeItem<Device> oldValue,
				TreeItem<Device> newValue) {
			if (null != newValue) {
				selectedDevice = newValue.getValue();
//				logger.info("changed dev " + selectedDevice.getName());
				tfDeviceName.setText(selectedDevice.getName());
				labelMainCode.setText(MainCodeHelper.getIns().getMc(selectedDevice.getMainCodeId()));
				labelLongCoding.setText(selectedDevice.getLongCoding());
				if(selectedDevice instanceof SubDev) {
				    btnEditCoding.setVisible(false);
				}else {
				    btnEditCoding.setVisible(true);
				}
				cbVisibility.setSelected(!selectedDevice.isVisibility());
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

				if (canShowImg(selectedDevice)) {
					imageDevice.setVisible(true);
					String imgName = MainCodeHelper.getIns().getMc(selectedDevice.getMainCodeId()) + ".png";
					String path = "file:" + HttpDownloadDeviceImgTask.imgSavePath + imgName;
					try {
						imageDevice.setImage(new Image(path));
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					imageDevice.setVisible(false);
				}
			}
		}
	};
	
	private boolean canShowImg(Device dev) {
		String mc = MainCodeHelper.getIns().getMc(dev.getMainCodeId());
		for(DeviceImg di : listDeviceImg) {
			if(di.getCode().equals(mc)) {
				return true;
			}
		}
		return false;
	}
}
