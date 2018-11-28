package com.drafire.client.handler;

import com.drafire.protocol.IMMessage;
import com.drafire.protocol.IMP;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.log4j.Logger;

import java.util.Scanner;

/**
 * 客户端api类
 */
public class ChatClientHandler extends ChannelInboundHandlerAdapter {
    private static Logger logger = Logger.getLogger(ChatClientHandler.class);
    private ChannelHandlerContext context;
    private String nickName;

    public ChatClientHandler(String nickName) {
        this.nickName = nickName;
    }

    /**
     * 连接上了服务器后回调
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.context = ctx;
        IMMessage message = new IMMessage(IMP.LOGIN.getName(), System.currentTimeMillis(), this.nickName);
        sendMessage(message);
        logger.info("成功连接服务器，并执行了登录动作");
        session();
    }

    private boolean sendMessage(IMMessage msg) {
        context.channel().writeAndFlush(msg);
        logger.info("已发送至聊天面板,请继续输入");
        return msg.getCmd().equals(IMP.LOGOUT) ? false : true;
    }

    private void session() {
        new Thread() {
            public void run() {
                logger.info(nickName + ",你好，请在控制台输入消息内容");
                IMMessage message = null;
                Scanner scanner = new Scanner(System.in);

                do {
                    if (scanner.hasNext()) {
                        String input = scanner.nextLine();
                        if ("exist".equals(input)) {
                            message = new IMMessage(IMP.LOGOUT.getName(), System.currentTimeMillis(), nickName);
                        } else {
                            message = new IMMessage(IMP.CHAT.getName(), System.currentTimeMillis(), nickName, input);
                        }
                    }
                } while (sendMessage(message));
                scanner.close();
            }
        }.start();
    }

    /**
     * 收到消息后回调
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        IMMessage message = (IMMessage) msg;
        logger.info(message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage());
        ctx.close();
    }
}
