package com.bairock.intelDevPc.comm;

import java.io.IOException;

import com.bairock.intelDevPc.IntelDevPcApplication;
import com.bairock.intelDevPc.SpringUtil;
import com.bairock.intelDevPc.Util;
import com.bairock.intelDevPc.controller.CtrlModelDialogController;
import com.bairock.intelDevPc.controller.MainController;
import com.bairock.intelDevPc.data.Config;
import com.bairock.intelDevPc.service.UserService;
import com.bairock.iot.intelDev.communication.DevChannelBridge;
import com.bairock.iot.intelDev.communication.DevChannelBridgeHelper;
import com.bairock.iot.intelDev.communication.DevServer;
import com.bairock.iot.intelDev.communication.FindDevHelper;
import com.bairock.iot.intelDev.device.CtrlModel;
import com.bairock.iot.intelDev.device.DevHaveChild;
import com.bairock.iot.intelDev.device.DevStateHelper;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.Gear;
import com.bairock.iot.intelDev.device.IStateDev;
import com.bairock.iot.intelDev.device.SetDevModelTask;
import com.bairock.iot.intelDev.device.devcollect.DevCollect;
import com.bairock.iot.intelDev.order.DeviceOrder;
import com.bairock.iot.intelDev.order.LoginModel;
import com.bairock.iot.intelDev.order.OrderType;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class PadClientHandler extends ChannelInboundHandlerAdapter {
	Channel channel;

	boolean syncDevMsg = false;

	private MainController mainController = SpringUtil.getBean(MainController.class);
	private CtrlModelDialogController ctrlModelDialogController = SpringUtil.getBean(CtrlModelDialogController.class);
	private Config config = SpringUtil.getBean(Config.class);

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		channel = ctx.channel();
		PadClient.getIns().setPadClientHandler(this);
		IntelDevPcApplication.SERVER_CONNECTED = true;
		if (null != mainController) {
			mainController.refreshServerState(true);
		}
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {

		String str = (String) msg;
		analysisMsg2(str);

//        ByteBuf m = (ByteBuf)msg;
//        try {
//            byte[] req = new byte[m.readableBytes()];
//            m.readBytes(req);
//            String str = new String(req, "GBK");
//            analysisMsg2(str);
////            displayMsg(str);
//        }catch (Exception e){
//            e.printStackTrace();
//        }
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		super.exceptionCaught(ctx, cause);
		// ctx.close();
//        PadClient.getIns().setPadClientHandler(null);
//        if(null != MainActivity.handler){
//            MainActivity.handler.obtainMessage(MainActivity.REFRESH_TITLE, "(未连接)").sendToTarget();
//        }
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		ctx.close();
		PadClient.getIns().setPadClientHandler(null);
		if (null != mainController) {
			mainController.refreshServerState(false);
		}
		setRemoteDeviceAbnormal();
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		super.userEventTriggered(ctx, evt);
		if (evt instanceof IdleStateEvent) { // 2
			ctx.close();
		}
	}

	private void setRemoteDeviceAbnormal() {
		IntelDevPcApplication.SERVER_CONNECTED = false;
		if (null != mainController) {
			mainController.refreshServerState(false);
		}
		for (Device device : UserService.user.getListDevGroup().get(0).getListDevice()) {
			if (device.getCtrlModel() == CtrlModel.REMOTE) {
				device.setDevStateId(DevStateHelper.DS_YI_CHANG);
			}
		}
	}

	void send(String msg) {
		msg = msg + System.getProperty("line.separator");
		try {
			if (null != channel) {
				channel.writeAndFlush(Unpooled.copiedBuffer(msg.getBytes("GBK")));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void analysisMsg2(String strData) {
		ObjectMapper om = new ObjectMapper();
		try {
			DeviceOrder orderBase = om.readValue(strData, DeviceOrder.class);
			String order = "";
			switch (orderBase.getOrderType()) {
			case HEAD_USER_INFO:
				sendUserInfo();
				if (config.getLoginModel().equals(LoginModel.LOCAL)) {
					// 发送设备状态
					sendInitStateToServer();
				}
				break;
			case HEAD_SYN:
				syncDevMsg = true;
//				order = om.writeValueAsString(orderBase);
				send(strData);
				break;
			case HEAD_NOT_SYN:
				syncDevMsg = false;
//				order = om.writeValueAsString(orderBase);
				send(strData);
				break;
			case GEAR:
				Device dev = UserService.getDevGroup().findDeviceWithCoding(orderBase.getLongCoding());
				if (null == dev) {
					return;
				}
				dev.setGear(Gear.valueOf(orderBase.getData()));
				if(config.getLoginModel().equals(LoginModel.LOCAL)) {
					send(strData);
				}
				break;
			case CTRL_DEV:
				dev = UserService.getDevGroup().findDeviceWithCoding(orderBase.getLongCoding());
				if (null == dev) {
					return;
				}
				dev.setCtrlModel(CtrlModel.REMOTE);
				IStateDev stateDev = (IStateDev) dev;
				if (orderBase.getData().equals(DevStateHelper.DS_KAI)) {
					order = stateDev.getTurnOnOrder();
				} else {
					order = stateDev.getTurnOffOrder();
				}
				DevChannelBridgeHelper.getIns().sendDevOrder(dev, order, true);
				break;
			case STATE:
				dev = UserService.getDevGroup().findDeviceWithCoding(orderBase.getLongCoding());
				if (null == dev) {
					return;
				}
				Device devParent = dev.findSuperParent();

                if(!devParent.isNormal()){
                    devParent.setDevStateId(DevStateHelper.DS_ZHENG_CHANG);
                }
                devParent.setCtrlModel(CtrlModel.REMOTE);
                
				dev.setDevStateId(orderBase.getData());
				isToCtrlModelDev(dev);
				break;
			case VALUE:
				dev = UserService.getDevGroup().findDeviceWithCoding(orderBase.getLongCoding());
				if (null == dev) {
					return;
				}
				dev.setDevStateId(DevStateHelper.DS_ZHENG_CHANG);
				dev.findSuperParent().setCtrlModel(CtrlModel.REMOTE);
				try {
					((DevCollect) dev).getCollectProperty().setCurrentValue(Float.valueOf(orderBase.getData()));
				} catch (Exception e) {
				}
				isToCtrlModelDev(dev);
				break;
			case TO_REMOTE_CTRL_MODEL:
				if (!orderBase.getData().equals("OK")) {
					Platform.runLater(() -> {
						Alert alert = new Alert(AlertType.INFORMATION);
						alert.titleProperty().set("信息");
						alert.headerTextProperty().set("请先上传数据");
						alert.showAndWait();
					});
				} else {
					if (null != ctrlModelDialogController && SetDevModelTask.setting) {
						// 服务器收到设为远程命令返回
						ctrlModelDialogController.setModelProgressValue(1);
					}
				}
				break;
			case LOGOUT:
				// 登出
				Util.CAN_CONNECT_SERVER = false;
				// 关闭服务器连接
				DevServer.getIns().close();
				channel.close();

				// 关闭本地服务器
				for (DevChannelBridge db : DevChannelBridgeHelper.getIns().getListDevChannelBridge()) {
					db.close();
				}

				// 停止寻找设备
				FindDevHelper.getIns().enable = false;
				mainController.showLogoutDialog();
				break;
			default:
				break;
			}
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void sendUserInfo() {
		if (null != UserService.user) {
			DeviceOrder ob = new DeviceOrder();
			ob.setOrderType(OrderType.HEAD_USER_INFO);
			ob.setUsername(config.getUserid());
			ob.setDevGroupName(config.getDevGroupName());
			ob.setData(config.getLoginModel());
			ObjectMapper om = new ObjectMapper();
			String order;
			try {
				order = om.writeValueAsString(ob);
				send(order);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void sendInitStateToServer() {
		for (Device d : UserService.getDevGroup().getListDevice()) {
			sendInitStateToServer(d);
		}
	}

	// 发送设备状态到服务器
	private void sendInitStateToServer(Device dev) {
		if (null != dev && dev.isNormal() && dev.isVisibility()) {
			// 从缓存中读取对象, 保存状态一致
			DeviceOrder devOrder = null;
			if (dev instanceof DevCollect) {
				devOrder = new DeviceOrder(OrderType.VALUE, dev.getId(), dev.getLongCoding(),
						String.valueOf(((DevCollect) dev).getCollectProperty().getCurrentValue()));
			} else {
				devOrder = new DeviceOrder(OrderType.STATE, dev.getId(), dev.getLongCoding(), dev.getDevStateId());
				if (dev instanceof IStateDev) {
					// 发送档位
					DeviceOrder devo = new DeviceOrder(OrderType.GEAR, dev.getId(), dev.getLongCoding(),
							dev.getGear().toString());
					String strOrder = Util.orderBaseToString(devo);
					send(strOrder);
				}
			}
			if (null != devOrder) {
				String strOrder = Util.orderBaseToString(devOrder);
				send(strOrder);
			}
			if (dev instanceof DevHaveChild) {
				for (Device d : ((DevHaveChild) dev).getListDev()) {
					sendInitStateToServer(d);
				}
			}
		}
	}

	private boolean isToCtrlModelDev(Device device) {
		if (null != ctrlModelDialogController && SetDevModelTask.setting
				&& ctrlModelDialogController.setDevModelThread.deviceModelHelper != null
				&& ctrlModelDialogController.setDevModelThread.deviceModelHelper.getDevToSet() == device
				&& ctrlModelDialogController.setDevModelThread.deviceModelHelper.getCtrlModel() == CtrlModel.REMOTE) {
			ctrlModelDialogController.setModelProgressValue(3);
		}
		return false;
	}
}
