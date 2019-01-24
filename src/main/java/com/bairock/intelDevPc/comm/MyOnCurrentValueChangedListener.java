package com.bairock.intelDevPc.comm;

import com.bairock.iot.intelDev.device.devcollect.CollectProperty.OnCurrentValueChangedListener;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.bairock.intelDevPc.SpringUtil;
import com.bairock.intelDevPc.controller.MainController;
import com.bairock.intelDevPc.data.DeviceValueHistory;
import com.bairock.intelDevPc.repository.DeviceValueHistoryRepo;
import com.bairock.iot.intelDev.device.devcollect.DevCollect;

public class MyOnCurrentValueChangedListener implements OnCurrentValueChangedListener {

	private MainController mainController = SpringUtil.getBean(MainController.class);
	private DeviceValueHistoryRepo deviceValueHistoryRepo = SpringUtil.getBean(DeviceValueHistoryRepo.class);
	
	@Override
	public void onCurrentValueChanged(DevCollect dev, Float value) {
		
		Date date = new Date();
		if(MainController.selectedValueDev == dev) {
			SimpleDateFormat sf = new SimpleDateFormat("HH:mm:ss");
			mainController.addValueHistoryToChart(sf.format(date), value);
		}
		
		DeviceValueHistory history = new DeviceValueHistory();
		history.setHistoryTime(date);
		history.setDeviceId(dev.getId());
		history.setValue(value);
		deviceValueHistoryRepo.saveAndFlush(history);
	}

}
