package com.bairock.intelDevPc;

import com.bairock.iot.intelDev.device.CtrlModel;
import com.bairock.iot.intelDev.order.OrderBase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Util {
	
	/**
	 * 游客访问
	 */
	public static boolean GUEST;
	/**
	 * 是否可以连接服务器, 没进入主界面之前或本地登录不可连接服务器
	 */
	public static boolean CAN_CONNECT_SERVER = false;

	public static String getCtrlModelName(CtrlModel ctrlModel) {
		if(ctrlModel == CtrlModel.REMOTE) {
			return "远程";
		}else {
			return "本地";
		}
	}
	
	public static String orderBaseToString(OrderBase ob) {
		ObjectMapper om = new ObjectMapper();
		String order = "";
		try {
			order = om.writeValueAsString(ob);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return order;
	}
}
