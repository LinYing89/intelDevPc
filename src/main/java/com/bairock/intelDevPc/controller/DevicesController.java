package com.bairock.intelDevPc.controller;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import com.bairock.intelDevPc.IntelDevPcApplication;
import com.bairock.intelDevPc.SpringUtil;
import com.bairock.intelDevPc.Util;
import com.bairock.intelDevPc.repository.DeviceImgRepo;
import com.bairock.intelDevPc.repository.DeviceRepository;
import com.bairock.intelDevPc.repository.DragDeviceRepository;
import com.bairock.intelDevPc.repository.LinkageConditionRepository;
import com.bairock.intelDevPc.repository.LinkageEffectRepository;
import com.bairock.intelDevPc.service.DragDeviceService;
import com.bairock.intelDevPc.service.UserService;
import com.bairock.intelDevPc.view.AddDeviceView;
import com.bairock.intelDevPc.view.CtrlModelDialogView;
import com.bairock.intelDevPc.view.EditDeviceCoding;
import com.bairock.iot.intelDev.communication.FindDevHelper;
import com.bairock.iot.intelDev.data.DeviceImg;
import com.bairock.iot.intelDev.data.DragDevice;
import com.bairock.iot.intelDev.data.DragDeviceHelper;
import com.bairock.iot.intelDev.device.Coordinator;
import com.bairock.iot.intelDev.device.CtrlModel;
import com.bairock.iot.intelDev.device.DevHaveChild;
import com.bairock.iot.intelDev.device.DevHaveChild.OnDeviceCollectionChangedListener;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.MainCodeHelper;
import com.bairock.iot.intelDev.device.XRoadDevice;
import com.bairock.iot.intelDev.device.devcollect.DevCollectSignalContainer;
import com.bairock.iot.intelDev.device.devswitch.DevSwitch;
import com.bairock.iot.intelDev.device.devswitch.SubDev;
import com.bairock.iot.intelDev.http.HttpDownloadDeviceImgTask;
import com.bairock.iot.intelDev.linkage.Effect;
import com.bairock.iot.intelDev.linkage.LinkageCondition;
import com.bairock.iot.intelDev.linkage.LinkageHelper;
import com.bairock.iot.intelDev.linkage.OnRemovedListener;
import com.bairock.iot.intelDev.linkage.guagua.GuaguaHelper;
import com.bairock.iot.intelDev.user.DevGroup;

import de.felixroske.jfxsupport.FXMLController;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

@FXMLController
public class DevicesController {

//	private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private CtrlModelDialogView ctrlModelDialogView;
    @Autowired
    private AddDeviceView addDeviceView;
    @Autowired
    private MainController mainController;
    @Autowired
    private DeviceImgRepo deviceImgRepo;
    private List<DeviceImg> listDeviceImg;

    @FXML
    private TreeView<Device> treeViewDevices;
    @FXML
    private Pane paneRight;
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
    @Autowired
    private DragDeviceService dragDeviceService;
    @Autowired
    private DragDeviceRepository dragDeviceRepository;
    @Autowired
    private LinkageConditionRepository linkageConditionRepository;
    @Autowired
    private LinkageEffectRepository linkageEffectRepository;

    private boolean inited;

