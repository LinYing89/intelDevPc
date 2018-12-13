package com.bairock.intelDevPc.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import com.bairock.intelDevPc.IntelDevPcApplication;
import com.bairock.intelDevPc.comm.listener.OnLinkageEnableChangeListener;
import com.bairock.intelDevPc.repository.LinkageConditionRepository;
import com.bairock.intelDevPc.repository.LinkageEffectRepository;
import com.bairock.intelDevPc.repository.LinkageHolderRepository;
import com.bairock.intelDevPc.repository.LoopDurationRepository;
import com.bairock.intelDevPc.repository.ZTimerRepository;
import com.bairock.intelDevPc.service.LinkageService;
import com.bairock.intelDevPc.service.UserService;
import com.bairock.intelDevPc.view.EditGuaguaEffectView;
import com.bairock.intelDevPc.view.EditLinkageConditionView;
import com.bairock.intelDevPc.view.EditLinkageEffectView;
import com.bairock.intelDevPc.view.EditLoopDurationView;
import com.bairock.intelDevPc.view.EditTimingZTimerView;
import com.bairock.intelDevPc.view.RenameView;
import com.bairock.iot.intelDev.linkage.ChainHolder;
import com.bairock.iot.intelDev.linkage.Effect;
import com.bairock.iot.intelDev.linkage.Linkage;
import com.bairock.iot.intelDev.linkage.LinkageCondition;
import com.bairock.iot.intelDev.linkage.LinkageHolder;
import com.bairock.iot.intelDev.linkage.SubChain;
import com.bairock.iot.intelDev.linkage.guagua.GuaguaHolder;
import com.bairock.iot.intelDev.linkage.loop.LoopDuration;
import com.bairock.iot.intelDev.linkage.loop.LoopHolder;
import com.bairock.iot.intelDev.linkage.loop.ZLoop;
import com.bairock.iot.intelDev.linkage.timing.Timing;
import com.bairock.iot.intelDev.linkage.timing.TimingHolder;
import com.bairock.iot.intelDev.linkage.timing.ZTimer;

import de.felixroske.jfxsupport.FXMLController;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.util.Callback;
import javafx.util.StringConverter;

@FXMLController
public class LinkageController {

//	private static Logger logger = LoggerFactory.getLogger(LinkageController.class);

	@Autowired
	private RenameView renameView;
	@Autowired
	private LinkageHolderRepository linkageHolderRepository;
	@Autowired
	private LinkageService linkageService;
	@Autowired
	private LinkageConditionRepository linkageConditionRepository;
	@Autowired
	private LinkageEffectRepository linkageEffectRepository;
	@Autowired
	private ZTimerRepository zTimerRepository;
	@Autowired
	private LoopDurationRepository loopDurationRepository;

	@Autowired
	private EditLinkageConditionView editLinkageConditionView;
	@Autowired
	private EditLinkageEffectView editLinkageEffectView;
	@Autowired
	private EditGuaguaEffectView editGuaguaEffectView;
	@Autowired
	private EditTimingZTimerView editTimingZTimerView;
	@Autowired
	private EditLoopDurationView editLoopDurationView;

	@FXML
	private HBox hboxAddChain;

	// --------------SubChain----------------
	@FXML
	private ToggleButton toogleBtnSubChainEnable;
	@FXML
	private ListView<Linkage> listViewLinkage;
	@FXML
	private ListView<LinkageCondition> listViewChainCondition;
	@FXML
	private ListView<Effect> listViewChainEffect;
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
	private ToggleButton toogleBtnTimingEnable;
	@FXML
	private ListView<Linkage> listViewTiming;
	@FXML
	private ListView<ZTimer> listViewTimingZTimer;
	@FXML
	private ListView<Effect> listViewTimingEffect;
	// ------------------Timing end------------------------------

	// ------------------loop start----------------------------------
	private LoopHolder loopHolder;
	// 选中的循环
	private ZLoop selectedLoop;
	// 选中的循环条件
	private LinkageCondition selectedLoopCondition;
	// 选中的循环影响
	private Effect selectedLoopEffect;
	// 选中的循环区间
	private LoopDuration selectedLoopDuration;

