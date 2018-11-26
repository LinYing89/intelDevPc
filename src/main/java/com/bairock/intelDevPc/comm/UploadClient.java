package com.bairock.intelDevPc.comm;

import java.util.concurrent.TimeUnit;

import com.bairock.intelDevPc.SpringUtil;
import com.bairock.intelDevPc.controller.UpDownloadDialogController;
import com.bairock.intelDevPc.data.Config;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

public class UploadClient {
	
	private UpDownloadDialogController upDownloadDialogController = SpringUtil.getBean(UpDownloadDialogController.class);
	private Config config = SpringUtil.getBean(Config.class);
	
	private EventLoopGroup workerGroup;

    private  ChannelFuture channelFuture;

    public void link(){

        try {
            workerGroup = new NioEventLoopGroup();
            Bootstrap b = new Bootstrap(); // (1)
            b.group(workerGroup); // (2)
            b.channel(NioSocketChannel.class); // (3)
            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) {
                    ch.pipeline().addLast(new IdleStateHandler(10, 8,15, TimeUnit.SECONDS)); // 1
                    ch.pipeline().addLast(new UploadClientHandler());
                }
            });

            // Start the client.
            channelFuture = b.connect(config.getServerName(), config.getUpDownloadPort()).addListener((ChannelFutureListener) future -> {
                if(!future.isSuccess()){
                	upDownloadDialogController.loadResult(false);
                }
            });

            // Wait until the connection is closed.
            channelFuture.channel().closeFuture();
        }catch (Exception e){
            e.printStackTrace();
            upDownloadDialogController.loadResult(false);
        } finally {
            //workerGroup.shutdownGracefully();
        }
    }

    public void send(String msg){
        if(null != channelFuture) {
            channelFuture.channel().writeAndFlush(Unpooled.copiedBuffer(msg.getBytes()));
        }
    }
    public void send(byte[] msg){
        if(null != channelFuture) {
            channelFuture.channel().writeAndFlush(Unpooled.copiedBuffer(msg));
        }
    }

    public void myClose(){
        if(null != workerGroup) {
            workerGroup.shutdownGracefully();
        }
    }
}
