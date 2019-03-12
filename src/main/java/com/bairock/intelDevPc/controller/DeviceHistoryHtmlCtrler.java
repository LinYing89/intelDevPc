package com.bairock.intelDevPc.controller;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;

import com.bairock.intelDevPc.ExcelUtil;
import com.bairock.intelDevPc.IntelDevPcApplication;
import com.bairock.intelDevPc.data.ChartPlotBands;
import com.bairock.intelDevPc.data.ChartValueHistory2;
import com.bairock.intelDevPc.data.DeviceValueHistory;
import com.bairock.intelDevPc.service.DeviceHistoryService;
import com.bairock.intelDevPc.service.UserService;
import com.bairock.intelDevPc.view.DeviceHistoryHtml;
import com.bairock.intelDevPc.view.ExportExcelDialog;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.devcollect.DevCollect;
import com.bairock.iot.intelDev.user.DevGroup;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.felixroske.jfxsupport.FXMLController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.util.StringConverter;

@FXMLController
public class DeviceHistoryHtmlCtrler {

	@Autowired
	private DeviceHistoryService deviceHistoryService;
	@Autowired
	private DeviceHistoryHtml deviceHistoryHtml;
	@Autowired
	private ExportExcelDialog exportExcelDialog;

	@FXML
	private ChoiceBox<Device> choiceBoxDevice;
	@FXML
	private DatePicker datePickerFrom;
	@FXML
	private DatePicker datePickerTo;
	@FXML
	private WebView webview;

	@FXML
	private TableView<DeviceValueHistory> tableHistory;
	@FXML
	private TableColumn<DeviceValueHistory, Date> tableColumnTime;
	@FXML
	private TableColumn<DeviceValueHistory, String> tableColumnDeviceName;
	@FXML
	private TableColumn<DeviceValueHistory, Float> tableColumnValue;

	private WebEngine webEngine;

	private List<Device> listDevice = new ArrayList<>();
	private List<DeviceValueHistory> listHistory;
	private LocalDate ldFrom;
	private LocalDate ldTo;
	private Device selectedDevice;

	private boolean inited;

	private void init1() {
		tableColumnTime.setCellValueFactory(new PropertyValueFactory<>("historyTime"));
		tableColumnDeviceName.setCellValueFactory(new PropertyValueFactory<>("deviceName"));
		tableColumnValue.setCellValueFactory(new PropertyValueFactory<>("value"));
		choiceBoxDevice.setConverter(new StringConverter<Device>() {
			@Override
			public String toString(Device object) {
				return object.getName();
			}

			@Override
			public Device fromString(String string) {
				return null;
			}
		});
		datePickerFrom.setValue(LocalDate.now());
		datePickerTo.setValue(LocalDate.now());
	}

	public void init() {

		if (!inited) {
			inited = true;
			init1();
		}
		listDevice.clear();
		DevGroup devGroup = UserService.getDevGroup();
		List<Device> listIStateDev = devGroup.findListIStateDev(true);
		listDevice.addAll(listIStateDev);
		List<DevCollect> listDev = devGroup.findListCollectDev(true);
		listDevice.addAll(listDev);

		choiceBoxDevice.getItems().clear();
		choiceBoxDevice.getItems().addAll(listDevice);
		if (null != selectedDevice) {
			choiceBoxDevice.getSelectionModel().select(selectedDevice);
		}

		webEngine = webview.getEngine();
//		webEngine.load("https://www.highcharts.com.cn/demo/highcharts/line-time-series");
		String url = DeviceHistoryHtmlCtrler.class.getResource("/html/deviceHistory2.html").toExternalForm();
		webEngine.load(url);

	}

	public static ObservableList<DeviceValueHistory> getDeviceValueHistoryList(List<DeviceValueHistory> list) {
		return FXCollections.<DeviceValueHistory>observableArrayList(list);
	}
	
