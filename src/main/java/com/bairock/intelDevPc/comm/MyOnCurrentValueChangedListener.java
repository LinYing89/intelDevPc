package com.bairock.intelDevPc.comm;

import com.bairock.iot.intelDev.device.devcollect.CollectProperty.OnCurrentValueChangedListener;
import com.bairock.iot.intelDev.order.DeviceOrder;
import com.bairock.iot.intelDev.order.OrderType;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.bairock.intelDevPc.SpringUtil;
import com.bairock.intelDevPc.Util;
import com.bairock.intelDevPc.controller.MainController;
import com.bairock.intelDevPc.data.DeviceValueHistory;
import com.bairock.intelDevPc.repository.DeviceValueHistoryRepo;
import com.bairock.iot.intelDev.device.CtrlModel;
import com.bairock.iot.intelDev.device.devcollect.DevCollect;

public class MyOnCurrentValueChangedListener implements OnCurrentValueChangedListener {

	private MainController mainController = SpringUtil.getBean(MainController.class);
	private DeviceValueHistoryRepo deviceValueHistoryRepo = SpringUtil.getBean(DeviceValueHistoryRepo.class);

	@Override
	public void onCurrentValueChanged(DevCollect dev, Float value) {

		// 本地设备才往服务器发送状态，远程设备只接收服务器状态
		if (dev.findSuperParent().getCtrlModel() == CtrlModel.LOCAL) {
			DeviceOrder devOrder = new DeviceOrder(OrderType.VALUE, dev.getId(), dev.getLongCoding(), String.valueOf(value));
			String strOrder = Util.orderBaseToString(devOrder);
			PadClient.getIns().send(strOrder);
		}

		Date date = new Date();
		if (MainController.selectedValueDev == dev) {
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
