package com.azhong.smackchat.founction.group.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.azhong.smackchat.R;
import com.azhong.smackchat.common.base.BaseActivity;
import com.azhong.smackchat.service.ConnectionService;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;

import butterknife.BindView;
import butterknife.ButterKnife;

public class JoinGroupActivity extends BaseActivity implements View.OnClickListener {
    @BindView(R.id.edt_group_name)
    EditText edtGroupName;
    @BindView(R.id.edt_user_remark)
    EditText edtUserRemark;
    @BindView(R.id.edt_group_pwd)
    EditText edtGroupPwd;
    @BindView(R.id.btn_join_group)
    Button btnJoinGroup;
    private ConnectionService iConnection;
    private XMPPTCPConnection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group);
        ButterKnife.bind(this);
        initToolBar(true, "加入群组");
        bindService();
        btnJoinGroup.setOnClickListener(this);
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
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, BIND_AUTO_CREATE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_join_group:
                if (TextUtils.isEmpty(edtGroupName.getText().toString())) {
                    Toast.makeText(this, "请输入群组名称", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(edtUserRemark.getText().toString())) {
                    Toast.makeText(this, "请输入群内备注名称", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(edtGroupPwd.getText().toString())) {
                    Toast.makeText(this, "请输入聊天室密码", Toast.LENGTH_SHORT).show();
                    return;
                }

                MultiUserChat multiUserChat = joinChatRoom(edtGroupName.getText().toString(), edtUserRemark.getText().toString(), edtGroupPwd.getText().toString());
                try {
                    multiUserChat.sendMessage("王者不可阻挡");//发送群聊消息
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }
                finish();
                break;
            default:
                break;
        }
    }

    /**
     * 加入一个群聊聊天室
     *
     * @param roomName 聊天室名字
     * @param nickName 用户在聊天室中的昵称
     * @param password 聊天室密码
     * @return
     */
    public MultiUserChat joinChatRoom(String roomName, String nickName, String password) {

        try {
            if (!iConnection.isConnected()) {
                Toast.makeText(this, "服务器连接失败，请先连接服务器", Toast.LENGTH_SHORT).show();
                return null;
            }
            // 使用XMPPConnection创建一个MultiUserChat窗口
            MultiUserChat muc = MultiUserChatManager.getInstanceFor(connection).
                    getMultiUserChat(roomName + "@conference." + connection.getServiceName());
            // 聊天室服务将会决定要接受的历史记录数量
            DiscussionHistory history = new DiscussionHistory();
            history.setMaxChars(0);
            // history.setSince(new Date());
            // 用户加入聊天室
            muc.join(nickName, password);
            return muc;
        } catch (XMPPException | SmackException e) {
            e.printStackTrace();
            Toast.makeText(JoinGroupActivity.this, "加入失败" + e.getMessage(), Toast.LENGTH_LONG).show();
            return null;
        }
    }

}
