package com.bairock.intelDevPc.comm;

import com.bairock.intelDevPc.SpringUtil;
import com.bairock.intelDevPc.repository.DeviceRepository;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.Device.OnSortIndexChangedListener;

public class MyOnSortIndexChangedListener implements OnSortIndexChangedListener {

	private DeviceRepository deviceRepository = SpringUtil.getBean(DeviceRepository.class);
	
	@Override
	public void onSortIndexChanged(Device dev, int sortIndex) {
		deviceRepository.saveAndFlush(dev);
	}

}
