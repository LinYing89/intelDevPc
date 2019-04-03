package com.bairock.intelDevPc;

import java.io.File;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.bairock.intelDevPc.data.Config;
import com.bairock.intelDevPc.repository.DeviceImgRepo;
import com.bairock.iot.intelDev.data.DeviceImg;
import com.bairock.iot.intelDev.http.CheckDevImgVersionCode;
import com.bairock.iot.intelDev.http.HttpDownloadDeviceImgTask;

@Component
public class MyApplicationRunner implements ApplicationRunner {

	@Autowired
	private DeviceImgRepo deviceImgRepo;
	@Autowired
	private Config config;

	@Override
	public void run(ApplicationArguments args) throws Exception {
//		if (null != config.getLoginModel() && config.getLoginModel().equals(LoginModel.LOCAL)) {
//			UdpServer.getIns().run();
//
//			try {
//				DevServer.getIns().run();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//
//			DevChannelBridge.analysiserName = MyMessageAnalysiser.class.getName();
////		DevChannelBridgeHelper.getIns().setUser(user);
//			DevChannelBridgeHelper.getIns().stopSeekDeviceOnLineThread();
//			DevChannelBridgeHelper.getIns().startSeekDeviceOnLineThread();
//		}
		List<DeviceImg> list = deviceImgRepo.findAll();

		HttpDownloadDeviceImgTask.imgSavePath = System.getProperty("user.home") + "\\dafa\\devImg\\";
		checkDeviceImg(list);
	}

	public void checkDeviceImg(List<DeviceImg> listLocal) {
		CheckDevImgVersionCode check = new CheckDevImgVersionCode(config.getServerName());
		check.setOnExecutedListener(result -> {
			if (result.getCode() == 0) {
				List<DeviceImg> listRemote = result.getData();
				checkDeviceImg(listRemote, listLocal);
			}
		});
		check.start();
	}

	public void checkDeviceImg(List<DeviceImg> listRemote, List<DeviceImg> listLocal) {
		for (DeviceImg devImg : listRemote) {
			// 默认更新
			boolean update = true;
			for (DeviceImg devImgLocal : listLocal) {
				if (devImgLocal.getCode().equals(devImg.getCode())) {
					if (devImgLocal.getVersionCode() >= devImg.getVersionCode()) {
						// 如果本地版本号>=远程版本号, 检查本地图片是否存在
						File file = new File(HttpDownloadDeviceImgTask.imgSavePath + devImg.getCode() + ".png");
						// 如果本地图片存在, 不更新, 不存在则更新
						if (file.exists()) {
							update = false;
							break;
						}
					}
				}
			}
			if (update) {
				HttpDownloadDeviceImgTask task = new HttpDownloadDeviceImgTask(config.getServerName(), devImg.getCode(),
						devImg.getVersionCode());
				task.setOnExecutedListener(result -> {
					if (result.getCode() == 0) {
						DeviceImg dImg = result.getData();
						DeviceImg di = deviceImgRepo.findByCode(dImg.getCode());
						if (null == di) {
							di = new DeviceImg();
							di.setCode(dImg.getCode());
						}
						di.setVersionCode(dImg.getVersionCode());
						deviceImgRepo.saveAndFlush(di);
					}
				});
				task.start();
			}
		}
	}

}