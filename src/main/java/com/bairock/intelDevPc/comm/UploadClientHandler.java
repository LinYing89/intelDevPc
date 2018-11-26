package com.bairock.intelDevPc.comm;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;

import com.bairock.intelDevPc.SpringUtil;
import com.bairock.intelDevPc.controller.UpDownloadDialogController;
import com.bairock.intelDevPc.service.UserService;
import com.bairock.iot.intelDev.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

public class UploadClientHandler extends ChannelInboundHandlerAdapter{
	
	private UpDownloadDialogController upDownloadDialogController = SpringUtil
			.getBean(UpDownloadDialogController.class);
	
	@Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        channel.writeAndFlush(Unpooled.copiedBuffer("upload".getBytes("GBK")));
        TimeUnit.MILLISECONDS.sleep(500);
        String userJson = getUserJson();
        if(null != userJson){
            userJson = userJson + "#";
            ctx.writeAndFlush(Unpooled.copiedBuffer(userJson.getBytes("GBK")));
        }
        //ctx.close();
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf m = (ByteBuf)msg;
        try {
            byte[] req = new byte[m.readableBytes()];
            m.readBytes(req);
            String str = new String(req, "GBK");
            if(str.equals("OK")){
                //refreshDbUser(user);
                ctx.close();
                upDownloadDialogController.loadResult(true);
            }else{
                ctx.close();
                upDownloadDialogController.loadResult(false);
            }
        }catch(UnsupportedEncodingException e){
            e.printStackTrace();
        }finally{
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
        if (evt instanceof IdleStateEvent) {  // 2
        	upDownloadDialogController.loadResult(false);
            ctx.close();
        }
    }

    private String getUserJson(){
        String json = null;
        User user = UserService.user;
        if(null != user){
            ObjectMapper mapper = new ObjectMapper();
            try {
                json = mapper.writeValueAsString(user);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return json;
    }
}
