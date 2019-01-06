package com.bairock.intelDevPc.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.bairock.intelDevPc.IntelDevPcApplication;
import com.bairock.intelDevPc.MainStateView;
import com.bairock.intelDevPc.comm.DownloadClient;
import com.bairock.intelDevPc.comm.UploadClient;
import com.bairock.intelDevPc.data.Config;
import com.bairock.intelDevPc.data.UILayoutConfig;
import com.bairock.intelDevPc.repository.UILayoutConfigRepository;
import com.bairock.intelDevPc.service.UserService;
import com.bairock.intelDevPc.view.DevicesView;
import com.bairock.intelDevPc.view.LinkageView;
import com.bairock.intelDevPc.view.SettingsView;
import com.bairock.intelDevPc.view.SortDeviceView;
import com.bairock.intelDevPc.view.StateDeviceGridView;
import com.bairock.intelDevPc.view.StateDeviceListView;
import com.bairock.intelDevPc.view.UpDownloadDialog;
import com.bairock.intelDevPc.view.ValueDeviceGridView;
import com.bairock.intelDevPc.view.ValueDeviceListView;
import com.bairock.iot.intelDev.user.DevGroup;

import de.felixroske.jfxsupport.FXMLController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

@FXMLController
public class MainController {

//	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private MainStateView mainStateView;
	@FXML
	private FlowPane fpSwitch;
	@FXML
	private AnchorPane paneStateDevContainer;
	@FXML
	private AnchorPane paneValueDevContainer;
	@FXML
	private FlowPane fpCollector;
	@FXML
	private Label labelServerState;
	@FXML
	private ToggleGroup tgStateDevLayout;
	@FXML
	private ToggleButton tbStateDevGrid;
	@FXML
	private ToggleButton tbStateDevList;
	@FXML
	private ToggleGroup tgValueDevLayout;
	@FXML
	private ToggleButton tbValueDevGrid;
	@FXML
	private ToggleButton tbValueDevList;

	@FXML
	private SplitPane splitePaneRoot;
	@FXML
	private SplitPane splitePaneDevice;
	@FXML
	private SplitPane splitePaneView;

	@Autowired
	private Config config;
	@Autowired
	private UILayoutConfig uiConfig;
	@Autowired
	private UILayoutConfigRepository configRepository;
	@Autowired
	private UILayoutConfigRepository uiConfigRepository;
	@Autowired
	private DevicesView devicesView;
	@Autowired
	private SortDeviceView sortDeviceView;
	@Autowired
	private LinkageView linkageView;
	@Autowired
	private UpDownloadDialog upDownloadDialog;
	@Autowired
	private SettingsView settingsView;

	private StateDeviceListView stateDeviceListView;
	private StateDeviceGridView stateDeviceGridView;
	private ValueDeviceListView valueDeviceListView;
	private ValueDeviceGridView valueDeviceGridView;

	public void init() {

		splitePaneRoot.setDividerPositions(uiConfig.getDividerRoot());
		splitePaneDevice.setDividerPositions(uiConfig.getDividerDevice());
		splitePaneView.setDividerPositions(uiConfig.getDividerView());
		mainStateView.getView().getScene().getWindow().setOnCloseRequest(e -> handlerExit());
		initToggleButton();
		refreshServerState(IntelDevPcApplication.SERVER_CONNECTED);
		
		DevGroup group = UserService.getDevGroup();
		Stage stage = IntelDevPcApplication.getStage();
		stage.setTitle(config.getAppTitle() + " - " + UserService.user.getName() + "-" + group.getName() + ":"
				+ group.getPetName());
		
		refreshDevicePane();
	}

	public void reInit() {
		Platform.runLater(() -> refreshDevicePane());
	}

	private void initToggleButton() {
		tgStateDevLayout.selectedToggleProperty().addListener((p0, p1, p2) -> {
			if (null != p2) {
				if (p2 == tbStateDevGrid) {
					uiConfig.setStateDevLayout(UILayoutConfig.LAYOUT_GRID);
				} else {
					uiConfig.setStateDevLayout(UILayoutConfig.LAYOUT_LIST);
				}
				uiConfigRepository.saveAndFlush(uiConfig);
				refreshStateDevicePane();
			}
		});

		tgValueDevLayout.selectedToggleProperty().addListener((p0, p1, p2) -> {
			if (null != p2) {
				if (p2 == tbValueDevGrid) {
					uiConfig.setValueDevLayout(UILayoutConfig.LAYOUT_GRID);
				} else {
					uiConfig.setValueDevLayout(UILayoutConfig.LAYOUT_LIST);
				}
				uiConfigRepository.saveAndFlush(uiConfig);
				refreshValueDevicePane();
			}
		});

		if (uiConfig.getStateDevLayout() == UILayoutConfig.LAYOUT_GRID) {
			tbStateDevGrid.setSelected(true);
		} else {
			tbStateDevList.setSelected(true);
		}

		if (uiConfig.getValueDevLayout() == UILayoutConfig.LAYOUT_GRID) {
			tbValueDevGrid.setSelected(true);
		} else {
			tbValueDevList.setSelected(true);
		}
	}

