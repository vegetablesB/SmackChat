package com.azhong.smackchat.common.db.bean;

/*
 * 项目名:    SmackChat
 * 包名       com.azhong.smackchat.common.db.bean
 * 文件名:    User
 * 创建者:    ZJB
 * 创建时间:  2017/3/6 on 15:55
 * 描述:     TODO 用户表对应的bean
 */
public class User {
    int user_id;
    String user_name;
    String user_psd;
    String user_head_img;

    public User(int user_id, String user_name, String user_psd, String user_head_img) {
        this.user_id = user_id;
        this.user_name = user_name;
        this.user_psd = user_psd;
        this.user_head_img = user_head_img;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_psd() {
        return user_psd;
    }

    public void setUser_psd(String user_psd) {
        this.user_psd = user_psd;
    }

    public String getUser_head_img() {
        return user_head_img;
    }

    public void setUser_head_img(String user_head_img) {
        this.user_head_img = user_head_img;
    }
}
