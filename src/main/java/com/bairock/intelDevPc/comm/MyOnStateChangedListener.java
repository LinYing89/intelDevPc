package com.bairock.intelDevPc.comm;

import com.bairock.intelDevPc.IntelDevPcApplication;
import com.bairock.iot.intelDev.communication.RefreshCollectorValueHelper;
import com.bairock.iot.intelDev.device.CtrlModel;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.Device.OnStateChangedListener;
import com.bairock.iot.intelDev.device.devcollect.DevCollectClimateContainer;
import com.bairock.iot.intelDev.device.devswitch.SubDev;

public class MyOnStateChangedListener implements OnStateChangedListener {

//	private MainController mainController = SpringUtil.getBean(MainController.class);
	
	@Override
	public void onStateChanged(Device dev, String stateId) {
//		if(null != mainController) {
//        	mainController.refreshDevicePaneState(dev);
//        }
	}

	@Override
	public void onNormalToAbnormal(Device dev) {
		IntelDevPcApplication.addOfflineDevCoding(dev);
        //本地设备才往服务器发送状态，远程设备只接收服务器状态
        if(!(dev instanceof SubDev) && dev.findSuperParent().getCtrlModel() == CtrlModel.LOCAL) {
            PadClient.getIns().send(dev.createAbnormalOrder());
        }
        if(dev instanceof DevCollectClimateContainer){
            RefreshCollectorValueHelper.getIns().endRefresh(dev);
        }
	}

	@Override
	public void onAbnormalToNormal(Device dev) {
		IntelDevPcApplication.removeOfflineDevCoding(dev);

        if(dev instanceof DevCollectClimateContainer){
            RefreshCollectorValueHelper.getIns().RefreshDev(dev);
        }
	}

	@Override
	public void onNoResponse(Device dev) {
		// TODO Auto-generated method stub

	}

}
