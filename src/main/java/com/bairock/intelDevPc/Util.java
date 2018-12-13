package com.bairock.intelDevPc;

import com.bairock.iot.intelDev.device.CtrlModel;

public class Util {

	public static String getCtrlModelName(CtrlModel ctrlModel) {
		if(ctrlModel == CtrlModel.REMOTE) {
			return "远程";
		}else {
			return "本地";
		}
	}
}
