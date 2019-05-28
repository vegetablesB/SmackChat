package com.azhong.smackchat.founction.chat.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;

import com.azhong.smackchat.R;
import com.azhong.smackchat.common.base.BaseActivity;
import com.azhong.smackchat.common.db.DbHelper;
import com.azhong.smackchat.common.db.bean.Msg;
import com.azhong.smackchat.common.db.bean.User;
import com.azhong.smackchat.common.rxbus.RxBus;
import com.azhong.smackchat.founction.chat.adapter.ChatAdapter;
import com.azhong.smackchat.founction.chat.adapter.EmoAdapter;
import com.azhong.smackchat.founction.ui.EmoticonsEditText;
import com.azhong.smackchat.service.ConnectionService;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;


/*
 * 项目名:    SmackChat
 * 包名       com.azhong.smackchat.activity
 * 文件名:    ChatActivity
 * 创建者:    ZSY
 * 创建时间:  2017/3/2 on 18:00
 * 描述:     TODO 聊天界面
 */
public class ChatActivity extends BaseActivity implements View.OnClickListener, View.OnTouchListener {

    @BindView(R.id.chat_view)
    RecyclerView chatView;
    @BindView(R.id.content)
    EmoticonsEditText content;
    @BindView(R.id.send)
    Button send;
    @BindView(R.id.emo)
    ImageButton Emo;
    @BindView(R.id.recycler_emo)
    RecyclerView recyclerView;
    private ChatAdapter adapter;
    private int msg_list_id;
    private String to_name;
    private DbHelper helper;
    private List<Msg> msgList = new ArrayList<>();
    private Subscription subscription;
    private XMPPTCPConnection xmpptcpConnection;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
        msg_list_id = getIntent().getIntExtra("msg_list_id", -1);
        to_name = getIntent().getStringExtra("to_name");
        initToolBar(true, to_name);
        bindService();
        helper = new DbHelper(this);
        Emo.setOnClickListener(this);
        send.setOnClickListener(this);
        content.setOnTouchListener(this);
        getMsg();
        newMsg();
        chatView.scrollToPosition(msgList.size() - 1);
        initEmo();
    }

    /**
     * 绑定服务
     */
    private void bindService() {
        //开启服务获得与服务器的连接
        Intent intent = new Intent(this, ConnectionService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
    }

    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ConnectionService.LocalBinder binder = (ConnectionService.LocalBinder) service;
            ConnectionService iConnection = binder.getService();
            user = iConnection.GetUser();
            xmpptcpConnection = iConnection.getConnection();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    /**
     * 重数据库中获取聊天记录
     */
    public void getMsg() {
        msgList.clear();
        msgList.addAll(helper.getAllMsg(msg_list_id, -1));
        if (adapter == null) {
            chatView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new ChatAdapter(msgList, this);
            chatView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
        chatView.scrollToPosition(msgList.size() - 1);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send:
                //发送消息
                String msg = content.getText().toString();
                if (!TextUtils.isEmpty(msg)) {
                    boolean b = sendMsg("text", msg, to_name);
                    if (b) {
                        msgList.add(new Msg(user.getUser_name(), msg, System.currentTimeMillis() + "", "text", 1));
                        adapter.notifyDataSetChanged();
                        content.setText("");
                        chatView.scrollToPosition(msgList.size() - 1);
                    }
                }
                break;
            case R.id.emo:
                recyclerView.setVisibility(View.VISIBLE);
                InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                break;
        }
    }

    /**
     * 发送一条消息
     *
     * @param msg     消息内容
     * @param to_name 好友userJid
     * @return
     */
    public boolean sendMsg(String type, String msg, String to_name) {
        boolean isSend;
        String json = toJson(msg, type);
        try {
            ChatManager manager = ChatManager.getInstanceFor(xmpptcpConnection);
            //得到与另一个帐号的连接，这里是一对
            //格式为azhon@106.14.20.176;服务器ip
            Chat chat = manager.createChat(to_name, null);
            chat.sendMessage(json);
            helper.insertOneMsg(user.getUser_id(), to_name, msg, System.currentTimeMillis() + "", user.getUser_name(), 1);
            isSend = true;
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
        recyclerView.setLayoutManager(new GridLayoutManager(this, 7, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);
        adapter.setListener(new EmoAdapter.onItemClickListener() {
            @Override
            public void onItemClick(int position, String emo) {
                try {
                    if (content != null && !TextUtils.isEmpty(emo)) {
                        int start = content.getSelectionStart();
                        CharSequence send_con = content.getText().insert(start, emo);
                        content.setText(send_con);
                        CharSequence info = content.getText();
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

    /**
     * 观察新消息
     */
    public void newMsg() {
        subscription = RxBus.getInstance().toObserverable(Message.class).
                observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Message>() {
                    @Override
                    public void call(Message message) {
                        if (message.getFrom().equals(to_name)) {
                            getMsg();
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        unbindService(connection);
        if (!subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
        super.onDestroy();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                recyclerView.setVisibility(View.GONE);
                break;
        }
        return false;
    }
}
