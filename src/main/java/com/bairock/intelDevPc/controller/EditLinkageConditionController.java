package com.bairock.intelDevPc.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import com.bairock.intelDevPc.service.UserService;
import com.bairock.intelDevPc.view.EditLinkageConditionView;
import com.bairock.iot.intelDev.device.CompareSymbol;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.IStateDev;
import com.bairock.iot.intelDev.linkage.LinkageCondition;
import com.bairock.iot.intelDev.linkage.ZLogic;

import de.felixroske.jfxsupport.FXMLController;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

@FXMLController
public class EditLinkageConditionController {

	@Autowired
	private EditLinkageConditionView editLinkageConditionView;

	@FXML
	private ChoiceBox<String> choiceBoxLogic;
	@FXML
	private ChoiceBox<String> choiceBoxCompareSymbol;
	@FXML
	private ChoiceBox<Device> choiceBoxDevice;
	@FXML
	private ChoiceBox<String> choiceBoxCompareValue;
	@FXML
	private TextField txtCompareValue;

	public LinkageCondition condition;
	/**
	 * 是否为编辑, 不是编辑就是添加
	 */
	public boolean edit;
	public boolean result = false;

	private boolean inited;

	private void init1() {
		choiceBoxLogic.getItems().addAll("AND", "OR");
		choiceBoxCompareSymbol.getItems().addAll("< (小于)", "= (等于)", "> (大于)");
		choiceBoxCompareValue.getItems().addAll("关", "开");
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
		choiceBoxDevice.getSelectionModel().selectedItemProperty().addListener((p0, p1, p2) -> {
			if (p2 instanceof IStateDev) {
				enableStateCompareValue();
			} else {
				enableValueCompareValue();
			}
		});
	}

	public void init(LinkageCondition condition) {
		result = false;
		if (!inited) {
			inited = true;
			init1();
		}
		List<Device> listDevices = new ArrayList<>();
		listDevices.addAll(UserService.getDevGroup().findListCollectDev(true));
		listDevices.addAll(UserService.getDevGroup().findListIStateDev(true));
		choiceBoxDevice.getItems().clear();
		choiceBoxDevice.getItems().addAll(listDevices);

		// 如果condition为空则为添加, 不为控股则为编辑
		if (null == condition) {
			edit = false;
			this.condition = new LinkageCondition();
			this.condition.setId(UUID.randomUUID().toString());

		} else {
			edit = true;
			this.condition = condition;

			choiceBoxLogic.getSelectionModel().select(condition.getLogic().toString());
			choiceBoxCompareSymbol.getSelectionModel().select(condition.getCompareSymbol().ordinal());

			Device device = condition.getDevice();
			if (device instanceof IStateDev) {
				enableStateCompareValue();
				if (condition.getCompareValue() == 0) {
					choiceBoxCompareValue.getSelectionModel().select(0);
				} else {
					choiceBoxCompareValue.getSelectionModel().select(1);
				}
			} else {
				enableValueCompareValue();
				txtCompareValue.setText(String.valueOf(condition.getCompareValue()));
			}

			choiceBoxDevice.getSelectionModel().select(device);
		}

	}

	private void enableStateCompareValue() {
		choiceBoxCompareSymbol.getSelectionModel().select(1);
		choiceBoxCompareSymbol.setDisable(true);
		choiceBoxCompareValue.setDisable(false);
		txtCompareValue.setDisable(true);
	}

	private void enableValueCompareValue() {
		choiceBoxCompareSymbol.setDisable(false);
		choiceBoxCompareValue.setDisable(true);
		txtCompareValue.setDisable(false);
	}

	// 确定按钮
	@FXML
	public void btnOkAction() {
		result = true;
		Device device = choiceBoxDevice.getSelectionModel().getSelectedItem();
		condition.setDevice(device);
		condition.setLogic(ZLogic.values()[choiceBoxLogic.getSelectionModel().getSelectedIndex()]);
		condition.setCompareSymbol(
				CompareSymbol.values()[choiceBoxCompareSymbol.getSelectionModel().getSelectedIndex()]);
		if (device instanceof IStateDev) {
			condition.setCompareValue(choiceBoxCompareValue.getSelectionModel().getSelectedIndex());
		} else {
			condition.setCompareValue(Float.parseFloat(txtCompareValue.getText()));
		}
		editLinkageConditionView.getView().getScene().getWindow().hide();
	}

	// 取消按钮
	@FXML
	public void btnCancelAction() {
		result = false;
		editLinkageConditionView.getView().getScene().getWindow().hide();
	}
}
