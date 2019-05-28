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
import com.azhong.smackchat.common.db.bean.User;
import com.azhong.smackchat.service.ConnectionService;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.FormField;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

//创建、加入聊天室
public class CreateGroupActivity extends BaseActivity implements View.OnClickListener {
    @BindView(R.id.edt_group_name)
    EditText edtGroupName;
    @BindView(R.id.edt_group_pwd)
    EditText edtGroupPwd;
    @BindView(R.id.btn_create_group)
    Button btnCreateGroup;
    private ConnectionService iConnection;
    private XMPPTCPConnection connection;
    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        ButterKnife.bind(this);
        initToolBar(true, "创建群组");
        bindService();
        btnCreateGroup.setOnClickListener(this);
    }

    private void bindService() {
        //开启服务获得与服务器的连接
        final Intent intent = new Intent(this, ConnectionService.class);
        bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                ConnectionService.LocalBinder binder = (ConnectionService.LocalBinder) service;
                iConnection = binder.getService();
                connection = iConnection.getConnection();
                mUser = iConnection.GetUser();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, BIND_AUTO_CREATE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_create_group:
                if (TextUtils.isEmpty(edtGroupName.getText().toString())) {
                    Toast.makeText(this, "请输入群组名称", Toast.LENGTH_SHORT).show();
                    return;
                }

                MultiUserChat chatRoom = createChatRoom(edtGroupName.getText().toString(), mUser.getUser_name(), edtGroupPwd.getText().toString());
                if (chatRoom != null) {
                    setResult(RESULT_OK);
                    finish();
                }
                finish();
                break;
            default:
                break;
        }
    }

    /**
     * 创建群聊聊天室
     *
     * @param roomName 聊天室名字
     * @param nickName 创建者在聊天室中的昵称
     * @param password 聊天室密码
     * @return
     */
    public MultiUserChat createChatRoom(String roomName, String nickName, String password) {
        if (!iConnection.isConnected()) {
            throw new NullPointerException("服务器连接失败，请先连接服务器");
        }
        MultiUserChat muc;
        try {
            // 创建一个MultiUserChat
            muc = MultiUserChatManager.getInstanceFor(connection).getMultiUserChat(roomName + "@conference." + connection.getServiceName());
            // 创建聊天室
            boolean isCreated = muc.createOrJoin(nickName);
            if (isCreated) {
                // 获得聊天室的配置表单
                Form form = muc.getConfigurationForm();
                // 根据原始表单创建一个要提交的新表单。
                Form submitForm = form.createAnswerForm();
                // 向要提交的表单添加默认答复
                List<FormField> fields = form.getFields();
                for (int i = 0; fields != null && i < fields.size(); i++) {
                    if (FormField.Type.hidden != fields.get(i).getType() &&
                            fields.get(i).getVariable() != null) {
                        // 设置默认值作为答复
                        submitForm.setDefaultAnswer(fields.get(i).getVariable());
                    }
                }
                // 设置聊天室的新拥有者
                List owners = new ArrayList();
                owners.add(connection.getUser());// 用户JID
                submitForm.setAnswer("muc#roomconfig_roomowners", owners);
                // 设置聊天室是持久聊天室，即将要被保存下来
                submitForm.setAnswer("muc#roomconfig_persistentroom", true);
                // 房间仅对成员开放
                submitForm.setAnswer("muc#roomconfig_membersonly", false);
                // 允许占有者邀请其他人
                submitForm.setAnswer("muc#roomconfig_allowinvites", true);
                if (password != null && password.length() != 0) {
                    // 进入是否需要密码
                    submitForm.setAnswer("muc#roomconfig_passwordprotectedroom", true);
                    // 设置进入密码
                    submitForm.setAnswer("muc#roomconfig_roomsecret", password);
                }
                // 能够发现占有者真实 JID 的角色
                // submitForm.setAnswer("muc#roomconfig_whois", "anyone");
                // 登录房间对话
                submitForm.setAnswer("muc#roomconfig_enablelogging", true);
                // 仅允许注册的昵称登录
                submitForm.setAnswer("x-muc#roomconfig_reservednick", true);
                // 允许使用者修改昵称
                submitForm.setAnswer("x-muc#roomconfig_canchangenick", false);
                // 允许用户注册房间
                submitForm.setAnswer("x-muc#roomconfig_registration", false);
                // 发送已完成的表单（有默认值）到服务器来配置聊天室
                muc.sendConfigurationForm(submitForm);
                Toast.makeText(this, "创建成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "创建失败", Toast.LENGTH_SHORT).show();
            }
        } catch (XMPPException | SmackException e) {
            e.printStackTrace();
            Toast.makeText(this, "创建失败" + e.getMessage(), Toast.LENGTH_LONG).show();
            return null;
        }
        return muc;
    }
}
