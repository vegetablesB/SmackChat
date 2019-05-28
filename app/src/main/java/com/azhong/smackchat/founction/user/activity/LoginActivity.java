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
import android.widget.TextView;
import android.widget.Toast;

import com.azhong.smackchat.R;
import com.azhong.smackchat.common.base.BaseActivity;
import com.azhong.smackchat.common.rxbus.RxBus;
import com.azhong.smackchat.common.rxbus.event.HandleEvent;
import com.azhong.smackchat.founction.main.activity.MainActivity;
import com.azhong.smackchat.service.ConnectionService;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/*
 * 项目名:    SmackChat
 * 包名       com.azhong.smackchat.activity
 * 文件名:    LoginActivity
 * 创建者:    ZSY
 * 创建时间:  2017/3/2 on 08:58
 * 描述:     TODO 登录页面
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.username)
    TextInputLayout username;
    @BindView(R.id.password)
    TextInputLayout password;
    @BindView(R.id.login)
    Button login;
    @BindView(R.id.register)
    TextView register;
    private EditText user;
    private EditText psd;
    private ConnectionService service;
    private Subscription subscribe;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        initToolBar(false, "登录");
        login.setOnClickListener(this);
        register.setOnClickListener(this);
        user = username.getEditText();
        psd = password.getEditText();
        bindService();
        loginResult();
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
        startService(intent);
    }

    /**
     * 观察登录状态
     */
    public void loginResult() {
        subscribe = RxBus.getInstance().toObserverable(HandleEvent.class).
                observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<HandleEvent>() {
                    @Override
                    public void call(HandleEvent userBean) {
                        if (userBean.getReceiveClass().equals("LoginActivity") && (Boolean) userBean.getMessage()) {
                            Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else if (userBean.getReceiveClass().equals("LoginActivity") && !(Boolean) userBean.getMessage()) {
                            Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.login:
                final String username = user.getText().toString();
                final String password = psd.getText().toString();
                if (TextUtils.isEmpty(username)) {
                    showSnackBar(v, "请输入帐号");
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    showSnackBar(v, "请输入密码");
                    return;
                }
                service.login(username, password);
                break;
            case R.id.register:
                startActivity(new Intent(this, RegisterActivity.class));
                break;

        }
    }

    @Override
    protected void onDestroy() {
        if (!subscribe.isUnsubscribed()) {
            subscribe.unsubscribe();
        }
        super.onDestroy();

    }
}
