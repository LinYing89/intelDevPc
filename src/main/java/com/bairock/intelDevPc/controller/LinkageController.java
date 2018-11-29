package com.bairock.intelDevPc.controller;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bairock.intelDevPc.IntelDevPcApplication;
import com.bairock.intelDevPc.comm.listener.OnLinkageEnableChangeListener;
import com.bairock.intelDevPc.repository.LinkageConditionRepository;
import com.bairock.intelDevPc.repository.LinkageEffectRepository;
import com.bairock.intelDevPc.service.LinkageService;
import com.bairock.intelDevPc.service.UserService;
import com.bairock.intelDevPc.view.EditLinkageConditionView;
import com.bairock.intelDevPc.view.EditLinkageEffectView;
import com.bairock.intelDevPc.view.RenameView;
import com.bairock.iot.intelDev.linkage.ChainHolder;
import com.bairock.iot.intelDev.linkage.Effect;
import com.bairock.iot.intelDev.linkage.Linkage;
import com.bairock.iot.intelDev.linkage.LinkageCondition;
import com.bairock.iot.intelDev.linkage.LinkageHolder;
import com.bairock.iot.intelDev.linkage.SubChain;
import com.bairock.iot.intelDev.linkage.timing.Timing;
import com.bairock.iot.intelDev.linkage.timing.TimingHolder;
import com.bairock.iot.intelDev.linkage.timing.ZTimer;

