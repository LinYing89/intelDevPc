package com.bairock.intelDevPc.comm;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.bairock.intelDevPc.SpringUtil;
import com.bairock.intelDevPc.Util;
import com.bairock.intelDevPc.controller.MainController;
import com.bairock.intelDevPc.data.Config;
import com.bairock.intelDevPc.data.DeviceValueHistory;
import com.bairock.intelDevPc.service.DeviceHistoryService;
import com.bairock.iot.intelDev.device.CtrlModel;
import com.bairock.iot.intelDev.device.devcollect.CollectProperty.OnCurrentValueChangedListener;
import com.bairock.iot.intelDev.device.devcollect.DevCollect;
import com.bairock.iot.intelDev.order.DeviceOrder;
import com.bairock.iot.intelDev.order.LoginModel;
import com.bairock.iot.intelDev.order.OrderType;

public class MyOnCurrentValueChangedListener implements OnCurrentValueChangedListener {

	private MainController mainController = SpringUtil.getBean(MainController.class);
	//private DeviceValueHistoryRepo deviceValueHistoryRepo = SpringUtil.getBean(DeviceValueHistoryRepo.class);
	private DeviceHistoryService deviceHistoryService = SpringUtil.getBean(DeviceHistoryService.class);
	private Config config = SpringUtil.getBean(Config.class);

	@Override
	public void onCurrentValueChanged(DevCollect dev, Float value) {

		// 客户端为本地登录,本地设备才往服务器发送状态，远程设备只接收服务器状态
		if (config.getLoginModel().equals(LoginModel.LOCAL) && dev.findSuperParent().getCtrlModel() == CtrlModel.LOCAL) {
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
		history.setDeviceName(dev.getName());
		history.setLongCoding(dev.getLongCoding());
		history.setValue(value);
		deviceHistoryService.insert(history);
//		deviceValueHistoryRepo.saveAndFlush(history);
	}

}
