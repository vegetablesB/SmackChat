package com.azhong.smackchat.founction.group.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;

import com.azhong.smackchat.R;
import com.azhong.smackchat.common.base.BaseActivity;
import com.azhong.smackchat.common.db.DbHelper;
import com.azhong.smackchat.common.db.bean.Msg;
import com.azhong.smackchat.common.db.bean.MsgList;
import com.azhong.smackchat.common.db.bean.User;
import com.azhong.smackchat.founction.chat.adapter.ChatAdapter;
import com.azhong.smackchat.founction.chat.adapter.EmoAdapter;
import com.azhong.smackchat.founction.ui.EmoticonsEditText;
import com.azhong.smackchat.service.ConnectionService;

import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.muc.InvitationRejectionListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/*
 * 项目名:    SmackChat
 * 包名       com.azhong.smackchat.founction.group.activity
 * 文件名:    GroupChatActivity
 * 创建者:    CYS
 * 创建时间:  2017/3/13 0013 on 14:18
 * 描述:     聊天室聊天界面
 */
public class GroupChatActivity extends BaseActivity implements View.OnClickListener, View.OnTouchListener {
    @BindView(R.id.recycler_chat)
    RecyclerView recyclerChat;
    @BindView(R.id.recycler_emo)
    RecyclerView recyclerEmo;
    @BindView(R.id.btn_send)
    Button btnSend;
    @BindView(R.id.edt_content)
    EmoticonsEditText edtContent;
    @BindView(R.id.imgb_emo)
    ImageButton imgButtonEmo;

    private ChatAdapter adapter;
    private String jid;
    private DbHelper helper;
    private List<Msg> msgList = new ArrayList<>();
    private User user;
    private MultiUserChat multiUserChat;
    private ConnectionService iConnection;
    private XMPPTCPConnection connection;
    private Handler handler = new Handler();
    private boolean flag = false;//为true则为自己发送的消息

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        ButterKnife.bind(this);
        String group_name = getIntent().getStringExtra("GroupName");
        jid = getIntent().getStringExtra("JID");
        initToolBar(true, group_name);
        bindService();
        helper = new DbHelper(this);
        imgButtonEmo.setOnClickListener(this);
        btnSend.setOnClickListener(this);
        edtContent.setOnTouchListener(this);
        recyclerChat.scrollToPosition(msgList.size() - 1);
        initEmo();
    }


    private void initListener() {
        multiUserChat.addMessageListener(new MessageListener() {
            @Override
            public void processMessage(final Message message) {
                //当消息返回为空的时候，表示用户正在聊天窗口编辑信息并未发出消息
                if (!TextUtils.isEmpty(message.getBody())) {
                    try {
                        JSONObject object = new JSONObject(message.getBody());
                        String type = object.getString("type");
                        String data = object.getString("data");
                        message.setFrom(message.getFrom().split("/")[0]);
                        message.setBody(data);
                        if (flag) {
                            helper.insertOneMsg(user.getUser_id(), message.getFrom(), data, System.currentTimeMillis() + "", message.getFrom(), 1);
                            flag = false;
                        } else {
                            helper.insertOneMsg(user.getUser_id(), message.getFrom(), data, System.currentTimeMillis() + "", message.getFrom(), 2);
                        }

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                //观察新消息
                                if (message.getFrom().equals(jid)) {
                                    getMsg();
                                }
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        multiUserChat.addInvitationRejectionListener(new InvitationRejectionListener() {
            @Override
            public void invitationDeclined(final String invitee, final String reason) {
                Log.d("test", invitee + "拒绝" + reason + "-----------");
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
                user = iConnection.GetUser();
                getMsg();
                multiUserChat = MultiUserChatManager.getInstanceFor(connection).
                        getMultiUserChat(jid);
                initListener();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.memu_invite_user, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.invite_user:
                Intent intent = new Intent(this, UserListActivity.class);
                intent.putExtra("JID", jid);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 从数据库中获取聊天记录
     */
    public void getMsg() {
        msgList.clear();
        MsgList messageList = helper.checkMsgList(user.getUser_id(), jid);
        msgList.addAll(helper.getAllMsg(messageList.getMsg_list_id(), -1));
        if (adapter == null) {
            recyclerChat.setLayoutManager(new LinearLayoutManager(this));
            adapter = new ChatAdapter(msgList, this);
            recyclerChat.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
        recyclerChat.scrollToPosition(msgList.size() - 1);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send:
                //发送消息
                String msg = edtContent.getText().toString();
                if (!TextUtils.isEmpty(msg)) {
                    boolean b = sendMsg("text", msg);
                    flag = true;
                    if (b) {
                        edtContent.setText("");
                        recyclerChat.scrollToPosition(msgList.size() - 1);
                    }
                }
                break;
            case R.id.imgb_emo:
                recyclerEmo.setVisibility(View.VISIBLE);
                InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                break;
        }
    }

    /**
     * 发送一条消息
     *
     * @param msg 消息内容
     * @return
     */
    public boolean sendMsg(String type, String msg) {
        boolean isSend = true;
        String json = toJson(msg, type);
        try {
            if (multiUserChat != null) {
                multiUserChat.sendMessage(json);
                isSend = true;
            }
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
            isSend = false;
        }
        return isSend;
    }

    /**
     * 打包成json字符串
     *
     * @param msg
     * @param type text>>文本,voice>>语音,image>>图片
     */
    private String toJson(String msg, String type) {
        try {
            JSONObject object = new JSONObject();
            object.put("type", type);
            object.put("data", msg);
            return object.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 初始化表情
     */
    private void initEmo() {
        EmoAdapter adapter = new EmoAdapter(this);
        recyclerEmo.setLayoutManager(new GridLayoutManager(this, 7, LinearLayoutManager.VERTICAL, false));
        recyclerEmo.setAdapter(adapter);
        adapter.setListener(new EmoAdapter.onItemClickListener() {
            @Override
            public void onItemClick(int position, String emo) {
                try {
                    if (edtContent != null && !TextUtils.isEmpty(emo)) {
                        int start = edtContent.getSelectionStart();
                        CharSequence send_con = edtContent.getText().insert(start, emo);
                        edtContent.setText(send_con);
                        CharSequence info = edtContent.getText();
                        if (info != null) {
                            Spannable spanText = (Spannable) info;
                            Selection.setSelection(spanText, start + emo.length());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                recyclerEmo.setVisibility(View.GONE);
                break;
        }
        return false;
    }
}
