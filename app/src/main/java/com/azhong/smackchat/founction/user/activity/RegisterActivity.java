package com.azhong.smackchat.founction.user.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.azhong.smackchat.R;
import com.azhong.smackchat.common.base.BaseActivity;
import com.azhong.smackchat.service.ConnectionService;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/*
 * 项目名:    SmackChat
 * 包名       com.azhong.smackchat.activity
 * 文件名:    RegisterActivity
 * 创建者:    ZSY
 * 创建时间:  2017/3/6 on 13:37
 * 描述:     TODO 注册
 */
public class RegisterActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.username)
    TextInputLayout username;
    @BindView(R.id.password)
    TextInputLayout password;
    @BindView(R.id.register)
    Button register;
    private EditText name;
    private EditText psd;
    private ConnectionService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        initToolBar(true, "注册");
        bindService();
        register.setOnClickListener(this);
        name = username.getEditText();
        psd = password.getEditText();

    }

    /**
     * 绑定服务
     */
    public void bindService() {
        //开启服务获得与服务器的连接
        Intent intent = new Intent(this, ConnectionService.class);
        bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder iBinder) {
                ConnectionService.LocalBinder binder = (ConnectionService.LocalBinder) iBinder;
                service = binder.getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, BIND_AUTO_CREATE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.register:
                String n = name.getText().toString();
                String p = psd.getText().toString();
                if (TextUtils.isEmpty(n)) {
                    showSnackBar(v, "请输入帐号");
                } else if (TextUtils.isEmpty(p)) {
                    showSnackBar(v, "请输入密码");
                } else {
                    /** attributes-->>>
                     name,first,last,email,city,state,zip,phone,url,date,misc,text,remove
                     */
                    Map<String, String> map = new HashMap<>();
                    map.put("name", "我是name");
                    map.put("email", "我是email");
                    service.registerAccount(n, p, map);
                }
                break;
        }
    }
}
