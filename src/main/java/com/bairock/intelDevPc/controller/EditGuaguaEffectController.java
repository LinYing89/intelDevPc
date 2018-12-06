package com.bairock.intelDevPc.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import com.bairock.intelDevPc.service.UserService;
import com.bairock.intelDevPc.view.EditGuaguaEffectView;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.linkage.Effect;

import de.felixroske.jfxsupport.FXMLController;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

@FXMLController
public class EditGuaguaEffectController {

	@Autowired
	private EditGuaguaEffectView editGuaguaEffectView;

	@FXML
	private ChoiceBox<Device> choiceBoxDevice;
	@FXML
	private TextField txtEffectCount;
	@FXML
	private TextArea txaEffectContent;
	
	public Effect effect;
	
	/**
	 * 是否为编辑, 不是编辑就是添加
	 */
	public boolean edit;
	public boolean result = false;

	private boolean inited;
	
	private void init1() {
		txtEffectCount.setText("1");
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
	
	public void init(Effect effect) {
		result = false;
		if (!inited) {
			inited = true;
			init1();
		}
		List<Device> listDevices = new ArrayList<>();
//		listDevices.addAll(UserService.getDevGroup().findListGuaguaMouth(true));
		listDevices.addAll(UserService.getDevGroup().findListIStateDev(true));
		choiceBoxDevice.getItems().clear();
		choiceBoxDevice.getItems().addAll(listDevices);

		// 如果effect为空则为添加, 不为控股则为编辑
		if (null == effect) {
			edit = false;
			this.effect = new Effect();
			this.effect.setId(UUID.randomUUID().toString());

		} else {
			edit = true;
			this.effect = effect;
			txtEffectCount.setText(String.valueOf(effect.getEffectCount()));
			txaEffectContent.setText(effect.getEffectContent());

			Device device = effect.getDevice();
			choiceBoxDevice.getSelectionModel().select(device);
		}
	}
	
	// 确定按钮
		@FXML
		public void btnOkAction() {
			result = true;
			Device device = choiceBoxDevice.getSelectionModel().getSelectedItem();
			if(null == device) {
				return;
			}
			effect.setDevice(device);
			effect.setEffectCount(Integer.parseInt(txtEffectCount.getText()));
			effect.setEffectContent(txaEffectContent.getText());
			editGuaguaEffectView.getView().getScene().getWindow().hide();
		}

		// 取消按钮
		@FXML
		public void btnCancelAction() {
			result = false;
			editGuaguaEffectView.getView().getScene().getWindow().hide();
		}
}
