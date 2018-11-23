package com.drafire.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.msgpack.MessagePack;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 自定义IM协议的解码器
 */
public class IMDecoder extends ByteToMessageDecoder {

    //解析IM写一下请求内容的正则
    private Pattern pattern = Pattern.compile("^\\[(.*)\\](\\s\\-\\s(.*))?");

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        //这里这么折腾，是因为需要处理空串或者非IMP消息
        try {
            final int length = byteBuf.readableBytes();
            final byte[] array = new byte[length];
            String content = new String(array, byteBuf.readerIndex(), length);

            //空消息不解析
            if (null == content || "".equalsIgnoreCase(content)) {
                return;
            }

            //非IMP消息不解析
            if (!IMP.isIMP(content)) {
                channelHandlerContext.channel().pipeline().remove(content);
                return;
            }

            //为什么不这样写呢？
            ByteBuf byteBuf2 = byteBuf.getBytes(byteBuf.readerIndex(), array, 0, length);
            list.add(new MessagePack().read(array, IMMessage.class));
            byteBuf.clear();
        } catch (IOException e) {
            channelHandlerContext.channel().pipeline().remove(this);
        }
    }

    //解码
    public IMMessage decode(String msg) {
        if (null == msg || "".equals(msg)) {
            return null;
        }

        String header = "";   //消息头
        String content = "";  //消息体
        Matcher c = pattern.matcher(content);
        if (c.matches()) {
            header = c.group(1);
            content = c.group(3);
        }

        String[] headers = header.split("\\]\\[");
        long time = 0;
        try {
            time = Long.parseLong(headers[1]);
        } catch (Exception e) {
        }

        String nickName = headers[2];
        nickName = nickName.length() < 10 ? nickName : nickName.substring(0, 9);

        IMMessage message = null;
        if (msg.startsWith("[" + IMP.LOGIN.getName() + "]")) {
            message = new IMMessage(headers[0], time, nickName);
        } else if (msg.startsWith("[" + IMP.CHAT.getName() + "]")) {
            message = new IMMessage(headers[0], time, nickName, content);
        } else if (msg.startsWith("[" + IMP.FLOWER.getName() + "]")) {
            message = new IMMessage(headers[0], time, nickName);
        }
        return message;
    }
}
