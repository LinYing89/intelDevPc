package com.bairock.intelDevPc.comm;

import java.io.IOException;

import com.bairock.intelDevPc.IntelDevPcApplication;
import com.bairock.intelDevPc.SpringUtil;
import com.bairock.intelDevPc.controller.CtrlModelDialogController;
import com.bairock.intelDevPc.controller.MainController;
import com.bairock.intelDevPc.service.UserService;
import com.bairock.iot.intelDev.communication.DevChannelBridgeHelper;
import com.bairock.iot.intelDev.device.CtrlModel;
import com.bairock.iot.intelDev.device.DevStateHelper;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.Gear;
import com.bairock.iot.intelDev.device.IStateDev;
import com.bairock.iot.intelDev.device.SetDevModelTask;
import com.bairock.iot.intelDev.device.devcollect.DevCollect;
import com.bairock.iot.intelDev.order.DeviceOrder;
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
				break;
			case HEAD_SYN:
				syncDevMsg = true;
				order = om.writeValueAsString(orderBase);
				send(order);
				break;
			case HEAD_NOT_SYN:
				syncDevMsg = false;
				order = om.writeValueAsString(orderBase);
				send(order);
				break;
			case GEAR:
				Device dev = UserService.getDevGroup().findDeviceWithCoding(orderBase.getLongCoding());
				if (null == dev) {
					return;
				}
				dev.setGear(Gear.valueOf(orderBase.getData()));
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
				dev.findSuperParent().setCtrlModel(CtrlModel.REMOTE);
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
				((DevCollect) dev).getCollectProperty().setCurrentValue(Float.valueOf(orderBase.getData()));
				isToCtrlModelDev(dev);
				break;
			case TO_REMOTE_CTRL_MODEL:
				if (!orderBase.getData().equals("OK")) {
					Platform.runLater(() ->{
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
			ob.setUsername(UserService.user.getName());
			ob.setDevGroupName(UserService.getDevGroup().getName());
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
