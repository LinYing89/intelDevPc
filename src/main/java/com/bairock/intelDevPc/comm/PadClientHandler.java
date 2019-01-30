package com.bairock.intelDevPc.comm;

import java.io.IOException;
import java.util.List;

import com.bairock.intelDevPc.IntelDevPcApplication;
import com.bairock.intelDevPc.SpringUtil;
import com.bairock.intelDevPc.controller.CtrlModelDialogController;
import com.bairock.intelDevPc.controller.MainController;
import com.bairock.intelDevPc.service.UserService;
import com.bairock.iot.intelDev.communication.DevChannelBridgeHelper;
import com.bairock.iot.intelDev.device.Coordinator;
import com.bairock.iot.intelDev.device.CtrlModel;
import com.bairock.iot.intelDev.device.DevStateHelper;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.Gear;
import com.bairock.iot.intelDev.device.IStateDev;
import com.bairock.iot.intelDev.device.OrderHelper;
import com.bairock.iot.intelDev.device.devcollect.DevCollect;
import com.bairock.iot.intelDev.device.devswitch.DevSwitch;
import com.bairock.iot.intelDev.order.DeviceOrder;
import com.bairock.iot.intelDev.order.OrderType;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

public class PadClientHandler extends ChannelInboundHandlerAdapter {
	Channel channel;

    boolean syncDevMsg = false;
    private MyMessageAnalysiser myMessageAnalysiser = new ServerMsgAnalysiser();
    
