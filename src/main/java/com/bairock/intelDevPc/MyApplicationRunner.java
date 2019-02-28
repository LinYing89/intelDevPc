package com.bairock.intelDevPc;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.bairock.intelDevPc.comm.MyMessageAnalysiser;
import com.bairock.iot.intelDev.communication.DevChannelBridge;
import com.bairock.iot.intelDev.communication.DevChannelBridgeHelper;
import com.bairock.iot.intelDev.communication.DevServer;
import com.bairock.iot.intelDev.communication.UdpServer;

@Component
public class MyApplicationRunner implements ApplicationRunner {

	@Override
	public void run(ApplicationArguments args) throws Exception {
		UdpServer.getIns().run();

		try {
			DevServer devServer = new DevServer();
			devServer.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		DevChannelBridge.analysiserName = MyMessageAnalysiser.class.getName();
//		DevChannelBridgeHelper.getIns().setUser(user);
		DevChannelBridgeHelper.getIns().stopSeekDeviceOnLineThread();
		DevChannelBridgeHelper.getIns().startSeekDeviceOnLineThread();
	}

}