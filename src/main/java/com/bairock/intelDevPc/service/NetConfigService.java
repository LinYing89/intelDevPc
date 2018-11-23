package com.bairock.intelDevPc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bairock.intelDevPc.comm.MyMessageAnalysiser;
import com.bairock.iot.intelDev.communication.DevChannelBridge;
import com.bairock.iot.intelDev.communication.DevChannelBridgeHelper;
import com.bairock.iot.intelDev.communication.DevServer;
import com.bairock.iot.intelDev.communication.UdpServer;

@Service
public class NetConfigService {

	@Autowired
	public NetConfigService() {
//		UdpServer.getIns().setUser(user);
		UdpServer.getIns().run();
		
		try {
			DevServer devServer = new DevServer();
			devServer.run();
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		DevChannelBridge.analysiserName = MyMessageAnalysiser.class.getName();
//		DevChannelBridgeHelper.getIns().setUser(user);
		DevChannelBridgeHelper.getIns().stopSeekDeviceOnLineThread();
		DevChannelBridgeHelper.getIns().startSeekDeviceOnLineThread();
	}
}
