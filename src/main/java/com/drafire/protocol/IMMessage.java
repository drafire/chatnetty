package com.drafire.protocol;

import org.msgpack.annotation.Message;

/**
 * 自定义消息实体
 * 加上@Message，用于msgpack序列化
 */
@Message
public class IMMessage {
    private String addr;        //ip地址+端口
    private String cmd;         //命令
    private long time;         //系统时间戳
    private int online;        //在线人数
    private String sender;     //发送者
    private String receiver;   //接收人
    private String content;    //内容

    public IMMessage() {
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getOnline() {
        return online;
    }

    public void setOnline(int online) {
        this.online = online;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
