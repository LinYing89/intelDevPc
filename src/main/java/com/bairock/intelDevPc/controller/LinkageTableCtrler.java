package com.bairock.intelDevPc.controller;

import java.util.List;

import com.bairock.intelDevPc.data.MyColor;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.linkage.LinkageTab;
import com.bairock.iot.intelDev.linkage.LinkageTab.OnCheckTableListener;
import com.bairock.iot.intelDev.linkage.LinkageTabRow;

import de.felixroske.jfxsupport.FXMLController;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

@FXMLController
public class LinkageTableCtrler {

	@FXML
	private Label labelRefresh;
	@FXML
	private TableView<LinkageTabRow> tableLinkage;
	@FXML
	private TableColumn<LinkageTabRow, String> deviceName;
	@FXML
	private TableColumn<LinkageTabRow, String> deviceCode;
	@FXML
	private TableColumn<LinkageTabRow, String> state;
	@FXML
	private TableColumn<LinkageTabRow, Integer> chain;
	@FXML
	private TableColumn<LinkageTabRow, Integer> timing;
	@FXML
	private TableColumn<LinkageTabRow, Integer> loop;
	@FXML
	private TableColumn<LinkageTabRow, Integer> result;
	@FXML
	private TableColumn<LinkageTabRow, String> colGear;

	private boolean inited;

	private void init1() {
		deviceName.setCellValueFactory(cellData -> {
			Device dev = cellData.getValue().getDevice();
			return new ReadOnlyStringWrapper(dev.getName());
		});
		deviceCode.setCellValueFactory(cellData -> {
			Device dev = cellData.getValue().getDevice();
			return new ReadOnlyStringWrapper(dev.getLongCoding());
		});
		state.setCellValueFactory(cellData -> {
			Device dev = cellData.getValue().getDevice();
			String state = "";
			if(!dev.isNormal()) {
				state = "×";
			}else {
				if(dev.isKaiState()) {
					state = "开";
				}else {
					state = "关";
				}
			}
			return new ReadOnlyStringWrapper(state);
		});
		chain.setCellValueFactory(new PropertyValueFactory<>("chainTem"));
		timing.setCellValueFactory(new PropertyValueFactory<>("timingTem"));
		loop.setCellValueFactory(new PropertyValueFactory<>("loop"));
		result.setCellValueFactory(new PropertyValueFactory<>("result"));
		colGear.setCellValueFactory(cellData -> {
			Device dev = cellData.getValue().getDevice();
			String strGear = "";
			switch(dev.getGear()) {
			case KAI : 
				strGear = "O";
				break;
			case GUAN : 
				strGear = "S";
				break;
			default : 
				strGear = "A";
				break;
			}
			return new ReadOnlyStringWrapper(strGear);
		});
	}

	public void init() {
		if (!inited) {
			inited = true;
			init1();
		}
		List<LinkageTabRow> listDevice = LinkageTab.getIns().getListLinkageTabRow();
		listDevice.sort((a, b) -> a.getDevice().getLongCoding().compareTo(b.getDevice().getLongCoding()));
		tableLinkage.setItems(getDeviceValueHistoryList(listDevice));
		LinkageTab.getIns().setOnCheckTableListener(onCheckTableListener);
	}
	
	public void removeOnCheckTableListener() {
		LinkageTab.getIns().setOnCheckTableListener(null);
	}
	
	private OnCheckTableListener onCheckTableListener = new OnCheckTableListener() {
		@Override
		public void onAfterCheck(List<LinkageTabRow> list) {
			Platform.runLater(() -> {
				labelRefresh.setStyle("-fx-background-color : " + MyColor.TRANSPARENT);
				tableLinkage.refresh();
			});
			
		}

		@Override
		public void onBeforCheck(List<LinkageTabRow> list) {
			Platform.runLater(() -> {
				labelRefresh.setStyle("-fx-background-color : " + MyColor.SUCCESS);
			});
		}
	};

	public static ObservableList<LinkageTabRow> getDeviceValueHistoryList(List<LinkageTabRow> list) {
		return FXCollections.<LinkageTabRow>observableArrayList(list);
	}
	
	@FXML
	public void resetLinakgeResult() {
		for(LinkageTabRow row : LinkageTab.getIns().getListLinkageTabRow()) {
			row.init();
		}
	}
}
