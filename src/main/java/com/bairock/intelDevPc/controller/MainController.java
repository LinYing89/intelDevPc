package com.bairock.intelDevPc.controller;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.bairock.intelDevPc.IntelDevPcApplication;
import com.bairock.intelDevPc.MainStateView;
import com.bairock.intelDevPc.Util;
import com.bairock.intelDevPc.comm.MyMessageAnalysiser;
import com.bairock.intelDevPc.comm.PadClient;
import com.bairock.intelDevPc.data.Config;
import com.bairock.intelDevPc.data.DeviceValueHistory;
import com.bairock.intelDevPc.data.UILayoutConfig;
import com.bairock.intelDevPc.repository.ConfigRepository;
import com.bairock.intelDevPc.repository.UILayoutConfigRepository;
import com.bairock.intelDevPc.service.DeviceHistoryService;
import com.bairock.intelDevPc.service.DownloadService;
import com.bairock.intelDevPc.service.UploadService;
import com.bairock.intelDevPc.service.UserService;
import com.bairock.intelDevPc.view.DeviceHistoryHtml;
import com.bairock.intelDevPc.view.DevicesView;
import com.bairock.intelDevPc.view.DragDeviceView;
import com.bairock.intelDevPc.view.LinkageTable;
import com.bairock.intelDevPc.view.LinkageView;
import com.bairock.intelDevPc.view.SettingsView;
import com.bairock.intelDevPc.view.SortDeviceView;
import com.bairock.intelDevPc.view.StateDeviceGridView;
import com.bairock.intelDevPc.view.StateDeviceListView;
import com.bairock.intelDevPc.view.UpDownloadDialog;
import com.bairock.intelDevPc.view.ValueDeviceGridView;
import com.bairock.intelDevPc.view.ValueDeviceListView;
import com.bairock.intelDevPc.view.VirtualDeviceListView;
import com.bairock.iot.intelDev.communication.DevChannelBridge;
import com.bairock.iot.intelDev.communication.DevChannelBridgeHelper;
import com.bairock.iot.intelDev.communication.DevServer;
import com.bairock.iot.intelDev.communication.UdpServer;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.devcollect.DevCollect;
import com.bairock.iot.intelDev.order.LoginModel;
import com.bairock.iot.intelDev.user.DevGroup;

import de.felixroske.jfxsupport.FXMLController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
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

    public static Device selectedValueDev;

    @Autowired
    private MainStateView mainStateView;
    @Autowired
    private DeviceHistoryService deviceHistoryService;
    @Autowired
    private UploadService uploadService;
    @Autowired
    private DownloadService downloadService;
    @FXML
    private FlowPane fpSwitch;
    @FXML
    private AnchorPane paneStateDevContainer;
    // 虚拟设备容器
    @FXML
    private AnchorPane paneVirtualDevContainer;
    @FXML
    private AnchorPane paneValueDevContainer;
    @FXML
    private AnchorPane apaneValueLineChart;
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

