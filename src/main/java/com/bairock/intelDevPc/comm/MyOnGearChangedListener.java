package com.bairock.intelDevPc.comm;

import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.Device.OnGearChangedListener;
import com.bairock.iot.intelDev.device.Gear;
import com.bairock.iot.intelDev.device.OrderHelper;

public class MyOnGearChangedListener implements OnGearChangedListener {

	@Override
	public void onGearChanged(Device dev, Gear gear) {
		PadClient.getIns().send(OrderHelper.getOrderMsg(OrderHelper.FEEDBACK_HEAD + dev.getLongCoding() + OrderHelper.SEPARATOR + "b" + dev.getGear()));
	}

}
