package com.bairock.intelDevPc.comm;

import com.bairock.intelDevPc.SpringUtil;
import com.bairock.intelDevPc.controller.MainController;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.Device.OnGearChangedListener;
import com.bairock.iot.intelDev.device.Gear;
import com.bairock.iot.intelDev.device.OrderHelper;

public class MyOnGearChangedListener implements OnGearChangedListener {

	private MainController mainController = SpringUtil.getBean(MainController.class);
	
	@Override
	public void onGearChanged(Device dev, Gear gear) {
		//本地设备才往服务器发送状态，远程设备只接收服务器状态
//        if(dev.findSuperParent().getCtrlModel() == CtrlModel.LOCAL) {
//            PadClient.getIns().send(OrderHelper.getOrderMsg(OrderHelper.FEEDBACK_HEAD + dev.getLongCoding() + OrderHelper.SEPARATOR + "b" + dev.getGear()));
//        }
        PadClient.getIns().send(OrderHelper.getOrderMsg(OrderHelper.FEEDBACK_HEAD + dev.getLongCoding() + OrderHelper.SEPARATOR + "b" + dev.getGear()));
        if(null != mainController) {
        	mainController.refreshDevicePaneGear(dev);
        }
//        refreshUi(device);
	}

}
