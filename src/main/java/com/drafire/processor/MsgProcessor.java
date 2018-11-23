package com.drafire.processor;

import com.alibaba.fastjson.JSONObject;
import com.drafire.protocol.IMDecoder;
import com.drafire.protocol.IMEncoder;
import com.drafire.protocol.IMMessage;
import com.drafire.protocol.IMP;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;


/**
 * 自定义协议api类
 */
public class MsgProcessor {
    //记录在线用户
    private static ChannelGroup onlineUsers = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    //定义一些扩展的属性
    private final AttributeKey<String> NICK_NAME = AttributeKey.valueOf("nickName");
    private final AttributeKey<String> IP_ADDR = AttributeKey.valueOf("ipAddr");
    private final AttributeKey<JSONObject> ATTRS = AttributeKey.valueOf("attrs");

    //自定义解码器
    private IMDecoder decoder = new IMDecoder();
    //自定义编码器
    private IMEncoder encoder = new IMEncoder();

    //获取用户昵称
    public String getNickName(Channel client) {
        return client.attr(NICK_NAME).get();
    }

    //获取用户的ip地址
    public String getAddress(Channel client) {
        return client.remoteAddress().toString().replaceFirst("/", "");
    }

    //获取扩展的属性
    public JSONObject getAttrs(Channel client) {
        return client.attr(ATTRS).get();
    }

    //设置扩展属性
    private void setAtrrs(Channel client, String key, Object value) {
        JSONObject json = null;
        try {
            json = client.attr(ATTRS).get();
        } catch (Exception e) {
            json = new JSONObject();
        }
        json.put(key, value);
        client.attr(ATTRS).set(json);
    }

    public void logout(Channel client) {
        if (null == getNickName(client)) {
            return;
        }

        //有人离开后，则通知所有人
        for (Channel channel : onlineUsers) {
            IMMessage request = new IMMessage(IMP.LOGOUT.getName(), getSysTime(), onlineUsers.size(), getNickName(client) + "离开 ");
            String content = encoder.encode(request);
            channel.writeAndFlush(content);   //发完自动清理
        }
        onlineUsers.remove(client);
    }

    public void sendMsg(Channel client, IMMessage message) {
        sendMsg(client, message.getContent());
    }

    //发送消息
    public void sendMsg(Channel client, String msg) {
        IMMessage message = decoder.decode(msg);
        if (null == message) {
            return;
        }

        String addr = getAddress(client);
        switch (IMP.getIMP(message.getCmd())) {
            case LOGIN:
                client.attr(NICK_NAME).set(message.getSender());
                client.attr(IP_ADDR).set(message.getAddr());
                onlineUsers.add(client);

                for (Channel channel : onlineUsers) {
                    if (channel != client) {
                        message = new IMMessage(IMP.LOGIN.getName(), getSysTime(), onlineUsers.size(), client.attr(NICK_NAME).get() + "登录");
                    } else {
                        message = new IMMessage(IMP.LOGIN.getName(), getSysTime(), onlineUsers.size(), "已和服务器建立链接");
                    }
                    String content = encoder.encode(message);
                    channel.writeAndFlush(content);
                }
                break;
            case CHAT:
                for (Channel channel : onlineUsers) {
                    if (channel == client) {
                        message.setSender("我");
                    } else {
                        message.setSender(client.attr(NICK_NAME).get());
                    }
                    message.setTime(getSysTime());
                    String content = encoder.encode(message);
                    channel.writeAndFlush(new TextWebSocketFrame(content));   //发送一个socket，给网页的websocket接收
                }
                break;
            case FLOWER:
                JSONObject attrs = getAttrs(client);
                //记录当前时间，用于针对恶意刷花
                long currentTime = getSysTime();
                if (null != attrs) {
                    long lastTime = attrs.getLong("lastFlowerTime");
                    int seconds = 10;
                    long sub = currentTime - lastTime;
                    if (sub < 1000 * seconds) {
                        message.setSender("you");
                        message.setCmd(IMP.SYSTEM.getName());
                        message.setContent("您刷鲜花太频繁，请" + (seconds - Math.round(sub / 1000)) + "秒后再试");
                        String content = encoder.encode(message);
                        client.writeAndFlush(content);
                    }
                }

                //正产刷鲜花
                for (Channel channel : onlineUsers) {
                    if (channel == client) {
                        message.setSender("you");
                        message.setContent("您给大家刷了一波鲜花 ");
                        setAtrrs(client, "lastFlowerTime", getSysTime());
                    } else {
                        message.setSender(getNickName(client));
                        message.setContent(getNickName(client) + "给大家刷了一波鲜花");
                    }
                    message.setTime(getSysTime());

                    channel.writeAndFlush(new TextWebSocketFrame(encoder.encode(message)));
                }
                break;
        }
    }

    /**
     * 获取系统时间
     *
     * @return
     */
    private long getSysTime() {
        return System.currentTimeMillis();
    }
}
