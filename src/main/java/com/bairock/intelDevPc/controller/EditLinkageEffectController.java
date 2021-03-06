package com.bairock.intelDevPc.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import com.bairock.intelDevPc.service.UserService;
import com.bairock.intelDevPc.view.EditLinkageEffectView;
import com.bairock.iot.intelDev.device.DevStateHelper;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.IStateDev;
import com.bairock.iot.intelDev.linkage.Effect;

import de.felixroske.jfxsupport.FXMLController;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

@FXMLController
public class EditLinkageEffectController {

	@Autowired
	private EditLinkageEffectView editLinkageEffectView;

	@FXML
	private ChoiceBox<Device> choiceBoxDevice;
	@FXML
	private ChoiceBox<String> choiceBoxState;
	@FXML
    private TextField txtEffectValue;
	
	public Effect effect;
	
	/**
	 * 是否为编辑, 不是编辑就是添加
	 */
	public boolean edit;
	public boolean result = false;

	private boolean inited;
	
	private void init1() {
		choiceBoxState.getItems().addAll("开", "关");
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
                txtEffectValue.setDisable(true);
                choiceBoxState.setDisable(false);
            } else {
                txtEffectValue.setDisable(false);
                choiceBoxState.setDisable(true);
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
		listDevices.addAll(UserService.getDevGroup().findListIStateDev(true));
		listDevices.addAll(UserService.getDevGroup().findListDevParam(true));
		choiceBoxDevice.getItems().clear();
		choiceBoxDevice.getItems().addAll(listDevices);

		// 如果effect为空则为添加, 不为空则为编辑
		if (null == effect) {
			edit = false;
			this.effect = new Effect();
			this.effect.setId(UUID.randomUUID().toString());

		} else {
			edit = true;
			this.effect = effect;

			Device device = effect.getDevice();
			
			if(device instanceof IStateDev) {
			    txtEffectValue.setDisable(true);
    			if(effect.getDsId().equals(DevStateHelper.DS_KAI)) {
    				choiceBoxState.getSelectionModel().select(0);
    			}else {
    				choiceBoxState.getSelectionModel().select(1);
    			}
			}else {
			    txtEffectValue.setText(effect.getEffectContent());
			    choiceBoxState.setDisable(true);
			}
			choiceBoxDevice.getSelectionModel().select(device);
		}
	}
	
	// 确定按钮
		@FXML
		public void btnOkAction() {
			result = true;
			Device device = choiceBoxDevice.getSelectionModel().getSelectedItem();
			effect.setDevice(device);
			if(choiceBoxState.getSelectionModel().getSelectedIndex() == 0) {
				effect.setDsId(DevStateHelper.DS_KAI);
			}else {
				effect.setDsId(DevStateHelper.DS_GUAN);
			}
			effect.setEffectContent(txtEffectValue.getText());
			editLinkageEffectView.getView().getScene().getWindow().hide();
		}

		// 取消按钮
		@FXML
		public void btnCancelAction() {
			result = false;
			editLinkageEffectView.getView().getScene().getWindow().hide();
		}
}
