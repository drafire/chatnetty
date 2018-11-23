package com.drafire.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.msgpack.MessagePack;

/**
 * 自定义IM协议的编码器
 */
public class IMEncoder extends MessageToByteEncoder<IMMessage> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, IMMessage imMessage, ByteBuf byteBuf) throws Exception {
        byteBuf.writeBytes(new MessagePack().write(imMessage));
    }

    public String encode(IMMessage msg) {
        if (null == msg) {
            return "";
        }
        String prex = "[" + msg.getCmd() + "]" + "[" + msg.getTime() + "]";  //前缀
        //拼接消息体
        if (IMP.isSystem(msg.getCmd())) {   //如果是系统命令
            prex += "[" + msg.getSender() + "]";
        } else {
            prex += "[" + msg.getSender() + "]";
        }

        if (null != msg.getContent() && "" != msg.getContent()) {
            prex += "-" + msg.getContent();
        }
        return prex;
    }
}
