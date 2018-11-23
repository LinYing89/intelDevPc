package com.bairock.intelDevPc.comm;

import java.util.concurrent.TimeUnit;

import com.bairock.intelDevPc.SpringUtil;
import com.bairock.intelDevPc.controller.MainController;
import com.bairock.intelDevPc.data.Config;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

public class PadClient {
	private static PadClient ins = new PadClient();

    private PadClientHandler padClientHandler;
    
    private Config config = SpringUtil.getBean(Config.class);
    private MainController mainController = SpringUtil.getBean(MainController.class);
    
    private Bootstrap b;

    private PadClient(){
        init();
    }

    public static PadClient getIns(){
        return ins;
    }

    private void init(){
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        b = new Bootstrap(); // (1)
        b.group(workerGroup); // (2)
        b.channel(NioSocketChannel.class); // (3)
        b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
        b.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new IdleStateHandler(-1, 20,20, TimeUnit.SECONDS)); // 1
                ch.pipeline().addLast(new PadClientHandler());
            }
        });
    }

    void setPadClientHandler(PadClientHandler padClientHandler){
        if(this.padClientHandler != null){
            this.padClientHandler.channel.close();
            this.padClientHandler = null;
        }
        this.padClientHandler = padClientHandler;
    }

    public boolean isLinked(){
        return padClientHandler != null;
    }

    public void link(){
        try {
            // Start the client.
            ChannelFuture channelFuture = b.connect(config.getServerName(), config.getPadPort()); // (5)
            // Wait until the connection is closed.
            channelFuture.channel().closeFuture();
        }catch (Exception e){
            e.printStackTrace();
            padClientHandler = null;
            if(null != mainController) {
            	mainController.refreshServerState(false);
            }
        }
    }

    public void closeHandler(){
        if(null != padClientHandler)
        padClientHandler.channel.close();
        padClientHandler = null;
    }

    public void send(String msg){
        if(null != padClientHandler){
            padClientHandler.send(msg);
        }
    }

    void sendIfSync(String msg){
        if(null != padClientHandler && padClientHandler.syncDevMsg){
            padClientHandler.send(msg);
        }
    }
}