    private ContextMenu treeviewContextMenu = new ContextMenu();

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
                        if (value.getId().equals("0")) {
                            this.setText(value.getName());
                        } else {
                            String ctrlModel = Util.getCtrlModelName(value.getCtrlModel());
                            this.setText(value.getName() + " (" + ctrlModel + ")");
                        }
                    }
                }

            };
            return cell;
        });

        treeViewDevices.setOnContextMenuRequested(v -> {
            if (null == selectedDevice || selectedDevice instanceof SubDev) {
                return;
            }
            treeviewContextMenu.getItems().clear();
            if (selectedDevice.getId().equals("0") || selectedDevice instanceof Coordinator) {
                MenuItem menuAdd = new MenuItem("添加设备");
                treeviewContextMenu.getItems().add(menuAdd);
                menuAdd.setOnAction(e -> {
                    addDevice();
                });
            }
            if (selectedDevice instanceof DevSwitch || selectedDevice instanceof DevCollectSignalContainer) {
                MenuItem menuRebuildChild = new MenuItem("修改子设备个数");
                treeviewContextMenu.getItems().add(menuRebuildChild);
                menuRebuildChild.setOnAction(e -> {
                    rebuildChild();
                });
            }
            if (!selectedDevice.getId().equals("0")) {
                MenuItem menuDelete = new MenuItem("删除设备");
                treeviewContextMenu.getItems().add(menuDelete);
                menuDelete.setOnAction(e -> {
                    onDeleteDeviceAction();
                });
            }

            treeviewContextMenu.show(treeViewDevices, v.getScreenX(), v.getScreenY());
        });

        cbVisibility.selectedProperty().addListener((p1, p2, p3) -> {
            if (selectedDevice.isVisibility() == p3) {
                selectedDevice.setVisibility(!p3);
                deviceRepository.saveAndFlush(selectedDevice);
                mainController.refreshDevicePane();
            }
        });
    }

    private void addDevice() {
        if (selectedDevice.getId().equals("0")) {
            AddDeviceController.PARENT_DEVICE = null;
        } else {
            AddDeviceController.PARENT_DEVICE = (DevHaveChild) selectedDevice;
        }
        ((AddDeviceController) addDeviceView.getPresenter()).init();
        IntelDevPcApplication.showView(AddDeviceView.class, Modality.WINDOW_MODAL);
    }

    public void rebuildChild() {
        VBox box = new VBox();
        box.setPrefHeight(300);
        box.setPrefWidth(300);
        box.setSpacing(10);

        Label label = new Label("请输入子设备个数:");
        box.getChildren().add(label);
        TextField txtCount = new TextField();
        txtCount.setPrefWidth(260);
        txtCount.setMaxWidth(260);
        box.getChildren().add(txtCount);

        Button btnOk = new Button("确定");
        btnOk.setPrefWidth(260);

        box.getChildren().add(btnOk);
        Scene scene = new Scene(box);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("修改子设备个数");

        btnOk.setOnAction(e -> {
            DevHaveChild devHaveChild = ((DevHaveChild) selectedDevice);
            try {
                devHaveChild.addOnDeviceCollectionChangedListener(onDeviceCollectionChangedListener);
                int count = Integer.parseInt(txtCount.getText());
                ((XRoadDevice) selectedDevice).rebuildChildren(count);
                init();
                mainController.refreshDevicePane();
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            } finally {
                devHaveChild.removeOnDeviceCollectionChangedListener(onDeviceCollectionChangedListener);
            }

            stage.close();
        });

        stage.showAndWait();
    }

    private OnDeviceCollectionChangedListener onDeviceCollectionChangedListener = new OnDeviceCollectionChangedListener() {

        @Override
        public void onRemoved(Device device) {
            deleteDevice(device);
        }

        @Override
        public void onAdded(Device device) {
            deviceRepository.save(device);
            DragDevice dragDevice = new DragDevice();
            dragDevice.setId(UUID.randomUUID().toString());
            dragDevice.setDeviceId(device.getId());
            dragDeviceService.insert(dragDevice);
            dragDevice.setDevice(device);
            DragDeviceHelper.getIns().addDragDevice(dragDevice);
        }
    };

    public void init() {

        if (!inited) {
            inited = true;
            init1();
        }

        treeViewDevices.setRoot(null);
        treeViewDevices.setShowRoot(true);

        Device devRoot = new Device();
        devRoot.setId("0");
        devRoot.setName("所有设备");
        TreeItem<Device> root = new TreeItem<>(devRoot);
        root.setExpanded(true);
        treeViewDevices.setRoot(root);

        DevGroup devGroup = UserService.user.getListDevGroup().get(0);
        List<Device> listDevice = devGroup.getListDevice();
        sortListDevice(listDevice);
        for (Device dev : listDevice) {
            initDevTree(dev, root);
        }

    }

    // 按设备编码排序
    private void sortListDevice(List<Device> listDevice) {
//        Collections.sort(listDevice);
        listDevice.sort((a, b) -> a.getCoding().compareTo(b.getCoding()));
        for (Device dev : listDevice) {
            if (dev instanceof DevHaveChild) {
                if (dev instanceof DevSwitch || dev instanceof DevCollectSignalContainer) {
                    // 开关子设备按次设备号排序
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
        if (EditDeviceCodingController.newSubCoding != null) {
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

//    @FXML
    private void onDeleteDeviceAction() {
        Alert warning = new Alert(Alert.AlertType.WARNING, "确定删除设备: [" + selectedDevice.getName() + "] 吗?");
        warning.showAndWait();
        if (warning.getResult() != ButtonType.OK) {
            return;
        }

        deleteDevice(selectedDevice);

        init();
        mainController.refreshDevicePane();
    }

    private void deleteDevice(Device device) {
        try {
            // 删除连锁信息
            LinkageHelper.getIns().setOnRemovedListener(onRemovedListener);
            GuaguaHelper.getIns().setOnRemovedListener(onRemovedListener);

            LinkageHelper.getIns().removeDevice(device);
            GuaguaHelper.getIns().removeDevice(device);

            // 删除组态设备
            List<DragDevice> dragDevices = DragDeviceHelper.getIns().findDragDevice(device);
            for (DragDevice dd : dragDevices) {
                DragDeviceHelper.getIns().removeDragDevice(dd);
                dragDeviceRepository.deleteById(dd.getId());
            }

            UserService.getDevGroup().removeDevice(device);
            deviceRepository.deleteById(device.getId());

            if (null == device.getParent() || !(device.findSuperParent() instanceof Coordinator)) {
                FindDevHelper.getIns().alreadyFind(device.findSuperParent().getCoding());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            LinkageHelper.getIns().setOnRemovedListener(null);
            GuaguaHelper.getIns().setOnRemovedListener(null);
        }
    }

    private OnRemovedListener onRemovedListener = new OnRemovedListener() {

        @Override
        public void onLinkageConditionRemoved(LinkageCondition condition) {
            linkageConditionRepository.deleteById(condition.getId());
        }

        @Override
        public void onEffectRemoved(Effect effect) {
            linkageEffectRepository.deleteById(effect.getId());
        }
    };

    @FXML
    private void onMouseClicked() {
        String path = HttpDownloadDeviceImgTask.imgSavePath
                + MainCodeHelper.getIns().getMc(selectedDevice.getMainCodeId()) + ".png";
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
                Device device = newValue.getValue();

                selectedDevice = device;
                if (device.getId().equals("0")) {
                    paneRight.setDisable(true);
                    return;
                }
                paneRight.setDisable(false);
//				logger.info("changed dev " + selectedDevice.getName());
                tfDeviceName.setText(selectedDevice.getName());
                labelMainCode.setText(MainCodeHelper.getIns().getMc(selectedDevice.getMainCodeId()));
                labelLongCoding.setText(selectedDevice.getLongCoding());
                if (selectedDevice instanceof SubDev) {
                    btnEditCoding.setVisible(false);
                } else {
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
        for (DeviceImg di : listDeviceImg) {
            if (di.getCode().equals(mc)) {
                return true;
            }
        }
        return false;
    }

    @FXML
    private void treevewOnMouseClicked() {
        treeviewContextMenu.hide();
    }
}