	@FXML
	private ToggleButton toogleBtnLoopEnable;
	@FXML
	private ListView<Linkage> listViewLoop;
	@FXML
	private ListView<LinkageCondition> listViewLoopCondition;
	@FXML
	private ListView<LoopDuration> listViewLoopDuration;
	@FXML
	private ListView<Effect> listViewLoopEffect;

	@FXML
	private TextField txtLoopCount;
	@FXML
	private Button btnSaveLoopCount;
	@FXML
	private CheckBox cbLoopInfinite;
	// ------------------loop end------------------------------

	// --------------Guagua----------------
	@FXML
	private ToggleButton toogleBtnGuaguaEnable;
	@FXML
	private ListView<Linkage> listViewGuagua;
	@FXML
	private ListView<LinkageCondition> listViewGuaguaCondition;
	@FXML
	private ListView<Effect> listViewGuaguaEffect;
	private GuaguaHolder guaguaHolder;
	// 选中的呱呱
	private SubChain selectedGuagua;
	// 选中的连锁条件
	private LinkageCondition selectedGuaguaCondition;
	// 选中的连锁影响
	private Effect selectedGuaguaEffect;
	// ----------------Guagua---------------------------------

	// 受控设备列表适配器
	private Callback<ListView<Effect>, ListCell<Effect>> effectCellFactory = new Callback<ListView<Effect>, ListCell<Effect>>() {
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
					} else {
						this.setText(null);
						setGraphic(null);
					}
				}
			};
		}
	};
	// 呱呱受控设备列表适配器
	private Callback<ListView<Effect>, ListCell<Effect>> guaguaEffectCellFactory = new Callback<ListView<Effect>, ListCell<Effect>>() {
		@Override
		public ListCell<Effect> call(ListView<Effect> param) {
			return new ListCell<Effect>() {
				@Override
				protected void updateItem(Effect item, boolean empty) {
					super.updateItem(item, empty);
					if (null != item && !empty) {
						String name = String.format("设备 :%-4s    \t次数 :%-4s\n内容: %s", item.getDevice().getName(),
								item.getEffectCount(), item.getEffectContent());
						this.setText(name);
						setGraphic(null);
					} else {
						this.setText(null);
						setGraphic(null);
					}
				}
			};
		}
	};

	// 连锁条件列表适配器
	private Callback<ListView<LinkageCondition>, ListCell<LinkageCondition>> linkageConditionCellFactory = new Callback<ListView<LinkageCondition>, ListCell<LinkageCondition>>() {
		@Override
		public ListCell<LinkageCondition> call(ListView<LinkageCondition> param) {
			return new ListCell<LinkageCondition>() {
				@Override
				protected void updateItem(LinkageCondition item, boolean empty) {
					super.updateItem(item, empty);
					if (null != item && !empty) {
						String compareValueStr = item.getCompareValue() == 0 ? "关" : "开";
						String name = String.format("%-4s %-5s %-2s %s", item.getLogic().toString(),
								item.getDevice().getName(), item.compareSymbolStr(), compareValueStr);
						this.setText(name);
						setGraphic(null);
					} else {
						this.setText(null);
						setGraphic(null);
					}
				}
			};
		}
	};

	private boolean inited = false;

	private void init1() {
		if (!inited) {
			inited = true;
			// -----------------------连锁
			toogleBtnSubChainEnable.selectedProperty().addListener((p0, p1, p2) -> {
				if (p2 != chainHolder.isEnable()) {
					chainHolder.setEnable(p2);
					linkageHolderRepository.saveAndFlush(chainHolder);
				}
			});
			// 连锁名称监听
			listViewLinkage.getSelectionModel().selectedItemProperty().addListener((p1, p2, linkage) -> {
				if (null == linkage) {
					return;
				}
				selectedChain = (SubChain) linkage;
				refreshSubChainCondition();
				refreshSubChainEffect();
			});
			// 设置连锁条件显示格式
			listViewChainCondition.setCellFactory(linkageConditionCellFactory);
			listViewChainEffect.setCellFactory(effectCellFactory);
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
			toogleBtnTimingEnable.selectedProperty().addListener((p0, p1, p2) -> {
				if (p2 != timingHolder.isEnable()) {
					timingHolder.setEnable(p2);
					linkageHolderRepository.saveAndFlush(timingHolder);
				}
			});
			// 定时名称监听
			listViewTiming.getSelectionModel().selectedItemProperty().addListener((p1, p2, linkage) -> {
				if (null == linkage) {
					return;
				}
				selectedTiming = (Timing) linkage;
				refreshTimingTimer();
				refreshTimingEffect();
			});
			// 设置定时时间显示格式
			listViewTimingZTimer.setCellFactory(new Callback<ListView<ZTimer>, ListCell<ZTimer>>() {
				@Override
				public ListCell<ZTimer> call(ListView<ZTimer> param) {
					return new ListCell<ZTimer>() {
						@Override
						protected void updateItem(ZTimer item, boolean empty) {
							super.updateItem(item, empty);
							if (null != item && !empty) {
								this.setText(item.onOffTimerStr());
								setGraphic(null);
							} else {
								this.setText(null);
								setGraphic(null);
							}
						}
					};
				}
			});
			listViewTimingEffect.setCellFactory(effectCellFactory);
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

			// -----------------------循环
			toogleBtnLoopEnable.selectedProperty().addListener((p0, p1, p2) -> {
				if (p2 != loopHolder.isEnable()) {
					loopHolder.setEnable(p2);
					linkageHolderRepository.saveAndFlush(loopHolder);
				}
			});
			// 循环名称监听
			listViewLoop.getSelectionModel().selectedItemProperty().addListener((p1, p2, linkage) -> {
				if (null == linkage) {
					return;
				}
				selectedLoop = (ZLoop) linkage;
				refreshLoopCondition();
				refreshLoopCount();
				refreshListViewLoopDuration();
				refreshLoopEffect();
			});
			// 设置循环条件显示格式
			listViewLoopCondition.setCellFactory(linkageConditionCellFactory);
			cbLoopInfinite.selectedProperty().addListener(this::loopInfiniteChanged);
			listViewLoopDuration.setCellFactory(new Callback<ListView<LoopDuration>, ListCell<LoopDuration>>() {

				@Override
				public ListCell<LoopDuration> call(ListView<LoopDuration> param) {
					return new ListCell<LoopDuration>() {
						@Override
						protected void updateItem(LoopDuration item, boolean empty) {
							super.updateItem(item, empty);
							if (null != item && !empty) {
								String name = String.format("开 %02d:%02d%02d - 关 %02d:%02d%02d",
										item.getOnKeepTime().getHour(), item.getOnKeepTime().getMinute(),
										item.getOnKeepTime().getSecond(), item.getOffKeepTime().getHour(),
										item.getOffKeepTime().getMinute(), item.getOffKeepTime().getSecond());
								this.setText(name);
								setGraphic(null);
							} else {
								this.setText(null);
								setGraphic(null);
							}
						}
					};
				}

			});
			listViewLoopEffect.setCellFactory(effectCellFactory);
			// 循环条件监听
			listViewLoopCondition.getSelectionModel().selectedItemProperty().addListener((p1, p2, p3) -> {
				if (null == p3) {
					return;
				}
				selectedLoopCondition = p3;
			});
			// 循环影响监听
			listViewLoopDuration.getSelectionModel().selectedItemProperty().addListener((p1, p2, p3) -> {
				if (null == p3) {
					return;
				}
				selectedLoopDuration = p3;
			});
			// 循环影响监听
			listViewLoopEffect.getSelectionModel().selectedItemProperty().addListener((p1, p2, p3) -> {
				if (null == p3) {
					return;
				}
				selectedLoopEffect = p3;
			});
			// -------------循环--------------

			// -----------------------呱呱
			toogleBtnGuaguaEnable.selectedProperty().addListener((p0, p1, p2) -> {
				if (p2 != guaguaHolder.isEnable()) {
					guaguaHolder.setEnable(p2);
					linkageHolderRepository.saveAndFlush(guaguaHolder);
				}
			});
			// 连锁名称监听
			listViewGuagua.getSelectionModel().selectedItemProperty().addListener((p1, p2, linkage) -> {
				if (null == linkage) {
					return;
				}
				selectedGuagua = (SubChain) linkage;
				refreshGuaguaCondition();
				refreshGuaguaEffect();
			});
			// 设置连锁条件显示格式
			listViewGuaguaCondition.setCellFactory(linkageConditionCellFactory);
			listViewGuaguaEffect.setCellFactory(guaguaEffectCellFactory);
			// 连锁条件监听
			listViewGuaguaCondition.getSelectionModel().selectedItemProperty().addListener((p1, p2, p3) -> {
				if (null == p3) {
					return;
				}
				selectedGuaguaCondition = p3;
			});
			// 连锁影响监听
			listViewGuaguaEffect.getSelectionModel().selectedItemProperty().addListener((p1, p2, p3) -> {
				if (null == p3) {
					return;
				}
				selectedGuaguaEffect = p3;
			});
			// -------------呱呱--------------
		}
	}

	public void init() {
		chainHolder = UserService.user.getListDevGroup().get(0).getChainHolder();
		timingHolder = UserService.user.getListDevGroup().get(0).getTimingHolder();
		loopHolder = UserService.user.getListDevGroup().get(0).getLoopHolder();
		guaguaHolder = UserService.user.getListDevGroup().get(0).getGuaguaHolder();
		toogleBtnSubChainEnable.setSelected(chainHolder.isEnable());
		toogleBtnTimingEnable.setSelected(timingHolder.isEnable());
		toogleBtnLoopEnable.setSelected(loopHolder.isEnable());
		toogleBtnGuaguaEnable.setSelected(guaguaHolder.isEnable());
		init1();

		refreshListViewSubChain();
		refreshListViewTiming();
		refreshListViewLoop();
		refreshListViewGuagua();
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
		listViewChainCondition.getItems().addAll(listCondition);
	}

	// 刷新连锁受控列表
	private void refreshSubChainEffect() {
		listViewChainEffect.getItems().clear();
		if (null == selectedChain) {
			return;
		}
		List<Effect> listEffect = selectedChain.getListEffect();
		listViewChainEffect.getItems().addAll(listEffect);
	}

	// 连锁名称使能事件
	public void linkageEnablechanged(ObservableValue<? extends Boolean> prop, Boolean oldValue, Boolean newValue) {
//		SimpleBooleanProperty boolProp = (SimpleBooleanProperty) prop;
//		Linkage linkage = (Linkage) boolProp.getBean();
//		logger.info("linkage enable changed " + linkage.getName() + linkage.isEnable());
	}

	// 添加连锁按钮
	public void btnAddSubChain() {
		RenameController renameController = (RenameController) renameView.getPresenter();
		renameController.init(null);
		IntelDevPcApplication.showView(RenameView.class, Modality.WINDOW_MODAL);
		if (renameController.result) {
			SubChain linkage = new SubChain();
			linkage.setName(renameController.getNewName());
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
//			logger.info("edit " + name);
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

	private void refreshTimingTimer() {
		listViewTimingZTimer.getItems().clear();
		if (null == selectedTiming) {
			return;
		}
		List<ZTimer> list = selectedTiming.getListZTimer();
		listViewTimingZTimer.getItems().addAll(list);
	}

	// 刷新连锁受控列表
	private void refreshTimingEffect() {
		listViewTimingEffect.getItems().clear();
		if (null == selectedTiming) {
			return;
		}
		List<Effect> listEffect = selectedTiming.getListEffect();
		listViewTimingEffect.getItems().addAll(listEffect);
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

	// 添加定时时间
	@FXML
	public void btnAddTimingZTimer() {
		if (null == selectedTiming) {
			return;
		}
		EditTimingZTimerController controller = (EditTimingZTimerController) editTimingZTimerView.getPresenter();
		controller.init(null);
		IntelDevPcApplication.showView(EditTimingZTimerView.class, Modality.WINDOW_MODAL);
		if (controller.result) {
			selectedTiming.addZTimer(controller.timer);
			zTimerRepository.saveAndFlush(controller.timer);
			refreshTimingTimer();
		}
	}

	// 编辑定时时间
	@FXML
	public void menuEditTimingZTimer() {
		if (null == selectedTimingZTimer) {
			return;
		}
		EditTimingZTimerController controller = (EditTimingZTimerController) editTimingZTimerView.getPresenter();
		controller.init(selectedTimingZTimer);
		IntelDevPcApplication.showView(EditTimingZTimerView.class, Modality.WINDOW_MODAL);
		if (controller.result) {
			zTimerRepository.saveAndFlush(selectedTimingZTimer);
			refreshTimingTimer();
		}
	}

	// 删除定时时间
	@FXML
	public void menuDelTimingZTimer() {
		if (null == selectedTimingZTimer) {
			return;
		}
		zTimerRepository.deleteById(selectedTimingZTimer.getId());
		refreshTimingTimer();
	}

	// 添加定时影响
	@FXML
	public void btnAddTimingEffect() {
		if (null == selectedTiming) {
			return;
		}
		EditLinkageEffectController controller = (EditLinkageEffectController) editLinkageEffectView.getPresenter();
		controller.init(null);
		IntelDevPcApplication.showView(EditLinkageEffectView.class, Modality.WINDOW_MODAL);
		if (controller.result) {
			boolean res = selectedTiming.addEffect(controller.effect);
			if (res) {
				linkageEffectRepository.saveAndFlush(controller.effect);
				refreshTimingEffect();
			}
		}
	}

	// 编辑连锁影响
	@FXML
	public void menuEditTimingEffect() {
		if (null == selectedTimingEffect) {
			return;
		}
		EditLinkageEffectController controller = (EditLinkageEffectController) editLinkageEffectView.getPresenter();
		controller.init(selectedTimingEffect);
		IntelDevPcApplication.showView(EditLinkageEffectView.class, Modality.WINDOW_MODAL);
		if (controller.result) {
			linkageEffectRepository.saveAndFlush(controller.effect);
			listViewTimingEffect.refresh();
		}
	}

	// 删除连锁影响
	@FXML
	public void menuDelTimingEffect() {
		if (null == selectedTimingEffect) {
			return;
		}
		selectedTiming.removeEffect(selectedTimingEffect);
		linkageEffectRepository.deleteById(selectedTimingEffect.getId());
		refreshTimingEffect();
	}

	// 定时使能
	@FXML
	public void toggleBtnTimingEnable() {
	}
	// -------------------------定时结束------------------------------

	// ==================================循环开始================================

	// 刷新循环名称列表
	private void refreshListViewLoop() {
		refreshListViewLinkageName(listViewLoop, loopHolder);
	}

	// 刷新循环条件列表
	private void refreshLoopCondition() {
		listViewLoopCondition.getItems().clear();
		if (null == selectedLoop) {
			return;
		}
		List<LinkageCondition> listCondition = selectedLoop.getListCondition();
		listViewLoopCondition.getItems().addAll(listCondition);
	}

	private void refreshLoopCount() {
		if (null == selectedLoop) {
			return;
		}
		if (selectedLoop.getLoopCount() == -1) {
			txtLoopCount.setDisable(true);
			btnSaveLoopCount.setDisable(true);
			cbLoopInfinite.setSelected(true);
		} else {
			txtLoopCount.setDisable(false);
			btnSaveLoopCount.setDisable(false);
			txtLoopCount.setText(String.valueOf(selectedLoop.getLoopCount()));
			cbLoopInfinite.setSelected(false);
		}
	}

	// 刷新循环区间
	private void refreshListViewLoopDuration() {
		listViewLoopDuration.getItems().clear();
		if (null == selectedLoop) {
			return;
		}
		List<LoopDuration> list = selectedLoop.getListLoopDuration();
		listViewLoopDuration.getItems().addAll(list);
	}

	// 刷新循环受控列表
	private void refreshLoopEffect() {
		listViewLoopEffect.getItems().clear();
		if (null == selectedLoop) {
			return;
		}
		List<Effect> listEffect = selectedLoop.getListEffect();
		listViewLoopEffect.getItems().addAll(listEffect);
	}

	// 无限循环状态改变
	public void loopInfiniteChanged(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
		if (newValue) {
			txtLoopCount.setDisable(true);
			btnSaveLoopCount.setDisable(true);
			selectedLoop.setLoopCount(-1);
			linkageService.updateLinkage(selectedLoop);
		} else {
			txtLoopCount.setDisable(false);
			btnSaveLoopCount.setDisable(false);
			int count = 0;
			try {
				count = Integer.parseInt(txtLoopCount.getText());
			} catch (Exception e) {
				e.printStackTrace();
			}
			selectedLoop.setLoopCount(count);
			linkageService.updateLinkage(selectedLoop);
		}
	}

	public void btnSaveLoopCountAction() {
		try {
			int count = Integer.parseInt(txtLoopCount.getText());
			selectedLoop.setLoopCount(count);
			linkageService.updateLinkage(selectedLoop);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 循环名称使能事件
	public void loopEnablechanged(ObservableValue<? extends Boolean> prop, Boolean oldValue, Boolean newValue) {
//		SimpleBooleanProperty boolProp = (SimpleBooleanProperty) prop;
//		Linkage linkage = (Linkage) boolProp.getBean();
//		logger.info("linkage enable changed " + linkage.getName() + linkage.isEnable());
	}

	// 添加循环按钮
	public void btnAddLoop() {
		RenameController renameController = (RenameController) renameView.getPresenter();
		renameController.init(null);
		IntelDevPcApplication.showView(RenameView.class, Modality.WINDOW_MODAL);
		if (renameController.result) {
			ZLoop loop = new ZLoop();
			loop.setName(renameController.getNewName());
			loop.setId(UUID.randomUUID().toString());
			loopHolder.addLinkage(loop);
			linkageService.addLinkage(loop);
			refreshListViewLoop();
		}
	}

	// 添加循环条件
	@FXML
	public void btnAddLoopCondition() {
		if (null == selectedLoop) {
			return;
		}
		EditLinkageConditionController controller = (EditLinkageConditionController) editLinkageConditionView
				.getPresenter();
		controller.init(null);
		IntelDevPcApplication.showView(EditLinkageConditionView.class, Modality.WINDOW_MODAL);
		if (controller.result) {
			selectedLoop.addCondition(controller.condition);
			linkageConditionRepository.saveAndFlush(controller.condition);
			refreshLoopCondition();
		}
	}

	// 添加循环区间
	@FXML
	private void btnAddLoopDurationAction() {
		if (null == selectedLoop) {
			return;
		}
		EditLoopDurationController controller = (EditLoopDurationController) editLoopDurationView.getPresenter();
		controller.init(null);
		IntelDevPcApplication.showView(EditLoopDurationView.class, Modality.WINDOW_MODAL);
		if (controller.result) {
			selectedLoop.addLoopDuration(controller.loopDuration);
			loopDurationRepository.saveAndFlush(controller.loopDuration);
			refreshListViewLoopDuration();
		}
	}

	// 添加循环影响按钮
	@FXML
	public void btnAddLoopEffect() {
		if (null == selectedLoop) {
			return;
		}
		EditLinkageEffectController controller = (EditLinkageEffectController) editLinkageEffectView.getPresenter();
		controller.init(null);
		IntelDevPcApplication.showView(EditLinkageEffectView.class, Modality.WINDOW_MODAL);
		if (controller.result) {
			selectedLoop.addEffect(controller.effect);
			linkageEffectRepository.saveAndFlush(controller.effect);
			refreshLoopEffect();
		}
	}

	// 编辑循环
	@FXML
	public void menuEditLoop() {
		Linkage linkage = listViewLoop.getSelectionModel().getSelectedItem();
		if (null != linkage) {
			String name = linkage.getName();
//			logger.info("edit " + name);
			RenameController renameController = (RenameController) renameView.getPresenter();
			renameController.init(name);
			IntelDevPcApplication.showView(RenameView.class, Modality.WINDOW_MODAL);
			if (renameController.result) {
				linkage.setName(renameController.getNewName());
				linkageService.updateLinkage(linkage);
				listViewLoop.refresh();
			}
		}
	}

	// 删除循环
	@FXML
	public void menuDelLoop() {
		Linkage linkage = listViewLoop.getSelectionModel().getSelectedItem();
		if (null != linkage) {
			if (null != linkage) {
				loopHolder.removeLinkage(linkage);
				linkageService.deleteLinkage(linkage);
				refreshListViewLoop();
			}
		}
	}

	// 编辑循环条件
	@FXML
	public void menuEditLoopCondition() {
		if (null == selectedLoopCondition) {
			return;
		}
		EditLinkageConditionController controller = (EditLinkageConditionController) editLinkageConditionView
				.getPresenter();
		controller.init(selectedLoopCondition);
		IntelDevPcApplication.showView(EditLinkageConditionView.class, Modality.WINDOW_MODAL);
		if (controller.result) {
			linkageConditionRepository.saveAndFlush(controller.condition);
			listViewLoopCondition.refresh();
		}
	}

	// 删除循环条件
	@FXML
	public void menuDelLoopCondition() {
		if (null == selectedLoopCondition) {
			return;
		}
		selectedLoop.removeCondition(selectedLoopCondition);
		linkageConditionRepository.deleteById(selectedLoopCondition.getId());
		refreshLoopCondition();
	}

	// 编辑循环区间
	@FXML
	public void menuLoopDurationEdit() {
		if (null == selectedLoopDuration) {
			return;
		}
		EditLoopDurationController controller = (EditLoopDurationController) editLoopDurationView.getPresenter();
		controller.init(selectedLoopDuration);
		IntelDevPcApplication.showView(EditLoopDurationView.class, Modality.WINDOW_MODAL);
		if (controller.result) {
			loopDurationRepository.saveAndFlush(controller.loopDuration);
			refreshListViewLoopDuration();
		}
	}

	// 删除循环区间
	@FXML
	public void menuLoopDurationDel() {
		if (null == selectedLoopDuration) {
			return;
		}
		selectedLoop.removeLoopDuration(selectedLoopDuration);
		loopDurationRepository.deleteById(selectedLoopDuration.getId());
		refreshListViewLoopDuration();
	}

	// 编辑循环影响
	@FXML
	public void menuEditLoopEffect() {
		if (null == selectedLoopEffect) {
			return;
		}
		EditLinkageEffectController controller = (EditLinkageEffectController) editLinkageEffectView.getPresenter();
		controller.init(selectedLoopEffect);
		IntelDevPcApplication.showView(EditLinkageEffectView.class, Modality.WINDOW_MODAL);
		if (controller.result) {
			linkageEffectRepository.saveAndFlush(controller.effect);
			listViewLoopEffect.refresh();
		}
	}

	// 删除循环影响
	@FXML
	public void menuDelLoopEffect() {
		if (null == selectedLoopEffect) {
			return;
		}
		selectedLoop.removeEffect(selectedLoopEffect);
		linkageEffectRepository.deleteById(selectedLoopEffect.getId());
		refreshLoopEffect();
	}
	// ------------------------------------循环结束----------------------------------------

	// ==================================呱呱开始================================

	// 刷新呱呱名称列表
	private void refreshListViewGuagua() {
		refreshListViewLinkageName(listViewGuagua, guaguaHolder);
	}

	// 刷新呱呱条件列表
	private void refreshGuaguaCondition() {
		listViewGuaguaCondition.getItems().clear();
		if (null == selectedGuagua) {
			return;
		}
		List<LinkageCondition> listCondition = selectedGuagua.getListCondition();
		listViewGuaguaCondition.getItems().addAll(listCondition);
	}

	// 刷新呱呱受控列表
	private void refreshGuaguaEffect() {
		listViewGuaguaEffect.getItems().clear();
		if (null == selectedGuagua) {
			return;
		}
		List<Effect> listEffect = selectedGuagua.getListEffect();
		listViewGuaguaEffect.getItems().addAll(listEffect);
	}

	// 呱呱名称使能事件
	public void toogleBtnGuaguaEnable(ObservableValue<? extends Boolean> prop, Boolean oldValue, Boolean newValue) {
		SimpleBooleanProperty boolProp = (SimpleBooleanProperty) prop;
		Linkage linkage = (Linkage) boolProp.getBean();
		linkageService.updateLinkage(linkage);
	}

	// 添加呱呱按钮
	public void btnAddGuagua() {
		RenameController renameController = (RenameController) renameView.getPresenter();
		renameController.init(null);
		IntelDevPcApplication.showView(RenameView.class, Modality.WINDOW_MODAL);
		if (renameController.result) {
			SubChain linkage = new SubChain();
			linkage.setName(renameController.getNewName());
			guaguaHolder.addLinkage(linkage);
			linkageService.addLinkage(linkage);
			refreshListViewGuagua();
		}
	}

	// 添加呱呱条件
	@FXML
	public void btnAddGuaguaCondition() {
		if (null == selectedGuagua) {
			return;
		}
		EditLinkageConditionController controller = (EditLinkageConditionController) editLinkageConditionView
				.getPresenter();
		controller.init(null);
		IntelDevPcApplication.showView(EditLinkageConditionView.class, Modality.WINDOW_MODAL);
		if (controller.result) {
			selectedGuagua.addCondition(controller.condition);
			linkageConditionRepository.saveAndFlush(controller.condition);
			refreshGuaguaCondition();
		}
	}

	// 添加呱呱影响按钮
	@FXML
	public void btnAddGuaguaEffect() {
		if (null == selectedGuagua) {
			return;
		}
		EditGuaguaEffectController controller = (EditGuaguaEffectController) editGuaguaEffectView.getPresenter();
		controller.init(null);
		IntelDevPcApplication.showView(EditGuaguaEffectView.class, Modality.WINDOW_MODAL);
		if (controller.result) {
			selectedGuagua.addEffect(controller.effect);
			linkageEffectRepository.saveAndFlush(controller.effect);
			refreshGuaguaEffect();
		}
	}

	// 编辑呱呱
	@FXML
	public void menuEditGuagua() {
		Linkage linkage = listViewGuagua.getSelectionModel().getSelectedItem();
		if (null != linkage) {
			String name = linkage.getName();
			RenameController renameController = (RenameController) renameView.getPresenter();
			renameController.init(name);
			IntelDevPcApplication.showView(RenameView.class, Modality.WINDOW_MODAL);
			if (renameController.result) {
				linkage.setName(renameController.getNewName());
				linkageService.updateLinkage(linkage);
				listViewGuagua.refresh();
			}
		}
	}

	// 删除呱呱
	@FXML
	public void menuDelGuagua() {
		Linkage linkage = listViewGuagua.getSelectionModel().getSelectedItem();
		if (null != linkage) {
			if (null != linkage) {
				guaguaHolder.removeLinkage(linkage);
				linkageService.deleteLinkage(linkage);
				refreshListViewGuagua();
			}
		}
	}

	// 编辑呱呱条件
	@FXML
	public void menuEditGuaguaCondition() {
		if (null == selectedGuaguaCondition) {
			return;
		}
		EditLinkageConditionController controller = (EditLinkageConditionController) editLinkageConditionView
				.getPresenter();
		controller.init(selectedGuaguaCondition);
		IntelDevPcApplication.showView(EditLinkageConditionView.class, Modality.WINDOW_MODAL);
		if (controller.result) {
			linkageConditionRepository.saveAndFlush(controller.condition);
			listViewGuaguaCondition.refresh();
		}
	}

	// 删除呱呱条件
	@FXML
	public void menuDelGuaguaCondition() {
		if (null == selectedGuaguaCondition) {
			return;
		}
		selectedGuagua.removeCondition(selectedGuaguaCondition);
		linkageConditionRepository.deleteById(selectedGuaguaCondition.getId());
		refreshGuaguaCondition();
	}

	// 编辑呱呱影响
	@FXML
	public void menuEditGuaguaEffect() {
		if (null == selectedGuaguaEffect) {
			return;
		}
		EditGuaguaEffectController controller = (EditGuaguaEffectController) editGuaguaEffectView.getPresenter();
		controller.init(selectedGuaguaEffect);
		IntelDevPcApplication.showView(EditGuaguaEffectView.class, Modality.WINDOW_MODAL);
		if (controller.result) {
			linkageEffectRepository.saveAndFlush(controller.effect);
			listViewGuaguaEffect.refresh();
		}
	}

	// 删除呱呱影响
	@FXML
	public void menuDelGuaguaEffect() {
		if (null == selectedGuaguaEffect) {
			return;
		}
		selectedGuagua.removeEffect(selectedGuaguaEffect);
		linkageEffectRepository.deleteById(selectedGuaguaEffect.getId());
		refreshGuaguaEffect();
	}
	// ------------------------------------呱呱结束----------------------------------------

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
