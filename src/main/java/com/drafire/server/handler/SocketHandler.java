package com.drafire.server.handler;

import com.drafire.protocol.IMMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;

public class SocketHandler extends SimpleChannelInboundHandler<IMMessage> {
    private static Logger logger= Logger.getLogger(SocketHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, IMMessage message) throws Exception {

    }
}
