package com.bairock.intelDevPc.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.bairock.intelDevPc.IntelDevPcApplication;
import com.bairock.intelDevPc.comm.DownloadClient;
import com.bairock.intelDevPc.comm.UploadClient;
import com.bairock.intelDevPc.service.UserService;
import com.bairock.intelDevPc.view.CollectorPane;
import com.bairock.intelDevPc.view.DevicePane;
import com.bairock.intelDevPc.view.DevicesView;
import com.bairock.intelDevPc.view.LinkageView;
import com.bairock.intelDevPc.view.UpDownloadDialog;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.devcollect.DevCollect;
import com.bairock.iot.intelDev.user.DevGroup;

import de.felixroske.jfxsupport.FXMLController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;

@FXMLController
public class MainController {

//	private Logger logger = LoggerFactory.getLogger(getClass());
	
	public MainController() {
		
	}
	
	@FXML
	private FlowPane fpSwitch;
	@FXML
	private FlowPane fpCollector;
	@FXML
	private Label labelServerState;
	
	@Autowired
	DevicesView devicesView;
	@Autowired
	LinkageView linkageView;
	@Autowired
	private UpDownloadDialog upDownloadDialog;
	
//	@Autowired
//	private DevicePane devPane;
	
	public void init() {
		fpSwitch.getChildren().clear();
		fpCollector.getChildren().clear();
		
		DevGroup devGroup = UserService.user.getListDevGroup().get(0);
		List<Device> listIStateDev = devGroup.findListIStateDev(true);
		Collections.sort(listIStateDev);
		for(Device dev : listIStateDev) {
			DevicePane dp = new DevicePane(dev);
			fpSwitch.getChildren().add(dp);
		}
		List<DevCollect> listDevCollector = devGroup.findListCollectDev(true);
		Collections.sort(listDevCollector);
		for(DevCollect dev : listDevCollector) {
			CollectorPane dp = new CollectorPane(dev);
			fpCollector.getChildren().add(dp);
		}
		refreshServerState(IntelDevPcApplication.SERVER_CONNECTED);
		
//		DevSwitch dev = (DevSwitch) DeviceAssistent.createDeviceByMcId(MainCodeHelper.KG_3LU_2TAI, "0001");
//		DevicePane dp = new DevicePane((SubDev)(dev.getSubDevBySc("1")));
//		
//		DevicePane dp2 = new DevicePane((SubDev)(dev.getSubDevBySc("2")));
//		fpSwitch.getChildren().addAll(dp, dp2);
	}
	
	public void reInit() {
		Platform.runLater(()->init());
	}
	
	public void refreshDevicePaneGear(Device dev) {
		for(Node node : fpSwitch.getChildren()) {
			DevicePane dp = (DevicePane) node;
			if(dp.getDevice() == dev) {
				dp.refreshGear();
				break;
			}
		}
	}
	
	public void refreshDevicePaneState(Device dev) {
		for(Node node : fpSwitch.getChildren()) {
			DevicePane dp = (DevicePane) node;
			if(dp.getDevice() == dev) {
				dp.refreshState();
				return;
			}
		}
		for(Node node : fpCollector.getChildren()) {
			CollectorPane dp = (CollectorPane) node;
			if(dp.getDevice() == dev) {
				dp.refreshState();
				return;
			}
		}
	}
	
	/**
	 * 刷新服务器连接状态
	 * @param state
	 */
	public void refreshServerState(boolean state) {
		if(null == labelServerState) {
			return;
		}
		String text;
		if(state) {
			text = "已连接";
			labelServerState.setStyle("-fx-text-fill : #008B45;");
		}else {
			text = "未连接";
			labelServerState.setStyle("-fx-text-fill : #CD6839;");
		}
		Platform.runLater(()->labelServerState.setText(text));
	}
	
	//上传
	public void handleMenuUpload() {
		UploadClient uploadClient = new UploadClient();
        uploadClient.link();
		((UpDownloadDialogController)upDownloadDialog.getPresenter()).init(UpDownloadDialogController.UPLOAD);
		IntelDevPcApplication.showView(UpDownloadDialog.class, Modality.WINDOW_MODAL);
	}
	
	//下载
	public void handleMenuDownload() {
		DownloadClient download = new DownloadClient();
		download.link();
		((UpDownloadDialogController)upDownloadDialog.getPresenter()).init(UpDownloadDialogController.DOWNLOAD);
		IntelDevPcApplication.showView(UpDownloadDialog.class, Modality.WINDOW_MODAL);
	}
	
	public void menuAllDevices(){
		System.out.println("menuAllDevices");
		((DevicesController)devicesView.getPresenter()).init();
		IntelDevPcApplication.showView(DevicesView.class, Modality.NONE);
	}
	
	public void menuLinkage(){
		System.out.println("menuLinkage");
		((LinkageController)linkageView.getPresenter()).init();
		IntelDevPcApplication.showView(LinkageView.class, Modality.NONE);
	}
}
