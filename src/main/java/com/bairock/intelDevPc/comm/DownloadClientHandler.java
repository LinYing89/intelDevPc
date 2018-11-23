package com.bairock.intelDevPc.comm;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.bairock.intelDevPc.SpringUtil;
import com.bairock.intelDevPc.controller.MainController;
import com.bairock.intelDevPc.controller.UpDownloadDialogController;
import com.bairock.intelDevPc.service.UserService;
import com.bairock.iot.intelDev.communication.DevChannelBridgeHelper;
import com.bairock.iot.intelDev.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

public class DownloadClientHandler extends ChannelInboundHandlerAdapter {

	private StringBuilder stringBuilder;

	private UserService userService = SpringUtil.getBean(UserService.class);
	private MainController mainController = SpringUtil.getBean(MainController.class);
	private UpDownloadDialogController upDownloadDialogController = SpringUtil
			.getBean(UpDownloadDialogController.class);

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		Channel channel = ctx.channel();
		stringBuilder = new StringBuilder();
		String str = "download:" + UserService.user.getName() + ":"
				+ UserService.user.getListDevGroup().get(0).getName();
		channel.writeAndFlush(Unpooled.copiedBuffer(str.getBytes("GBK")));
		super.channelActive(ctx);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		ByteBuf m = (ByteBuf) msg;
		try {
			byte[] req = new byte[m.readableBytes()];
			m.readBytes(req);
			// String str = new String(req, "GBK");
			stringBuilder.append(new String(req, "GBK"));
			String strMsg = stringBuilder.toString();
			if (strMsg.endsWith("OK")) {
				// refreshDbUser(user);
				strMsg = strMsg.substring(0, strMsg.length() - 2);
				// stringBuilder.append(str);
				ctx.close();
				User user = getUserFromJson(strMsg);
				if (null != user) {
					userService.cleanUser();
					userService.addUser(user);
					DevChannelBridgeHelper.getIns().stopSeekDeviceOnLineThread();
					userService.initUser();
					mainController.reInit();

					upDownloadDialogController.downLoadResult(true);
				}else {
					upDownloadDialogController.downLoadResult(false);
				}
			}

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			upDownloadDialogController.downLoadResult(false);
		} finally {
			m.release();
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		super.userEventTriggered(ctx, evt);
		if (evt instanceof IdleStateEvent) { // 2
			// IdleStateEvent event = (IdleStateEvent) evt;
			upDownloadDialogController.downLoadResult(false);
			ctx.close();
		}
	}

	private User getUserFromJson(String json) {
		ObjectMapper mapper = new ObjectMapper();
		User user = null;
		try {
			user = mapper.readValue(json, User.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return user;
	}

}
