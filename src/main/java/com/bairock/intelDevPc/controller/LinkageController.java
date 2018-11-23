package com.bairock.intelDevPc.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bairock.intelDevPc.comm.listener.OnLinkageEnableChangeListener;
import com.bairock.intelDevPc.service.LinkageService;
import com.bairock.intelDevPc.service.UserService;
import com.bairock.iot.intelDev.linkage.ChainHolder;
import com.bairock.iot.intelDev.linkage.Linkage;

import de.felixroske.jfxsupport.FXMLController;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import javafx.util.StringConverter;

@FXMLController
public class LinkageController {

	private static Logger logger = LoggerFactory.getLogger(LinkageController.class);

	@Autowired
	private LinkageService linkageService;
	@FXML
	private HBox hboxAddChain;

	@FXML
	private ListView<Linkage> listViewLinkage;
	@FXML
	private TextField txtLinkageName;

	private ChainHolder chainHolder;

	private ChangeListener<Boolean> linkageChangedListener = new OnLinkageEnableChangeListener();

	public void init() {
		hboxAddChain.setVisible(false);
		hboxAddChain.setManaged(false);

		chainHolder = UserService.user.getListDevGroup().get(0).getChainHolder();
		refreshChain();
	}

	private void refreshChain() {

		listViewLinkage.getItems().clear();
		List<Linkage> listChain = chainHolder.getListLinkage();

		for (Linkage linkage : listChain) {
			linkage.enableProperty().removeListener(linkageChangedListener);
			linkage.enableProperty().addListener(linkageChangedListener);
		}

		Callback<Linkage, ObservableValue<Boolean>> itemToBoolean = (Linkage item) -> item.enableProperty();
		listViewLinkage.setCellFactory(CheckBoxListCell.forListView(itemToBoolean, new StringConverter<Linkage>() {

			@Override
			public String toString(Linkage object) {
				return object.getName();
			}

			@Override
			public Linkage fromString(String string) {
				return null;
			}
		}));
		listViewLinkage.getItems().addAll(listChain);
	}

	public void linkageEnablechanged(ObservableValue<? extends Boolean> prop, Boolean oldValue,
			Boolean newValue) {
		SimpleBooleanProperty boolProp = (SimpleBooleanProperty) prop;
		Linkage linkage = (Linkage) boolProp.getBean();
		logger.info("linkage enable changed " + linkage.getName() + linkage.isEnable());
	}

	// 添加连锁按钮
	public void handlerAddLinkage() {
		hboxAddChain.setVisible(true);
		hboxAddChain.setManaged(true);
	}

	public void handlerAddChainOk() {
		hboxAddChain.setVisible(false);
		hboxAddChain.setManaged(false);
		String name = txtLinkageName.getText();
		if (!name.isEmpty()) {
			Linkage linkage = new Linkage();
			linkage.setName(name);
			chainHolder.addLinkage(linkage);
			linkageService.addLinkage(linkage);
			refreshChain();
		}
	}
}
