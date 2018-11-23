package com.drafire.server.handler;

import com.drafire.processor.MsgProcessor;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.apache.log4j.Logger;

/**
 * 处理WebSocket的请求
 */
public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private static final Logger logger = Logger.getLogger(WebSocketHandler.class);

    private MsgProcessor processor = new MsgProcessor();

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {
        processor.sendMsg(channelHandlerContext.channel(), textWebSocketFrame.text());
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        processor.sendMsg(channel, "WebSocket Client:" + processor.getAddress(channel) + "加入");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        processor.sendMsg(channel, "WebSocket Client:" + processor.getAddress(channel) + "离开");
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        processor.sendMsg(channel, processor.getNickName(channel) + "离线");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        processor.sendMsg(channel, processor.getNickName(channel) + "上线");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Channel client = ctx.channel();
        String addr = processor.getAddress(client);

        logger.info("WebSocket Client:" + addr + "异常");
        cause.printStackTrace();
        //关闭channel
        ctx.close();
    }
}
