package com.drafire.client;

import com.drafire.client.handler.ChatClientHandler;
import com.drafire.protocol.IMDecoder;
import com.drafire.protocol.IMEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 客户端
 */
public class ChatClient {
    private ChatClientHandler handler;
    private String host;
    private int port;

    public ChatClient(String nickName) {
        this.handler = new ChatClientHandler(nickName);
    }

    public void connect(String host,int port) {
        this.host=host;
        this.port=port;

        EventLoopGroup workerGroup=new NioEventLoopGroup();

        Bootstrap b=new Bootstrap();
        try {
            b.group(workerGroup).channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new IMDecoder());
                            socketChannel.pipeline().addLast(new IMEncoder());
                            socketChannel.pipeline().addLast(handler);
                        }
                    });

            ChannelFuture f=b.connect(this.host,this.port).sync();
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new ChatClient("drafire").connect("127.0.0.1",8083);
    }
}
