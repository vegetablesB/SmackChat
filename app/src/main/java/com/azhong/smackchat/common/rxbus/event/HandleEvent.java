package com.azhong.smackchat.common.rxbus.event;

/*
 * 项目名:    SmackChat
 * 包名       com.azhong.smackchat.common.rxbus.event
 * 文件名:    HandleEvent
 * 创建者:    ZJB
 * 创建时间:  2017/3/6 on 11:44
 * 描述:     TODO 操作类消息
 */
public class HandleEvent {
    String receiveClass;
    Object message;
    String time;
    String type;

    public HandleEvent(String receiveClass, String message) {
        this.receiveClass = receiveClass;
        this.message = message;
    }

    public HandleEvent(String receiveClass, boolean message) {
        this.receiveClass = receiveClass;
        this.message = message;
    }

    public HandleEvent(String message) {
        this.message = message;
    }

    public String getReceiveClass() {
        return receiveClass;
    }

    public void setReceiveClass(String receiveClass) {
        this.receiveClass = receiveClass;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
