package com.azhong.smackchat.common.rxbus.event;

/*
 * 项目名:    SmackChat
 * 包名       com.azhong.smackchat.common.rxbus.event
 * 文件名:    FriendListenerEvent
 * 创建者:    YHF
 * 创建时间:  2017/3/6 on 17:23
 * 描述:     TODO
 */
public class FriendListenerEvent {
    public String getRequestName() {
        return requestName;
    }

    public void setRequestName(String requestName) {
        this.requestName = requestName;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getReciverClass() {
        return reciverClass;
    }

    public void setReciverClass(String reciverClass) {
        this.reciverClass = reciverClass;
    }

    String requestName;//请求人的姓名
    String requestType;//请求类型

    public FriendListenerEvent(String requestName, String requestType, String reciverClass) {
        this.requestName = requestName;
        this.requestType = requestType;
        this.reciverClass = reciverClass;
    }

    String reciverClass;//接收的类


}
