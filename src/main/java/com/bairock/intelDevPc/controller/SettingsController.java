package com.bairock.intelDevPc.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.bairock.intelDevPc.data.Config;
import com.bairock.intelDevPc.repository.ConfigRepository;

import de.felixroske.jfxsupport.FXMLController;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

@FXMLController
public class SettingsController {

	@Autowired
	private Config config;
	@Autowired
	private ConfigRepository configRepository;
	
	@FXML
	private TreeView<String> treeView;
	@FXML
	private TextField txtServiceName;
	@FXML
	private TextField txtAppName;
	
	private boolean inited = false;
	
	private void init1() {
		if(inited) {
			return;
		}
		treeView.setRoot(null);
		TreeItem<String> root = new TreeItem<>("设备");
		treeView.setRoot(root);
		
		TreeItem<String> item1 = new TreeItem<>("通用");
		root.getChildren().add(item1);
		
		treeView.getSelectionModel().selectedItemProperty().addListener((p1, p2, p3) -> {
			
		});
	}
	
	public void init() {
		init1();
		txtServiceName.setText(config.getServerName());
		txtAppName.setText(config.getAppTitle());
	}
	
	@FXML
	public void btnOkAction() {
		String serverName = txtServiceName.getText();
		String appName = txtAppName.getText();
		config.setServerName(serverName);
		config.setAppTitle(appName);
		configRepository.saveAndFlush(config);
	}
}
