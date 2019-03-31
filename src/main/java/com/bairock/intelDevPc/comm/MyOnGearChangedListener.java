package com.bairock.intelDevPc.comm;

import com.bairock.intelDevPc.SpringUtil;
import com.bairock.intelDevPc.repository.DeviceRepository;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.Device.OnGearChangedListener;
import com.bairock.iot.intelDev.device.Gear;

public class MyOnGearChangedListener implements OnGearChangedListener {

//	private Config config = SpringUtil.getBean(Config.class);
	
	private DeviceRepository deviceRepo = SpringUtil.getBean(DeviceRepository.class);
	
	@Override
	public void onGearChanged(Device dev, Gear gear, boolean touchDev) {
		deviceRepo.saveAndFlush(dev);
		//设备异常不要发送档位了, 否则可能多个终端登录时, 造成状态不匹配终端之间循环发送
//		if(config.getLoginModel().equals(LoginModel.LOCAL)){
//			
//		}
//		
//		if(!dev.isNormal()) {
//			return;
//		}
//		//发往服务器, 服务器只有收到本地档位报文才会改变档位, 不回自动改变
//		DeviceOrder ob = new DeviceOrder(OrderType.GEAR, dev.getId(), dev.getLongCoding(), gear.toString());
//		String order = Util.orderBaseToString(ob);
//		PadClient.getIns().send(order);
	}

}
