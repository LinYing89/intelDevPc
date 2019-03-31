package com.bairock.intelDevPc.comm;

import java.util.Date;

import com.bairock.intelDevPc.IntelDevPcApplication;
import com.bairock.intelDevPc.SpringUtil;
import com.bairock.intelDevPc.Util;
import com.bairock.intelDevPc.data.Config;
import com.bairock.intelDevPc.data.DeviceValueHistory;
import com.bairock.intelDevPc.service.DeviceHistoryService;
import com.bairock.iot.intelDev.communication.RefreshCollectorValueHelper;
import com.bairock.iot.intelDev.device.CtrlModel;
import com.bairock.iot.intelDev.device.DevStateHelper;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.Device.OnStateChangedListener;
import com.bairock.iot.intelDev.device.IStateDev;
import com.bairock.iot.intelDev.device.devcollect.DevCollect;
import com.bairock.iot.intelDev.device.devcollect.DevCollectClimateContainer;
import com.bairock.iot.intelDev.order.DeviceOrder;
import com.bairock.iot.intelDev.order.LoginModel;
import com.bairock.iot.intelDev.order.OrderType;

public class MyOnStateChangedListener implements OnStateChangedListener {

//	private MainController mainController = SpringUtil.getBean(MainController.class);
	private DeviceHistoryService deviceHistoryService = SpringUtil.getBean(DeviceHistoryService.class);
	private Config config = SpringUtil.getBean(Config.class);

	@Override
	public void onStateChanged(Device dev, String stateId) {
		if (stateId.equals(DevStateHelper.DS_UNKNOW)) {
			return;
		}
		// 本地设备才往服务器发送状态，远程设备只接收服务器状态
		if (config.getLoginModel().equals(LoginModel.LOCAL) && dev.findSuperParent().getCtrlModel() == CtrlModel.LOCAL) {
			DeviceOrder devOrder = new DeviceOrder(OrderType.STATE, dev.getId(), dev.getLongCoding(), stateId);
			String strOrder = Util.orderBaseToString(devOrder);
			PadClient.getIns().send(strOrder);
		}
		float value = 0;
		boolean abnormal = false;
		if (stateId.equals(DevStateHelper.DS_YI_CHANG)) {
			abnormal = true;
			if (dev instanceof IStateDev) {
				try {
					value = DevStateHelper.getStateCode(dev.getDevStateId());
				} catch (Exception e) {
				}
			} else if (dev instanceof DevCollect) {
				value = ((DevCollect) dev).getCollectProperty().getCurrentValue();
			}
		} else if (stateId.equals(DevStateHelper.DS_KAI)) {
			value = 1;
		} else if (stateId.equals(DevStateHelper.DS_GUAN)) {
			value = 0;
		}else if(stateId.equals(DevStateHelper.DS_ZHENG_CHANG)) {
			if (dev instanceof DevCollect) {
				Float fv = ((DevCollect) dev).getCollectProperty().getCurrentValue();
				if(fv != null) {
					value = fv;
				}
			}
		}

		DeviceValueHistory history = new DeviceValueHistory();
		history.setHistoryTime(new Date());
		history.setDeviceId(dev.getId());
		history.setDeviceName(dev.getName());
		history.setLongCoding(dev.getLongCoding());
		history.setAbnormal(abnormal);
		history.setValue(value);
		deviceHistoryService.insert(history);
	}

	@Override
	public void onNormalToAbnormal(Device dev) {
		IntelDevPcApplication.addOfflineDevCoding(dev);
		if (dev instanceof DevCollectClimateContainer) {
			RefreshCollectorValueHelper.getIns().endRefresh(dev);
		}
	}

	@Override
	public void onAbnormalToNormal(Device dev) {
		IntelDevPcApplication.removeOfflineDevCoding(dev);

		if (dev instanceof DevCollectClimateContainer) {
			RefreshCollectorValueHelper.getIns().RefreshDev(dev);
		}
	}

	@Override
	public void onNoResponse(Device dev) {
		// TODO Auto-generated method stub

	}

}
