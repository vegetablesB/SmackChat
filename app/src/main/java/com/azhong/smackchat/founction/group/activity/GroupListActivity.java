package com.azhong.smackchat.founction.group.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.azhong.smackchat.R;
import com.azhong.smackchat.common.base.BaseActivity;
import com.azhong.smackchat.common.db.bean.User;
import com.azhong.smackchat.founction.group.adapter.GroupListAdapter;
import com.azhong.smackchat.service.ConnectionService;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

//获取已加入的聊天室列表
public class GroupListActivity extends BaseActivity {
    private static final int REQUEST_CODE = 1000;

    @BindView(R.id.recycler_group)
    RecyclerView recyclerGroup;
    @BindView(R.id.txt_nodata)
    TextView txtNoData;

    private ConnectionService iConnection;
    private XMPPTCPConnection connection;
    private MultiUserChatManager multiUserChatManager;
    private List<HostedRoom> roomList = new ArrayList<>();
    private GroupListAdapter groupListAdapter;
    private User mUser;//当前登录用户
    private boolean isError = false;//标志是否加入聊天室出错

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);
        ButterKnife.bind(this);
        initToolBar(true, "所有群组列表");
        bindService();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            //清空聊天室列表集合，请求数据获取最新聊天室列表
            roomList.clear();
            getData();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_or_join, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent();
        switch (item.getItemId()) {
            case R.id.create_group:
                //创建群聊
                intent.setClass(this, CreateGroupActivity.class);
                break;
            case R.id.join_group:
                //加入群聊
                intent.setClass(this, JoinGroupActivity.class);
                break;
            default:
                break;
        }
        startActivityForResult(intent, REQUEST_CODE);
        return super.onOptionsItemSelected(item);
    }

    //获取群组列表
    private void getData() {
        multiUserChatManager = MultiUserChatManager.getInstanceFor(connection);
        try {
            String user = connection.getUser();
            //判断用户是否支持Multi-User聊天协议
            //注：需要加上资源标识符
            boolean supports = multiUserChatManager.isServiceEnabled(user);
            if (supports) {
                //获取某用户所加入的聊天室
                List<String> serviceNames = multiUserChatManager.getServiceNames();
                for (int i = 0; i < serviceNames.size(); i++) {
                    List<HostedRoom> hostedRooms = multiUserChatManager.getHostedRooms(serviceNames.get(i));
                    roomList.addAll(hostedRooms);
                }
                recyclerGroup.setLayoutManager(new LinearLayoutManager(this));
                groupListAdapter = new GroupListAdapter(roomList, this);
                groupListAdapter.setGroupListClickListener(new GroupListAdapter.GroupListClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        joinChatRoom(roomList.get(position).getJid(), mUser.getUser_name(), "");
                        if (!isError) {
                            Intent intent = new Intent();
                            intent.setClass(GroupListActivity.this, GroupChatActivity.class);
                            intent.putExtra("GroupName", roomList.get(position).getName());
                            intent.putExtra("JID", roomList.get(position).getName() + "@conference." + connection.getServiceName());
                            startActivity(intent);
                        }
                    }
                });
                recyclerGroup.setAdapter(groupListAdapter);
                if (roomList.size() == 0) {
                    txtNoData.setVisibility(View.VISIBLE);
                } else {
                    txtNoData.setVisibility(View.GONE);
                }
            }
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

    private void showDialog(final String groupName) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(GroupListActivity.this);
        View v = GroupListActivity.this.getLayoutInflater()
                .inflate(R.layout.dialog_join_group, null);
        final EditText edtPwd = (EditText) v.findViewById(R.id.edt_group_pwd);
        Button btnJoin = (Button) v.findViewById(R.id.btn_join_group);
        TextView txtName = (TextView) v.findViewById(R.id.txt_group_name);
        txtName.setText("加入" + groupName + "需要密码");
        builder.setView(v);
        final AlertDialog dialog = builder.create();
        dialog.show();
        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(edtPwd.getText().toString())) {
                    joinChatRoom(groupName, mUser.getUser_name(), edtPwd.getText().toString());
                    //若加入聊天室未出错
                    if (!isError) {
                        Intent intent = new Intent();
                        intent.setClass(GroupListActivity.this, GroupChatActivity.class);
                        intent.putExtra("GroupName", groupName.split("@")[0]);
                        intent.putExtra("JID", groupName);
                        startActivity(intent);
                    }
                    dialog.dismiss();
                }
            }
        });
    }

    /**
     * 加入一个群聊聊天室
     *
     * @param jid 聊天室名字
     * @param nickName 用户在聊天室中的昵称
     * @param password 聊天室密码
     * @return
     */
    public MultiUserChat joinChatRoom(String jid, String nickName, String password) {

        try {
            if (!iConnection.isConnected()) {
                Toast.makeText(this, "服务器连接失败，请先连接服务器", Toast.LENGTH_SHORT).show();
                return null;
            }
            // 使用XMPPConnection创建一个MultiUserChat窗口
            MultiUserChat muc = MultiUserChatManager.getInstanceFor(connection).
                    getMultiUserChat(jid);
            // 聊天室服务将会决定要接受的历史记录数量
            DiscussionHistory history = new DiscussionHistory();
            history.setMaxChars(0);
            // history.setSince(new Date());
            // 用户加入聊天室
            muc.join(nickName, password);
            isError = false;
            return muc;
        } catch (XMPPException | SmackException e) {
            isError = true;
            e.printStackTrace();
            if ("XMPPError: not-authorized - auth".equals(e.getMessage())) {
                showDialog(jid);
            }
            Toast.makeText(GroupListActivity.this, "加入失败" + e.getMessage(), Toast.LENGTH_LONG).show();
            return null;
        }
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
                mUser = iConnection.GetUser();
                getData();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, BIND_AUTO_CREATE);
    }
}