    private MainController mainController = SpringUtil.getBean(MainController.class);
    private CtrlModelDialogController ctrlModelDialogController = SpringUtil.getBean(CtrlModelDialogController.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        channel = ctx.channel();
        PadClient.getIns().setPadClientHandler(this);
        IntelDevPcApplication.SERVER_CONNECTED = true;
        if(null != mainController) {
        	mainController.refreshServerState(true);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf m = (ByteBuf)msg;
        try {
            byte[] req = new byte[m.readableBytes()];
            m.readBytes(req);
            String str = new String(req, "GBK");
            analysisMsg2(str);
//            displayMsg(str);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        //ctx.close();
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
        if(null != mainController) {
        	mainController.refreshServerState(false);
        }
        setRemoteDeviceAbnormal();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if (evt instanceof IdleStateEvent) {  // 2
            ctx.close();
        }
    }

    private void setRemoteDeviceAbnormal(){
    	IntelDevPcApplication.SERVER_CONNECTED = false;
    	if(null != mainController) {
        	mainController.refreshServerState(false);
        }
        for(Device device : UserService.user.getListDevGroup().get(0).getListDevice()){
            if(device.getCtrlModel() == CtrlModel.REMOTE) {
                device.setDevStateId(DevStateHelper.DS_YI_CHANG);
            }
        }
    }

    void send(String msg){
        try {
            if(null != channel) {
                channel.writeAndFlush(Unpooled.copiedBuffer(msg.getBytes("GBK")));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void displayMsg(String msg) {
        try {
            if (null == msg) {
                return;
            }
            String[] arryMsg = msg.split("\\$");
            for (String str : arryMsg) {
                if (!str.isEmpty()) {
                    analysisMsg(str);
                }
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
			switch(orderBase.getOrderType()) {
			case HEAD_USER_INFO : 
				if(null != UserService.user) {
					DeviceOrder ob = new DeviceOrder();
					ob.setOrderType(OrderType.HEAD_USER_INFO);
					ob.setUsername(UserService.user.getName());
					ob.setDevGroupName(UserService.getDevGroup().getName());
					order = om.writeValueAsString(ob);
	        		send(order);
	        	}
				break;
			case HEAD_SYN : 
				syncDevMsg = true;
				order = om.writeValueAsString(orderBase);
        		send(order);
				break;
			case HEAD_NOT_SYN : 
				syncDevMsg = false;
				order = om.writeValueAsString(orderBase);
        		send(order);
				break;
			case GEAR : 
				Device dev = UserService.getDevGroup().findDeviceWithCoding(orderBase.getLongCoding());
	            if(null == dev){
	                return;
	            }
	            dev.setGear(Gear.valueOf(orderBase.getData()));
				break;
			case CTRL_DEV:
				dev = UserService.getDevGroup().findDeviceWithCoding(orderBase.getLongCoding());
	            if(null == dev){
	                return;
	            }
	            dev.setCtrlModel(CtrlModel.REMOTE);
	            IStateDev stateDev = (IStateDev)dev;
	            if(orderBase.getData().equals(DevStateHelper.DS_KAI)) {
	            	order = stateDev.getTurnOnOrder();
	            }else {
	            	order = stateDev.getTurnOffOrder();
	            }
	            DevChannelBridgeHelper.getIns().sendDevOrder(dev, order, true);
				break;
			case STATE:
				dev = UserService.getDevGroup().findDeviceWithCoding(orderBase.getLongCoding());
	            if(null == dev){
	                return;
	            }
	            dev.findSuperParent().setCtrlModel(CtrlModel.REMOTE);
	            dev.setDevStateId(orderBase.getData());
				break;
			case VALUE:
				dev = UserService.getDevGroup().findDeviceWithCoding(orderBase.getLongCoding());
	            if(null == dev){
	                return;
	            }
	            dev.findSuperParent().setCtrlModel(CtrlModel.REMOTE);
	            ((DevCollect)dev).getCollectProperty().setCurrentValue(Float.valueOf(orderBase.getData()));
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

    private synchronized void analysisMsg(String strData) {
    	
        strData = strData.replaceAll("\n", "").replaceAll("\r", "").trim();
        String msg = strData;
        if(msg.contains("#")) {
            msg = msg.substring(0, msg.indexOf("#"));
        }
        if(msg.contains("h2")){
        	if(null != UserService.user) {
        		send(OrderHelper.getOrderMsg("UN" + UserService.user.getName() + OrderHelper.SEPARATOR + UserService.user.getListDevGroup().get(0).getName()));
        	}
        }else if(msg.contains("h1")){
            syncDevMsg = true;
            send(OrderHelper.getOrderMsg("h1"));
        }else if(msg.contains("h3")){
            syncDevMsg = false;
            send(OrderHelper.getOrderMsg("h3"));
        }else if(msg.startsWith("RF")){
            //更新状态
            for(Device device : UserService.user.getListDevGroup().get(0).getListDevice()){
                if(device instanceof Coordinator){
                    Coordinator coordinator = (Coordinator)device;
                    for (Device device1 : coordinator.getListDev()){
                        queueDevice(device1);
                    }
                }else{
                    queueDevice(device);
                }
            }
        }else if(msg.startsWith("C")) {
            //网页控制命令"C"表示控制命令, like: CB10001:31
            if(msg.length() < 5){
                return;
            }
            int index = msg.indexOf(":");
            String devCoding = msg.substring(1, index);
            Device dev = UserService.user.getListDevGroup().get(0).findDeviceWithCoding(devCoding);
            if(null == dev){
                return;
            }

            String state = msg.substring(index + 1, index + 2);
            if(!state.equals("0")){
                //不是自动信息
                DevChannelBridgeHelper.getIns().sendDevOrder(dev, OrderHelper.getOrderMsg(msg), true);
            }

            int gear = Integer.parseInt(Gear.getPadGearModel(state));
            if(dev instanceof DevSwitch){
                String subDevCode = msg.substring(index + 2, index + 3);
                Device device = ((DevSwitch) dev).getSubDevBySc(subDevCode);
                Gear.setGearModel(device, gear);
            }else{
                Gear.setGearModel(dev, gear);
            }
//            if(Config.INSTANCE.getCtrlRing()) {
//                Media.INSTANCE.playCtrlRing();
//            }
        }else if(msg.startsWith("I")){
            //推送或响应命令 like: IB10001:81
            int index = msg.indexOf(":");
            if(index == -1 || index + 2 > msg.length()){
                return;
            }
            String state = msg.substring(index + 1);
            String stateHead = state.substring(0, 1);

            if(stateHead.equals("a")) {
                //设置模式时服务器响应命令
                if (null != ctrlModelDialogController && ctrlModelDialogController.setting) {
                    String model = state.substring(1, 2);
                    if(model.equals("1")) {
                    	//服务器收到设为远程命令返回
                    	ctrlModelDialogController.setModelProgressValue(1);
                    }else{
                    	//服务器收到设为本地命令返回
                    	ctrlModelDialogController.setModelProgressValue(2);
                    }
                }
                return;
            }


            //String devCoding = msg.substring(1, index);
            List<Device> listDev = myMessageAnalysiser.putMsg("$" + strData, UserService.user);
            if(listDev == null || listDev.isEmpty()){
                return;
            }
            Device dev = listDev.get(0);
            if(stateHead.equals("2")){
                String s1 = state.substring(1,2);
                if(s1.equals(DevStateHelper.getIns().getDs(DevStateHelper.DS_YI_CHANG))){
                    dev.setDevStateId(DevStateHelper.DS_YI_CHANG);
                }
            }
        }
    }

    private void queueDevice(Device device){
    	//发送挡位信息
        if(device instanceof DevSwitch){
            for(Device device1 : ((DevSwitch) device).getListDev()){
                PadClient.getIns().send(OrderHelper.getOrderMsg(OrderHelper.FEEDBACK_HEAD  + device1.getLongCoding() + OrderHelper.SEPARATOR + "b" + device1.getGear()));
            }
        }
        
        //本地设备才往服务器发送状态，远程设备只接收服务器状态
        if(device.findSuperParent().getCtrlModel() == CtrlModel.REMOTE) {
            return;
        }
        if(device.isNormal()){
            IntelDevPcApplication.sendOrder(device, device.createInitOrder(), true);
        }else{
            //发送异常信息
            PadClient.getIns().send(device.createAbnormalOrder());
        }
        
    }
}
