package com.bairock.intelDevPc.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.bairock.intelDevPc.IntelDevPcApplication;
import com.bairock.intelDevPc.IntelDevPcApplication.OnStageCreatedListener;
import com.bairock.intelDevPc.comm.PadClient;
import com.bairock.intelDevPc.data.Config;
import com.bairock.intelDevPc.data.DeviceModelHelper;
import com.bairock.intelDevPc.service.UserService;
import com.bairock.intelDevPc.view.CtrlModelDialogView;
import com.bairock.iot.intelDev.communication.DevChannelBridgeHelper;
import com.bairock.iot.intelDev.communication.DevServer;
import com.bairock.iot.intelDev.device.CtrlModel;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.OrderHelper;
import com.bairock.iot.intelDev.user.DevGroup;
import com.bairock.iot.intelDev.user.IntelDevHelper;
import com.bairock.iot.intelDev.user.User;

import de.felixroske.jfxsupport.FXMLController;
import javafx.application.Platform;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

@FXMLController
public class CtrlModelDialogController {

	public DeviceModelHelper deviceModelHelper = new DeviceModelHelper();
	public boolean setting = false;

	@Autowired
	private CtrlModelDialogView ctrlModelDialogView;
	@Autowired
	private Config config;

	@FXML
	private Label labelMessage;
	@FXML
	private Button btnOk;

	public static final int LOCAL = 0;
	public static final int DOWNLOAD = 1;

	private CtrlModel oldCtrlModel;

	private SetDevModelThread setDevModelThread;

	public void init(Device device) {
		
//		btnOk.setDisable(true);
		oldCtrlModel = device.getCtrlModel();
		deviceModelHelper = new DeviceModelHelper();
		deviceModelHelper.setDevToSet(device);
		String order;
		if (oldCtrlModel == CtrlModel.LOCAL) {
			deviceModelHelper.setCtrlModel(CtrlModel.REMOTE);
			order = device.createTurnRemoteModelOrder(config.getServerName(), config.getDevPort());
			setMessage("正在设置远程模式, 请稍等...");
		} else {
			deviceModelHelper.setCtrlModel(CtrlModel.LOCAL);
			order = device.createTurnLocalModelOrder(IntelDevHelper.getLocalIp(), DevServer.PORT);
			setMessage("正在设置本地模式, 请稍等...");
		}
		deviceModelHelper.setOrder(order);

		setDevModelThread = new SetDevModelThread();
		setDevModelThread.setOnResultListener(new OnResultListener() {

			@Override
			public void onResult(boolean result) {
				// 设置线程的结果
				loadResult(result);
			}
		});
		IntelDevHelper.executeThread(setDevModelThread);
	}

	public void handlerOk() {
		setting = false;
		ctrlModelDialogView.getView().getScene().getWindow().hide();
	}
	
	public void handle(WindowEvent e) {
		EventType<WindowEvent> type = e.getEventType();
		if (type == WindowEvent.WINDOW_CLOSE_REQUEST || type == WindowEvent.WINDOW_HIDING) {
			System.out.println("ctrlModelDialogView WINDOW_CLOSE_REQUEST");
			setting = false;
			setDevModelThread.interrupt();
		}
	}
	
	//设置进度
	public void setModelProgressValue(int value) {
		setDevModelThread.setModelProgressValue = value;
	}

	public void loadResult(boolean success) {
		Platform.runLater(() -> {
			btnOk.setDisable(false);
		});
		String str;
		if (success) {
			if (oldCtrlModel == CtrlModel.LOCAL) {
				str = "设置远程模式成功";
			} else {
				str = "设置本地模式成功";
			}
		} else {
			if (oldCtrlModel == CtrlModel.REMOTE) {
				str = "设置本地模式失败";
			} else {
				str = "设置远程模式失败";
			}
		}
		setMessage(str);
	}

	private void setMessage(String message) {
		Platform.runLater(() -> {
			labelMessage.setText(message);
		});
	}

	private class SetDevModelThread extends Thread {

		// 设置模式进度
		// 0向服务器发
		// 1向设备发
		public int setModelProgressValue = 0;
		private int count;

		private OnResultListener onResultListener;

		public void setOnResultListener(OnResultListener onResultListener) {
			this.onResultListener = onResultListener;
		}

		@Override
		public void run() {
			try {
				setting = true;
				while (setModelProgressValue < 3 && setting) {
					count++;
					if (count > 10) {
						deviceModelHelper = null;
						onResult(false);
						return;
					}
					if (setModelProgressValue == 0) {
						// 第一步 向服务器发
						if (deviceModelHelper.getCtrlModel() == CtrlModel.REMOTE) {
							String oldOrder = deviceModelHelper.getOrder().substring(1,
									deviceModelHelper.getOrder().indexOf("#"));
							User user = new User();
							user.setName(UserService.user.getName());
							DevGroup group = new DevGroup();
							group.setName(UserService.getDevGroup().getName());
							user.addGroup(group);
							group.addDevice(deviceModelHelper.getDevToSet());
							String jsonOrder = UserService.getUserJson(user);
							PadClient.getIns().send(OrderHelper.getOrderMsg(oldOrder + ":" + jsonOrder));
							deviceModelHelper.getDevToSet().setDevGroup(UserService.getDevGroup());
						} else {
							String oldOrder = deviceModelHelper.getOrder().substring(1,
									deviceModelHelper.getOrder().indexOf("#"));
							oldOrder += ":u" + UserService.user.getName() + ":g" + UserService.getDevGroup().getName();
							PadClient.getIns().send(OrderHelper.getOrderMsg(oldOrder));
						}
					} else if (setModelProgressValue == 1) {
						// 第二步
						// 如果是设为远程模式, 向本地发送报文
						// 如果是设为本地模式, 不需要向本地发送报文, 只需向服务器发, 收到服务器的回应后等待本地设备的心跳
						if (deviceModelHelper.getCtrlModel() == CtrlModel.REMOTE) {
							String oldOrder = deviceModelHelper.getOrder().substring(1,
									deviceModelHelper.getOrder().indexOf("#"));
							oldOrder += ":u" + UserService.user.getName() + ":g" + UserService.getDevGroup().getName();
							DevChannelBridgeHelper.getIns().sendDevOrder(deviceModelHelper.getDevToSet(),
									OrderHelper.getOrderMsg(oldOrder), true);
						}
					}
					Thread.sleep(5000);
				}
			} catch (Exception e) {
				e.printStackTrace();
				onResult(false);
				return;
			}
			onResult(true);
			setting = false;
		}

		private void onResult(boolean result) {
			if (null != onResultListener) {
				onResultListener.onResult(result);
			}
		}
	}

	interface OnResultListener {
		void onResult(boolean result);
	}
	
	public IntelDevPcApplication.OnStageCreatedListener onStageCreatedListener = new OnStageCreatedListener() {
		
		@Override
		public void onStageCreated(Stage newStage) {
			newStage.setOnCloseRequest(e -> handle(e));
			newStage.setOnHiding(e -> handle(e));
		}
	};

}
