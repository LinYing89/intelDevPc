package com.bairock.intelDevPc.comm;

import com.bairock.intelDevPc.Util;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.Device.OnGearChangedListener;
import com.bairock.iot.intelDev.device.Gear;
import com.bairock.iot.intelDev.order.DeviceOrder;
import com.bairock.iot.intelDev.order.OrderType;

public class MyOnGearChangedListener implements OnGearChangedListener {

	@Override
	public void onGearChanged(Device dev, Gear gear) {
		//发往服务器, 服务器只有收到本地档位报文才会改变档位, 不回自动改变
		DeviceOrder ob = new DeviceOrder(OrderType.GEAR, dev.getId(), dev.getLongCoding(), gear.toString());
		String order = Util.orderBaseToString(ob);
		PadClient.getIns().send(order);
	}

}
