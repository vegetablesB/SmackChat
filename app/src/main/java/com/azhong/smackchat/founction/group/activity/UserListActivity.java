package com.azhong.smackchat.founction.group.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.azhong.smackchat.R;
import com.azhong.smackchat.common.base.BaseActivity;
import com.azhong.smackchat.founction.group.adapter.UserListAdapter;
import com.azhong.smackchat.founction.user.bean.UserBean;
import com.azhong.smackchat.service.ConnectionService;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/*
 * 项目名:    SmackChat
 * 包名       com.azhong.smackchat.founction.group.activity
 * 文件名:    UserListActivity
 * 创建者:    CYS
 * 创建时间:  2017/3/13 0013 on 14:18
 * 描述:      聊天室邀请好友   好友列表界面
 */
public class UserListActivity extends BaseActivity implements View.OnClickListener {
    @BindView(R.id.recycler_user)
    RecyclerView recyclerUser;
    @BindView(R.id.edt_new_user)
    EditText edtNewUser;
    @BindView(R.id.btn_invite_new)
    Button btnInviteNew;
    private UserListAdapter adapter;
    private List<UserBean.UserBeanDetails> userList = new ArrayList<>();
    private List<UserBean> contactList;

    private MultiUserChat multiUserChat;
    private ConnectionService iConnection;
    private XMPPTCPConnection connection;
    private String jid;

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            ConnectionService.LocalBinder binder = (ConnectionService.LocalBinder) iBinder;
            contactList = binder.getService().getContact();
            for (UserBean bean : contactList) {
                userList.addAll(bean.getDetails());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        ButterKnife.bind(this);
        initToolBar(true, "邀请好友");
        jid = getIntent().getStringExtra("JID");
        initView();
        bind();
        bindService();
    }

    /**
     * 绑定服务
     */
    private void bind() {
        //开启服务获得与服务器的连接
        Intent intent = new Intent(this, ConnectionService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    private void initView() {
        recyclerUser.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserListAdapter(userList, this);
        recyclerUser.setAdapter(adapter);
        btnInviteNew.setOnClickListener(this);
        adapter.setUserListClickListener(new UserListAdapter.UserListClickListener() {
            @Override
            public void onUserClick(int position) {
                inviteUser(userList.get(position).getUserIp());
            }
        });
    }

    private void bindService() {
        //开启服务获得与服务器的连接
        Intent intent = new Intent(this, ConnectionService.class);
        bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                ConnectionService.LocalBinder binder = (ConnectionService.LocalBinder) service;
                iConnection = binder.getService();
                connection = iConnection.getConnection();
                multiUserChat = MultiUserChatManager.getInstanceFor(connection).
                        getMultiUserChat(jid);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, BIND_AUTO_CREATE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_invite_new:
                inviteUser(edtNewUser.getText().toString()+"@"+connection.getHost());
                break;
            default:
                break;
        }
    }

    //邀请好友
    private void inviteUser(String s) {
        try {
            if (multiUserChat != null) {
                multiUserChat.invite(s, "hello");
                finish();
            }
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }
}
