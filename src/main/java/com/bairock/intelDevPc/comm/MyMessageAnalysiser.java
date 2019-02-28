package com.bairock.intelDevPc.comm;

import com.bairock.intelDevPc.SpringUtil;
import com.bairock.intelDevPc.controller.CtrlModelDialogController;
import com.bairock.intelDevPc.service.UserService;
import com.bairock.iot.intelDev.communication.MessageAnalysiser;
import com.bairock.iot.intelDev.device.Coordinator;
import com.bairock.iot.intelDev.device.CtrlModel;
import com.bairock.iot.intelDev.device.Device;
import com.bairock.iot.intelDev.device.DeviceAssistent;
import com.bairock.iot.intelDev.device.SetDevModelTask;

public class MyMessageAnalysiser extends MessageAnalysiser {

	private CtrlModelDialogController ctrlModelDialogController = SpringUtil.getBean(CtrlModelDialogController.class);

	@Override
	public void deviceHandleAfter(Device device, String msg) {
//		PadClient.getIns().sendIfSync("$" + msg);
		updateDevice(device);
	}

	protected void updateDevice(Device device) {
		if (null != ctrlModelDialogController && SetDevModelTask.setting
				&& ctrlModelDialogController.setDevModelThread.deviceModelHelper != null
				&& ctrlModelDialogController.setDevModelThread.deviceModelHelper.getDevToSet() == device
				&& ctrlModelDialogController.setDevModelThread.deviceModelHelper.getCtrlModel() == CtrlModel.LOCAL) {
			ctrlModelDialogController.setModelProgressValue(3);
		}
	}

	@Override
	public void unKnowDev(Device device, String msg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unKnowMsg(String msg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void allMessageEnd() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean singleMessageStart(String msg) {
		if (msg.startsWith("!")) {
			if (msg.contains("#")) {
				msg = msg.substring(0, msg.indexOf("#"));
			}
			String[] codings = msg.split(":");
			if (codings.length < 2) {
				return false;
			}
			// Device device = DeviceAssistent.createDeviceByCoding(codings[1]);
			Device device = UserService.getDevGroup().findDeviceWithCoding(codings[1]);
			if (null == device || !(device instanceof Coordinator)) {
				return false;
			}

			Coordinator coordinator = (Coordinator) device;
			if (!coordinator.isConfigingChildDevice()) {
				return false;
			}
			for (int i = 2; i < codings.length; i++) {
				String coding = codings[i];
				Device device1 = coordinator.findDevByCoding(coding);
				if (null == device1) {
					device1 = DeviceAssistent.createDeviceByCoding(coding);
					if (device1 != null) {
						UserService.getDevGroup().createDefaultDeviceName(device1);
						coordinator.addChildDev(device1);
//                        DeviceDao deviceDao = DeviceDao.get(HamaApp.HAMA_CONTEXT);
//                        deviceDao.add(device1);
					}
				}
			}
//            if(null != SearchActivity.handler){
//                SearchActivity.handler.obtainMessage(SearchActivity.handler.DEV_ADD_CHILD).sendToTarget();
//            }
			return false;
		}
		return true;
	}

	@Override
	public void singleMessageEnd(Device device, String msg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void configDevice(Device device, String msg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deviceHandleBefore(Device device, String msg) {
		device.setCtrlModel(CtrlModel.LOCAL);
	}

}
