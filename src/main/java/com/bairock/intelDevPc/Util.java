package com.bairock.intelDevPc;

import com.bairock.iot.intelDev.device.CtrlModel;

public class Util {
	
	/**
	 * 是否是用户信息登录, 否则为本地登录
	 */
	public static boolean USER_ADMIN;

	public static String getCtrlModelName(CtrlModel ctrlModel) {
		if(ctrlModel == CtrlModel.REMOTE) {
			return "远程";
		}else {
			return "本地";
		}
	}
}
