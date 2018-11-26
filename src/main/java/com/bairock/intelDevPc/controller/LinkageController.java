package com.bairock.intelDevPc.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bairock.intelDevPc.IntelDevPcApplication;
import com.bairock.intelDevPc.comm.listener.OnLinkageEnableChangeListener;
import com.bairock.intelDevPc.service.LinkageService;
import com.bairock.intelDevPc.service.UserService;
import com.bairock.intelDevPc.view.RenameView;
import com.bairock.iot.intelDev.linkage.ChainHolder;
import com.bairock.iot.intelDev.linkage.Linkage;

import de.felixroske.jfxsupport.FXMLController;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.util.Callback;
import javafx.util.StringConverter;

@FXMLController
public class LinkageController {

	private static Logger logger = LoggerFactory.getLogger(LinkageController.class);

	@Autowired
	private RenameView renameView;
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
//		listViewLinkage.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> handleChainNameMouseSelected(e));
		ContextMenu cm = new ContextMenu();
		MenuItem mEdit = new MenuItem("编辑");
		mEdit.setOnAction(value -> {
			Linkage linkage = listViewLinkage.getSelectionModel().getSelectedItem();
			if (null != linkage) {
				String name = linkage.getName();
				logger.info("edit " + name);
				RenameController renameController = (RenameController)renameView.getPresenter();
				renameController.init(name);
				IntelDevPcApplication.showView(RenameView.class, Modality.WINDOW_MODAL);
				if(renameController.result) {
					linkage.setName(renameController.getNewName());
					linkageService.updateLinkage(linkage);
					listViewLinkage.refresh();
				}
			}
		});
		cm.getItems().add(mEdit);
		MenuItem mDel = new MenuItem("删除");
		mDel.setOnAction(value -> {
			if (null != listViewLinkage.getSelectionModel().getSelectedItem()) {
				String name = listViewLinkage.getSelectionModel().getSelectedItem().getName();
				logger.info("del " + name);
			}
		});
		cm.getItems().add(mDel);
		listViewLinkage.setContextMenu(cm);
	}

	public void linkageEnablechanged(ObservableValue<? extends Boolean> prop, Boolean oldValue, Boolean newValue) {
		SimpleBooleanProperty boolProp = (SimpleBooleanProperty) prop;
		Linkage linkage = (Linkage) boolProp.getBean();
		logger.info("linkage enable changed " + linkage.getName() + linkage.isEnable());
	}

//	public void handleChainNameMouseSelected(MouseEvent e) {
//		if(e.getButton() == MouseButton.SECONDARY) {
////			ContextMenu cm = new ContextMenu();
////			MenuItem mEdit = new MenuItem("编辑");
////			cm.getItems().add(mEdit);
////			MenuItem mDel = new MenuItem("删除");
////			cm.getItems().add(mDel);
////			cm.show(listViewLinkage, e.getScreenX(), e.getScreenY());
//			if(nu)
//			String name = listViewLinkage.getSelectionModel().getSelectedItem().getName();
//			logger.info("handleChainNameMouseSelected " + name);
//		}
//	}

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