	public void refreshDevicePane() {
		refreshValueDevicePane();
		refreshStateDevicePane();
	}

	public void refreshStateDevicePane() {
		if (uiConfig.getStateDevLayout() == UILayoutConfig.LAYOUT_GRID) {
			if (null == stateDeviceGridView) {
				stateDeviceGridView = new StateDeviceGridView();
			}
			paneStateDevContainer.getChildren().clear();
			paneStateDevContainer.getChildren().add(stateDeviceGridView);
			stateDeviceGridView.updateItem();
			if (null != stateDeviceListView) {
				stateDeviceListView.destory();
				stateDeviceListView = null;
			}
		} else {
			if (null == stateDeviceListView) {
				stateDeviceListView = new StateDeviceListView();
			}
			paneStateDevContainer.getChildren().clear();
			paneStateDevContainer.getChildren().add(stateDeviceListView);
			stateDeviceListView.updateItem();
			if (null != stateDeviceGridView) {
				stateDeviceGridView.destory();
				stateDeviceGridView = null;
			}
		}
	}

	public void refreshValueDevicePane() {
		if (uiConfig.getValueDevLayout() == UILayoutConfig.LAYOUT_GRID) {
			if (null == valueDeviceGridView) {
				valueDeviceGridView = new ValueDeviceGridView();
			}
			paneValueDevContainer.getChildren().clear();
			paneValueDevContainer.getChildren().add(valueDeviceGridView);
			valueDeviceGridView.updateItem();
			if (null != valueDeviceListView) {
				valueDeviceListView.destory();
				valueDeviceListView = null;
			}
		} else {
			if (null == valueDeviceListView) {
				valueDeviceListView = new ValueDeviceListView();
			}
			paneValueDevContainer.getChildren().clear();
			paneValueDevContainer.getChildren().add(valueDeviceListView);
			valueDeviceListView.updateItem();
			if (null != valueDeviceGridView) {
				valueDeviceGridView.destory();
				valueDeviceGridView = null;
			}
		}
	}

	/**
	 * 刷新服务器连接状态
	 * 
	 * @param state
	 */
	public void refreshServerState(boolean state) {
		if (null == labelServerState) {
			return;
		}
		String text;
		if (state) {
			text = "已连接";
			labelServerState.setStyle("-fx-text-fill : #008B45;");
		} else {
			text = "未连接";
			labelServerState.setStyle("-fx-text-fill : #CD6839;");
		}
		Platform.runLater(() -> labelServerState.setText(text));
	}

	// 上传
	public void handleMenuUpload() {
		UploadClient uploadClient = new UploadClient();
		uploadClient.link();
		((UpDownloadDialogController) upDownloadDialog.getPresenter()).init(UpDownloadDialogController.UPLOAD);
		IntelDevPcApplication.showView(UpDownloadDialog.class, Modality.WINDOW_MODAL);
	}

	// 下载
	public void handleMenuDownload() {
		DownloadClient download = new DownloadClient();
		download.link();
		((UpDownloadDialogController) upDownloadDialog.getPresenter()).init(UpDownloadDialogController.DOWNLOAD);
		IntelDevPcApplication.showView(UpDownloadDialog.class, Modality.WINDOW_MODAL);
	}

	public void menuAllDevices() {
		System.out.println("menuAllDevices");
		((DevicesController) devicesView.getPresenter()).init();
		IntelDevPcApplication.showView(DevicesView.class, Modality.NONE);
	}

	public void menuSortDevices() {
		((SortDeviceController) sortDeviceView.getPresenter()).init();
		IntelDevPcApplication.showView(SortDeviceView.class, Modality.NONE);
	}

	public void menuLinkage() {
		System.out.println("menuLinkage");
		((LinkageController) linkageView.getPresenter()).init();
		IntelDevPcApplication.showView(LinkageView.class, Modality.NONE);
	}

	@FXML
	public void menuSettingsAction() {
		((SettingsController) settingsView.getPresenter()).init();
		IntelDevPcApplication.showView(SettingsView.class, Modality.WINDOW_MODAL);
	}
	
	private void handlerExit() {
		//窗口关闭时保存当前布局
		uiConfig.setDividerRoot(splitePaneRoot.getDividerPositions()[0]);
		uiConfig.setDividerDevice(splitePaneDevice.getDividerPositions()[0]);
		uiConfig.setDividerView(splitePaneView.getDividerPositions()[0]);
		configRepository.saveAndFlush(uiConfig);
		System.exit(0);
	}
}
