package com.bairock.intelDevPc.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.bairock.intelDevPc.view.RenameView;

import de.felixroske.jfxsupport.FXMLController;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

@FXMLController
public class RenameController {

	@Autowired
	private RenameView renameView;

	@FXML
	private TextField txtNewName;

	private String oldName;
	private String newName;
	/**
	 * true is OK, false is cancel
	 */
	public boolean result;

	public String getOldName() {
		return oldName;
	}

	public String getNewName() {
		return newName;
	}

	public boolean edit = false;

	public void init(String oldName) {
		result = false;
		if (null == oldName || oldName.isEmpty()) {
			edit = true;
		} else {
			edit = false;
			this.oldName = oldName;
			txtNewName.setText(oldName);
		}
	}

	@FXML
	public void handlerOk() {
		newName = txtNewName.getText();
		if (!newName.equals(oldName)) {
			result = true;
		} else {
			result = false;
		}
		renameView.getView().getScene().getWindow().hide();
	}

	@FXML
	public void handlerCancel() {
		result = false;
		renameView.getView().getScene().getWindow().hide();
	}
}
