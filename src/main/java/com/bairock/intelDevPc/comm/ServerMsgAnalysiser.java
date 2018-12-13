package com.bairock.intelDevPc.comm;

import com.bairock.intelDevPc.IntelDevPcApplication;
import com.bairock.intelDevPc.SpringUtil;
import com.bairock.intelDevPc.controller.CtrlModelDialogController;
import com.bairock.iot.intelDev.device.CtrlModel;
import com.bairock.iot.intelDev.device.Device;

public class ServerMsgAnalysiser extends MyMessageAnalysiser {
	
	private CtrlModelDialogController ctrlModelDialogController = SpringUtil.getBean(CtrlModelDialogController.class);
	
	@Override
    public void deviceFeedback(Device device, String msg) {
        //PadClient.getIns().sendIfSync("$" + msg);
        //device.setLinkType(LinkType.NET);
        updateDevice(device);
    }

    @Override
    public void updateDevice(Device device){
        if(device.getCtrlModel() != CtrlModel.REMOTE){
            device.setCtrlModel(CtrlModel.REMOTE);
            //远程设备第一次返回询问状态
            IntelDevPcApplication.sendOrder(device, device.createInitOrder(), true);
        }
        if (null != ctrlModelDialogController && ctrlModelDialogController.setting
				&& ctrlModelDialogController.deviceModelHelper != null
				&& ctrlModelDialogController.deviceModelHelper.getDevToSet() == device
				&& ctrlModelDialogController.deviceModelHelper.getCtrlModel() == CtrlModel.REMOTE) {
			ctrlModelDialogController.setModelProgressValue(3);
		}
    }

    @Override
    public void configDeviceCtrlModel(Device device, String s) {
    }
}
