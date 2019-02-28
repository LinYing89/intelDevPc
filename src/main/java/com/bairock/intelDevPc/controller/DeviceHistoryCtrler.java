package com.bairock.intelDevPc.controller;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.bairock.intelDevPc.data.DeviceValueHistory;
import com.bairock.intelDevPc.service.DeviceHistoryService;
import com.bairock.intelDevPc.service.UserService;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.devcollect.DevCollect;
import com.bairock.iot.intelDev.user.DevGroup;

import de.felixroske.jfxsupport.FXMLController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

@FXMLController
public class DeviceHistoryCtrler {
	@Autowired
	private DeviceHistoryService deviceHistoryService;

	@FXML
	private ChoiceBox<Device> choiceBoxDevice;
	@FXML
	private DatePicker datePickerFrom;
	@FXML
	private DatePicker datePickerTo;
	@FXML
	private LineChart<String, Float> chartHistory;
	private XYChart.Series<String, Float> seriesValueChart;
	@FXML
	private TableView<DeviceValueHistory> tableHistory;
	@FXML
	private TableColumn<DeviceValueHistory, Date> tableColumnTime;
	@FXML
	private TableColumn<DeviceValueHistory, String> tableColumnDeviceName;
	@FXML
	private TableColumn<DeviceValueHistory, Float> tableColumnValue;

	private List<Device> listDevice = new ArrayList<>();

	private boolean inited;

	private void init1() {
		tableColumnTime.setCellValueFactory(new PropertyValueFactory<>("historyTime"));
		tableColumnDeviceName.setCellValueFactory(new PropertyValueFactory<>("deviceName"));
		tableColumnValue.setCellValueFactory(new PropertyValueFactory<>("value"));
		
		seriesValueChart = new XYChart.Series<>();
		chartHistory.getData().add(seriesValueChart);
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
	}

	public static ObservableList<DeviceValueHistory> getDeviceValueHistoryList(List<DeviceValueHistory> list) {
		return FXCollections.<DeviceValueHistory>observableArrayList(list);
	}

	// 查询按钮
	@FXML
	private void onBtnSearchAction() {
		seriesValueChart.getData().clear();

		Device device = choiceBoxDevice.getSelectionModel().getSelectedItem();
		LocalDate ldFrom = datePickerFrom.getValue();
		LocalDate ldTo = datePickerTo.getValue();
		if(device == null || ldFrom == null || ldTo == null) {
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

		List<DeviceValueHistory> listHistory = deviceHistoryService.find(device.getLongCoding(), dateFrom, dateTo);

		for (DeviceValueHistory dv : listHistory) {
			seriesValueChart.getData().add(new XYChart.Data<String, Float>(dv.strTimeFormat(), dv.getValue()));
		}
		tableHistory.setItems(getDeviceValueHistoryList(listHistory));
		
	}

	// 导出按钮
	@FXML
	private void onBtnExportAction() {

	}

}
