package com.bairock.intelDevPc.comm;

import com.bairock.intelDevPc.SpringUtil;
import com.bairock.intelDevPc.controller.DevicesController;
import com.bairock.intelDevPc.repository.DeviceRepository;
import com.bairock.iot.intelDev.device.CtrlModel;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.Device.OnCtrlModelChangedListener;

public class MyOnCtrlModelChangedListener implements OnCtrlModelChangedListener {

	private DeviceRepository deviceRepository = SpringUtil.getBean(DeviceRepository.class);
	private DevicesController devicesController = SpringUtil.getBean(DevicesController.class);
	
	@Override
	public void onCtrlModelChanged(Device dev, CtrlModel ctrlModel) {
		deviceRepository.saveAndFlush(dev);
		if(null != devicesController) {
			devicesController.refresh();
		}
	}

}