import de.felixroske.jfxsupport.FXMLController;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
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
	@Autowired
	private LinkageConditionRepository linkageConditionRepository;
	@Autowired
	private LinkageEffectRepository linkageEffectRepository;
	@Autowired
	private EditLinkageConditionView editLinkageConditionView;
	@Autowired
	private EditLinkageEffectView editLinkageEffectView;

	@FXML
	private HBox hboxAddChain;

	// --------------SubChain----------------
	@FXML
	private ListView<Linkage> listViewLinkage;
	@FXML
	private ListView<LinkageCondition> listViewChainCondition;
	@FXML
	private ListView<Effect> listViewChainEffect;
	@FXML
	private TextField txtLinkageName;
	private ChainHolder chainHolder;
	// 选中的连锁
	private SubChain selectedChain;
	// 选中的连锁条件
	private LinkageCondition selectedChainCondition;
	// 选中的连锁影响
	private Effect selectedChainEffect;

	private ChangeListener<Boolean> linkageChangedListener = new OnLinkageEnableChangeListener();
	// ----------------SubChain---------------------------------

	// ------------------Timing start----------------------------------
	private TimingHolder timingHolder;
	// 选中的定时
	private Timing selectedTiming;
	// 选中的定时时间
	private ZTimer selectedTimingZTimer;
	// 选中的定时影响
	private Effect selectedTimingEffect;

	@FXML
	private ListView<Linkage> listViewTiming;
	@FXML
	private ListView<ZTimer> listViewTimingZTimer;
	@FXML
	private ListView<Effect> listViewTimingEffect;

	// ------------------Timing end------------------------------

	private boolean inited = false;

	private void init1() {
		if (!inited) {
			inited = true;
			// -----------------------连锁
			// 连锁名称监听
			listViewLinkage.getSelectionModel().selectedItemProperty().addListener((p1, p2, linkage) -> {
				if (null == linkage) {
					return;
				}
				selectedChain = (SubChain) linkage;
				refreshSubChainCondition();
				refreshSubChainEffect();
			});
			// 连锁条件监听
			listViewChainCondition.getSelectionModel().selectedItemProperty().addListener((p1, p2, p3) -> {
				if (null == p3) {
					return;
				}
				selectedChainCondition = p3;
			});
			// 连锁影响监听
			listViewChainEffect.getSelectionModel().selectedItemProperty().addListener((p1, p2, p3) -> {
				if (null == p3) {
					return;
				}
				selectedChainEffect = p3;
			});
			// -------------连锁--------------

			// --------定时--------------------
			// 定时名称监听
			listViewTiming.getSelectionModel().selectedItemProperty().addListener((p1, p2, linkage) -> {
				if (null == linkage) {
					return;
				}
				selectedTiming = (Timing) linkage;
//				refreshSubChainCondition();
//				refreshSubChainEffect();
			});
			// 定时时间监听
			listViewTimingZTimer.getSelectionModel().selectedItemProperty().addListener((p1, p2, p3) -> {
				if (null == p3) {
					return;
				}
				selectedTimingZTimer = p3;
			});
			// 定时影响监听
			listViewTimingEffect.getSelectionModel().selectedItemProperty().addListener((p1, p2, p3) -> {
				if (null == p3) {
					return;
				}
				selectedTimingEffect = p3;
			});
			// --------------定时-------------

		}
	}

	public void init() {
		init1();
		hboxAddChain.setVisible(false);
		hboxAddChain.setManaged(false);

		chainHolder = UserService.user.getListDevGroup().get(0).getChainHolder();
		timingHolder = UserService.user.getListDevGroup().get(0).getTimingHolder();
		refreshListViewSubChain();
		refreshListViewTiming();
	}

	// ==================================连锁开始================================

	// 刷新连锁名称列表
	private void refreshListViewSubChain() {
		refreshListViewLinkageName(listViewLinkage, chainHolder);
	}

	// 刷新连锁条件列表
	private void refreshSubChainCondition() {
		listViewChainCondition.getItems().clear();
		if (null == selectedChain) {
			return;
		}
		List<LinkageCondition> listCondition = selectedChain.getListCondition();
		listViewChainCondition.setCellFactory(new Callback<ListView<LinkageCondition>, ListCell<LinkageCondition>>() {
			@Override
			public ListCell<LinkageCondition> call(ListView<LinkageCondition> param) {
				return new ListCell<LinkageCondition>() {
					@Override
					protected void updateItem(LinkageCondition item, boolean empty) {
						super.updateItem(item, empty);
						if (null != item && !empty) {
							String name = String.format("%-4s %-5s %-2s %s", item.getLogic().toString(),
									item.getDevice().getName(), item.compareSymbolStr(),
									String.valueOf(item.getCompareValue()));
							this.setText(name);
							setGraphic(null);
						}
					}
				};
			}
		});
		listViewChainCondition.getItems().addAll(listCondition);
	}

	// 刷新连锁受控列表
	private void refreshSubChainEffect() {
		listViewChainEffect.getItems().clear();
		if (null == selectedChain) {
			return;
		}
		List<Effect> listEffect = selectedChain.getListEffect();
		listViewChainEffect.setCellFactory(new Callback<ListView<Effect>, ListCell<Effect>>() {
			@Override
			public ListCell<Effect> call(ListView<Effect> param) {
				return new ListCell<Effect>() {
					@Override
					protected void updateItem(Effect item, boolean empty) {
						super.updateItem(item, empty);
						if (null != item && !empty) {
							String name = String.format("%-4s %-4s", item.getDevice().getName(), item.effectStateStr());
							this.setText(name);
							setGraphic(null);
						}
					}
				};
			}
		});
		listViewChainEffect.getItems().addAll(listEffect);
	}

	// 连锁名称使能事件
	public void linkageEnablechanged(ObservableValue<? extends Boolean> prop, Boolean oldValue, Boolean newValue) {
		SimpleBooleanProperty boolProp = (SimpleBooleanProperty) prop;
		Linkage linkage = (Linkage) boolProp.getBean();
		logger.info("linkage enable changed " + linkage.getName() + linkage.isEnable());
	}

	// 添加连锁按钮
	public void btnAddSubChain() {
		hboxAddChain.setVisible(true);
		hboxAddChain.setManaged(true);
	}

	// 添加连锁确定按钮
	public void handlerAddChainOk() {
		hboxAddChain.setVisible(false);
		hboxAddChain.setManaged(false);
		String name = txtLinkageName.getText();
		if (!name.isEmpty()) {
			Linkage linkage = new SubChain();
			linkage.setName(name);
			chainHolder.addLinkage(linkage);
			linkageService.addLinkage(linkage);
			refreshListViewSubChain();
		}
	}

	// 添加连锁条件
	@FXML
	public void btnAddChainCondition() {
		if (null == selectedChain) {
			return;
		}
		EditLinkageConditionController controller = (EditLinkageConditionController) editLinkageConditionView
				.getPresenter();
		controller.init(null);
		IntelDevPcApplication.showView(EditLinkageConditionView.class, Modality.WINDOW_MODAL);
		if (controller.result) {
			selectedChain.addCondition(controller.condition);
			linkageConditionRepository.saveAndFlush(controller.condition);
			refreshSubChainCondition();
		}
	}

	// 添加连锁影响按钮
	@FXML
	public void btnAddChainEffect() {
		if (null == selectedChain) {
			return;
		}
		EditLinkageEffectController controller = (EditLinkageEffectController) editLinkageEffectView.getPresenter();
		controller.init(null);
		IntelDevPcApplication.showView(EditLinkageEffectView.class, Modality.WINDOW_MODAL);
		if (controller.result) {
			selectedChain.addEffect(controller.effect);
			linkageEffectRepository.saveAndFlush(controller.effect);
			refreshSubChainEffect();
		}
	}

	// 编辑连锁
	@FXML
	public void menuEditSubChain() {
		Linkage linkage = listViewLinkage.getSelectionModel().getSelectedItem();
		if (null != linkage) {
			String name = linkage.getName();
			logger.info("edit " + name);
			RenameController renameController = (RenameController) renameView.getPresenter();
			renameController.init(name);
			IntelDevPcApplication.showView(RenameView.class, Modality.WINDOW_MODAL);
			if (renameController.result) {
				linkage.setName(renameController.getNewName());
				linkageService.updateLinkage(linkage);
				listViewLinkage.refresh();
			}
		}
	}

	// 删除连锁
	@FXML
	public void menuDelSubChain() {
		Linkage linkage = listViewLinkage.getSelectionModel().getSelectedItem();
		if (null != linkage) {
			if (null != linkage) {
				chainHolder.removeLinkage(linkage);
				linkageService.deleteLinkage(linkage);
				refreshListViewSubChain();
			}
		}
	}

	// 编辑连锁条件
	@FXML
	public void menuEditChainCondition() {
		if (null == selectedChainCondition) {
			return;
		}
		EditLinkageConditionController controller = (EditLinkageConditionController) editLinkageConditionView
				.getPresenter();
		controller.init(selectedChainCondition);
		IntelDevPcApplication.showView(EditLinkageConditionView.class, Modality.WINDOW_MODAL);
		if (controller.result) {
			linkageConditionRepository.saveAndFlush(controller.condition);
			listViewChainCondition.refresh();
		}
	}

	// 删除连锁条件
	@FXML
	public void menuDelChainCondition() {
		if (null == selectedChainCondition) {
			return;
		}
		selectedChain.removeCondition(selectedChainCondition);
		linkageConditionRepository.deleteById(selectedChainCondition.getId());
		refreshSubChainCondition();
	}

	// 编辑连锁影响
	@FXML
	public void menuEditChainEffect() {
		if (null == selectedChainEffect) {
			return;
		}
		EditLinkageEffectController controller = (EditLinkageEffectController) editLinkageEffectView.getPresenter();
		controller.init(selectedChainEffect);
		IntelDevPcApplication.showView(EditLinkageEffectView.class, Modality.WINDOW_MODAL);
		if (controller.result) {
			linkageEffectRepository.saveAndFlush(controller.effect);
			listViewChainEffect.refresh();
		}
	}

	// 删除连锁影响
	@FXML
	public void menuDelChainEffect() {
		if (null == selectedChainEffect) {
			return;
		}
		selectedChain.removeEffect(selectedChainEffect);
		linkageEffectRepository.deleteById(selectedChainEffect.getId());
		refreshSubChainEffect();
	}
	// ------------------------------------连锁结束----------------------------------------

	// =====================================定时开始========================================

	private void refreshListViewTiming() {
		refreshListViewLinkageName(listViewTiming, timingHolder);
	}

	// 添加定时
	@FXML
	public void btnAddTiming() {
		RenameController renameController = (RenameController) renameView.getPresenter();
		renameController.init(null);
		IntelDevPcApplication.showView(RenameView.class, Modality.WINDOW_MODAL);
		if (renameController.result) {
			Timing timing = new Timing();
			timing.setName(renameController.getNewName());
			timing.setId(UUID.randomUUID().toString());
			timingHolder.addLinkage(timing);
			linkageService.addLinkage(timing);
			refreshListViewTiming();
		}
	}

	// 编辑定时
	@FXML
	public void menuEditTiming() {
		if (null == selectedTiming) {
			return;
		}
		RenameController renameController = (RenameController) renameView.getPresenter();
		renameController.init(selectedTiming.getName());
		IntelDevPcApplication.showView(RenameView.class, Modality.WINDOW_MODAL);
		if (renameController.result) {
			selectedTiming.setName(renameController.getNewName());
			linkageService.updateLinkage(selectedTiming);
			listViewTiming.refresh();
		}
	}

	// 删除定时
	@FXML
	public void menuDelTiming() {
		Linkage linkage = listViewTiming.getSelectionModel().getSelectedItem();
		if (null != linkage) {
			if (null != linkage) {
				timingHolder.removeLinkage(linkage);
				linkageService.deleteLinkage(linkage);
				refreshListViewTiming();
			}
		}
	}

	// -------------------------定时结束------------------------------

	/**
	 * 更新连锁名称列表
	 * 
	 * @param listView 列表对象
	 * @param holder   连锁持有者
	 */
	private void refreshListViewLinkageName(ListView<Linkage> listView, LinkageHolder holder) {
		listView.getItems().clear();
		List<Linkage> listChain = holder.getListLinkage();

		for (Linkage linkage : listChain) {
			linkage.enableProperty().removeListener(linkageChangedListener);
			linkage.enableProperty().addListener(linkageChangedListener);
		}

		Callback<Linkage, ObservableValue<Boolean>> itemToBoolean = (Linkage item) -> item.enableProperty();
		listView.setCellFactory(CheckBoxListCell.forListView(itemToBoolean, linkageNameConvert));
		listView.getItems().addAll(listChain);

		if (!listView.getItems().isEmpty()) {
			listView.scrollTo(0);
		}
	}

	// 连锁名称对象转字符串名称
	private StringConverter<Linkage> linkageNameConvert = new StringConverter<Linkage>() {

		@Override
		public String toString(Linkage object) {
			return object.getName();
		}

		@Override
		public Linkage fromString(String string) {
			// TODO Auto-generated method stub
			return null;
		}

	};
}
