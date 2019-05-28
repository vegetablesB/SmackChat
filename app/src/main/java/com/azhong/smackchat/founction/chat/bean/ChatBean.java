package com.azhong.smackchat.founction.chat.bean;


/*
 * 项目名:    SmackChat
 * 包名       com.azhong.smackchat
 * 文件名:    ChatBean
 * 创建者:    ZSY
 * 创建时间:  2017/3/3 on 10:24
 * 描述:     TODO 聊天消息实体类
 */
public class ChatBean {

    public static final int SELF_MSG = 1;//自己的消息
    public static final int FRIENDS_MSG = 2;//对方的消息
    private String from;
    private String body;
    private String to;
    private int type;//区分自己还是好友的消息
    private String stanzaId;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getStanzaId() {
        return stanzaId;
    }

    public void setStanzaId(String stanzaId) {
        this.stanzaId = stanzaId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
