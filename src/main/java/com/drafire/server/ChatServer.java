package com.drafire.server;

import com.drafire.server.handler.WebSocketHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.apache.log4j.Logger;

/**
 * 服务端
 */
public class ChatServer {
    private final static Logger logger = Logger.getLogger(ChatServer.class);

    private final int port = 9010;

    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            // 绑定boss组（用于接收request）和work组（用于处理request）
            b.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)    // 开通一个NioServerSocketChannel
                    .option(ChannelOption.SO_BACKLOG, 128)   //
                    .childHandler(new ChannelInitializer<SocketChannel>() {  //声明子hanndler，用于处理各种情况
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline= socketChannel.pipeline();  //在channel下声明一个管道

                            //解析WebSocket请求
                            pipeline.addLast(new WebSocketServerProtocolHandler("/im"));  //如果是im下的websocket请求，则使用WebSocket的协议处理
                            pipeline.addLast(new WebSocketHandler());
                        }
                    });

            ChannelFuture f = b.bind(port).sync();
            logger.info("正在监听：" + port + "端口");
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new ChatServer().start();
    }
}