	// 查询按钮
	@FXML
	private void onBtnSearchAction() {
		selectedDevice = choiceBoxDevice.getSelectionModel().getSelectedItem();
		ldFrom = datePickerFrom.getValue();
		ldTo = datePickerTo.getValue();
		if (selectedDevice == null || ldFrom == null || ldTo == null) {
			return;
		}
		ZonedDateTime zonedDateTime = ldFrom.atStartOfDay(ZoneId.systemDefault());
		Date dateFrom = Date.from(zonedDateTime.toInstant());
		ZonedDateTime zonedDateTimeTo = ldTo.atStartOfDay(ZoneId.systemDefault());
		Date dateTo = Date.from(zonedDateTimeTo.toInstant());
		Calendar cTo = Calendar.getInstance();
		cTo.setTime(dateTo); // 设置时间
		cTo.set(Calendar.HOUR_OF_DAY, 23);
		cTo.set(Calendar.MINUTE, 59);
		cTo.set(Calendar.SECOND, 59);
		dateTo = cTo.getTime(); // 结果

		listHistory = deviceHistoryService.find(selectedDevice.getLongCoding(), dateFrom, dateTo);

		List<ChartValueHistory2> list = new ArrayList<>();
		List<ChartPlotBands> listPlotBands = new ArrayList<>();
		for (int i = 0; i < listHistory.size(); i++) {
			DeviceValueHistory dv = listHistory.get(i);
			list.add(new ChartValueHistory2(dv.getHistoryTime().toString(), dv.getValue()));
			if (i != listHistory.size() - 1 && dv.isAbnormal()) {
				ChartPlotBands cpb = new ChartPlotBands(i, i + 1);
				listPlotBands.add(cpb);
			}
		}
		ObjectMapper om = new ObjectMapper();
		try {
			webEngine.executeScript("setChartTitle('" + selectedDevice.getName() + "')");
			String json = om.writeValueAsString(list);
			if (selectedDevice instanceof DevCollect) {
				webEngine.executeScript("setValueData('" + json + "')");
			} else {
				webEngine.executeScript("setStateData('" + json + "')");
			}
			String jsonBands = om.writeValueAsString(listPlotBands);
			webEngine.executeScript("setPlotBands('" + jsonBands + "')");
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		tableHistory.setItems(getDeviceValueHistoryList(listHistory));

	}

	private String getStrTime(LocalDate ld) {
		return ld.format(DateTimeFormatter.BASIC_ISO_DATE);
	}
	
	// 导出按钮
	@FXML
	private void onBtnExportAction() {
		if (null == ldFrom || null == ldTo) {
			return;
		}
		if (null == listHistory || listHistory.isEmpty()) {
			return;
		}
		DeviceValueHistory dvFirst = listHistory.get(0);

		// excel标题
		String[] title = { "时间", "设备", "值" };
		// excel文件名
		String fileName = dvFirst.getDeviceName() + getStrTime(ldFrom) + "-" + getStrTime(ldTo) + ".xls";
		// sheet名
		String sheetName = "历史纪录表";

		String[][] content = new String[listHistory.size()][];
		for (int i = 0; i < listHistory.size(); i++) {
			content[i] = new String[title.length];
			DeviceValueHistory obj = listHistory.get(i);
			content[i][0] = obj.strTimeFormat();
			content[i][1] = obj.getDeviceName();
			content[i][2] = String.valueOf(obj.getValue());
		}

		// 创建HSSFWorkbook
		HSSFWorkbook wb = ExcelUtil.getHSSFWorkbook(sheetName, title, content, null);

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("保存历史纪录");
		fileChooser.setInitialFileName(fileName);
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XLS Files", "*.xls"));
		File file = fileChooser.showSaveDialog(deviceHistoryHtml.getView().getScene().getWindow());
		if (file != null) {

			((ExportExcelDialogCtrler) exportExcelDialog.getPresenter()).init(file.getAbsolutePath());
			new Thread(() -> {
				try {
					Thread.sleep(1000);
					ExcelUtil.outputExcel(wb, file.getAbsolutePath());
					Platform.runLater(() -> ((ExportExcelDialogCtrler) exportExcelDialog.getPresenter()).exportOk());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}).start();
			IntelDevPcApplication.showView(ExportExcelDialog.class, Modality.WINDOW_MODAL);

		}
	}
}