//	@FXML
//	private LineChart<String,Number> chartValueHistory;
    @Autowired
    private UserService userService;
    @Autowired
    private Config config;
    @Autowired
    private UILayoutConfig uiConfig;
    @Autowired
    private ConfigRepository configRepository;
    @Autowired
    private UILayoutConfigRepository uiConfigRepository;
    @Autowired
    private DevicesView devicesView;
    @Autowired
    private DragDeviceView dragDeviceView;
    @Autowired
    private SortDeviceView sortDeviceView;
    @Autowired
    private LinkageView linkageView;
    @Autowired
    private LinkageTable linkageTableView;
    @Autowired
    private UpDownloadDialog upDownloadDialog;
    @Autowired
    private SettingsView settingsView;
    @Autowired
    private DeviceHistoryHtml deviceHistoryHtml;

    private StateDeviceListView stateDeviceListView;
    private StateDeviceGridView stateDeviceGridView;
    private ValueDeviceListView valueDeviceListView;
    private ValueDeviceGridView valueDeviceGridView;
    private VirtualDeviceListView virtualDeviceListView;

    private XYChart.Series<String, Number> seriesValueChart;
    private LineChart<String, Number> chartValueHistory;

    public void init() {
        userService.initUser();
        UserService.getDevGroup().setUserid(config.getUserid());
        UserService.getDevGroup().setName(config.getDevGroupName());
        UserService.getDevGroup().setPetName(config.getDevGroupPetname());

        if (Util.GUEST) {
            Util.CAN_CONNECT_SERVER = false;
        } else {
            Util.CAN_CONNECT_SERVER = true;
        }
        if (Util.CAN_CONNECT_SERVER) {
            if (!PadClient.getIns().isLinked()) {
                PadClient.getIns().link();
            }
        }

        initLocalConfig();

        mainStateView.getView().getScene().getWindow().setOnCloseRequest(e -> handlerExit());
        initToggleButton();
        refreshServerState(IntelDevPcApplication.SERVER_CONNECTED);

        Stage stage = IntelDevPcApplication.getStage();

        String groupName = "";
        if (null == config.getDevGroupPetname() || config.getDevGroupPetname().isEmpty()) {
            groupName = config.getDevGroupName();
        } else {
            groupName = config.getDevGroupPetname();
        }
        stage.setTitle(config.getUserid() + "-" + groupName);

        refreshDevicePane();

        initLineChart();

        splitePaneRoot.setDividerPositions(uiConfig.getDividerRoot());
        splitePaneDevice.setDividerPositions(uiConfig.getDividerDevice());
        splitePaneView.setDividerPositions(uiConfig.getDividerView());
    }

    public void reInit() {
        Platform.runLater(() -> refreshDevicePane());
    }

    private void initLineChart() {
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        chartValueHistory = new LineChart<String, Number>(xAxis, yAxis);
        chartValueHistory.setLegendVisible(false);
        chartValueHistory.setCreateSymbols(false);
        seriesValueChart = new XYChart.Series<>();

//        series.getData().add(new XYChart.Data<String, Number>("Jan", 23));
//        series.getData().add(new XYChart.Data<String, Number>("Feb", 14));
        chartValueHistory.getData().add(seriesValueChart);

        apaneValueLineChart.getChildren().add(chartValueHistory);
        AnchorPane.setLeftAnchor(chartValueHistory, 0.0);
        AnchorPane.setTopAnchor(chartValueHistory, 0.0);
        AnchorPane.setRightAnchor(chartValueHistory, 0.0);
        AnchorPane.setBottomAnchor(chartValueHistory, 0.0);

        DevGroup devGroup = UserService.user.getListDevGroup().get(0);
        List<DevCollect> listDev = devGroup.findListCollectDev(true);
        if (listDev.size() > 0) {
//			selectedValueDev = listDev.get(0);
//			refreshValueChartTitle(selectedValueDev.getName());

//			setHistoryChart();
        }
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
        refreshDevParamPane();
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

    public void refreshDevParamPane() {
        if (null == virtualDeviceListView) {
            virtualDeviceListView = new VirtualDeviceListView();
        }
        paneVirtualDevContainer.getChildren().clear();
        paneVirtualDevContainer.getChildren().add(virtualDeviceListView);
        virtualDeviceListView.updateItem();
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

    // 被踢掉显示提示
    public void showLogoutDialog() {
        Platform.runLater(() -> {
            Alert warning = new Alert(Alert.AlertType.WARNING, "该账号已在其他设备上本地登录!");
            warning.showAndWait();
            if (warning.getResult() == ButtonType.OK) {
                config.setAutoLogin(false);
                configRepository.saveAndFlush(config);
                handlerExit();
                return;
            }
        });

    }

    public void refreshValueChartTitle(String title) {
        chartValueHistory.setTitle(title);
    }

    public void removeValueHistory() {
        seriesValueChart.getData().clear();
        setHistoryChart();
    }

    public void setHistoryChart() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        // 获取今天历史纪录
        List<DeviceValueHistory> list = deviceHistoryService.find(selectedValueDev.getLongCoding(), c.getTime(),
                new Date());
        for (DeviceValueHistory h : list) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            String time = h.strTimeChartFormat();
            addValueHistoryToChart(time, h.getValue());
        }
    }

    public void addValueHistoryToChart(String time, float value) {
        Platform.runLater(() -> {
            if (seriesValueChart.getData().size() > 500) {
                seriesValueChart.getData().remove(0);
            }
            seriesValueChart.getData().add(new XYChart.Data<String, Number>(time, value));
        });
    }

    // 上传
    public void handleMenuUpload() {
//		UploadClient uploadClient = new UploadClient();
//		uploadClient.link();
        Alert warning = new Alert(Alert.AlertType.WARNING, "上传将用本地数据覆盖服务器上的数据, 并且不可恢复, 确定上传吗?");
        warning.showAndWait();
        if (warning.getResult() != ButtonType.OK) {
            return;
        }

        uploadService.upload();
//		HttpUploadTask task = new HttpUploadTask(UserService.getDevGroup(), config.getServerName());
//		task.setOnExecutedListener(loginResult -> {
//			Platform.runLater(() -> uploadResult(loginResult));
//		});
//		task.start();

        ((UpDownloadDialogController) upDownloadDialog.getPresenter()).init(UpDownloadDialogController.UPLOAD);
        IntelDevPcApplication.showView(UpDownloadDialog.class, Modality.WINDOW_MODAL);
    }

    // 下载
    public void handleMenuDownload() {
        Alert warning = new Alert(Alert.AlertType.WARNING, "下载将覆盖本地的设备信息, 并且不可恢复, 确定下载吗?");
        warning.showAndWait();
        if (warning.getResult() != ButtonType.OK) {
            return;
        }

        downloadService.download();

//		HttpDownloadTask task = new HttpDownloadTask(config.getServerName(), config.getUserid(),
//				config.getDevGroupName());
//		task.setOnExecutedListener(loginResult -> {
//			Platform.runLater(() -> downloadResult(loginResult));
//		});
//		task.start();
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

    @FXML
    public void menuLinkage() {
        System.out.println("menuLinkage");
        ((LinkageController) linkageView.getPresenter()).init();
        IntelDevPcApplication.showView(LinkageView.class, Modality.NONE);
    }

    @FXML
    public void menuLinkageTable() {
        LinkageTableCtrler ctrler = (LinkageTableCtrler) linkageTableView.getPresenter();
        ctrler.init();
        IntelDevPcApplication.showView(LinkageTable.class, Modality.NONE);
        ctrler.removeOnCheckTableListener();
    }

    @FXML
    public void menuSettingsAction() {
        ((SettingsController) settingsView.getPresenter()).init();
        IntelDevPcApplication.showView(SettingsView.class, Modality.WINDOW_MODAL);
    }

    // 退出
    @FXML
    public void menuExitAction() {
        config.setAutoLogin(false);
        configRepository.saveAndFlush(config);
        handlerExit();
    }

    // 历史纪录
    @FXML
    public void menuDeviceHistory() {
//		((DeviceHistoryCtrler) deviceHistoryView.getPresenter()).init();
//		IntelDevPcApplication.showView(DeviceHistoryView.class, Modality.WINDOW_MODAL);

        ((DeviceHistoryHtmlCtrler) deviceHistoryHtml.getPresenter()).init();
        IntelDevPcApplication.showView(DeviceHistoryHtml.class, Modality.WINDOW_MODAL);
    }

    private void handlerExit() {
        // 窗口关闭时保存当前布局
        uiConfig.setDividerRoot(splitePaneRoot.getDividerPositions()[0]);
        uiConfig.setDividerDevice(splitePaneDevice.getDividerPositions()[0]);
        uiConfig.setDividerView(splitePaneView.getDividerPositions()[0]);
        uiConfigRepository.saveAndFlush(uiConfig);
        System.exit(0);
    }

    // 组态视图
    @FXML
    public void onDragItemAction() {
        ((DragDeviceCtrler) dragDeviceView.getPresenter()).init();
        IntelDevPcApplication.showViewMax(DragDeviceView.class, "组态视图");
    }

    // 组态视图
    @FXML
    public void onDragViewAction() {
        ((DragDeviceCtrler) dragDeviceView.getPresenter()).init();
        IntelDevPcApplication.showViewMax(DragDeviceView.class, "组态视图");
    }

    private void initLocalConfig() {
        if (null == config.getLoginModel()) {
            Alert warning = new Alert(Alert.AlertType.WARNING, "登录过期, 请退出重新登录.");
            warning.showAndWait();
            if (warning.getResult() != ButtonType.OK) {
                config.setAutoLogin(false);
                configRepository.saveAndFlush(config);
                handlerExit();
                return;
            }
        }

        if (config.getLoginModel().equals(LoginModel.LOCAL)) {
            UdpServer.getIns().run();

            try {
                DevServer.getIns().run();
            } catch (Exception e) {
                e.printStackTrace();
            }

            DevChannelBridge.analysiserName = MyMessageAnalysiser.class.getName();
            DevChannelBridgeHelper.getIns().stopSeekDeviceOnLineThread();
            DevChannelBridgeHelper.getIns().startSeekDeviceOnLineThread();
        }
    }
}
