package com.azhong.smackchat.founction.main.fragment;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.azhong.smackchat.R;
import com.azhong.smackchat.common.db.DbHelper;
import com.azhong.smackchat.common.db.bean.MsgList;
import com.azhong.smackchat.common.db.bean.User;
import com.azhong.smackchat.common.rxbus.RxBus;
import com.azhong.smackchat.founction.chat.activity.ChatActivity;
import com.azhong.smackchat.founction.main.adapter.MessageAdapter;
import com.azhong.smackchat.service.ConnectionService;

import org.jivesoftware.smack.packet.Message;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

import static android.content.Context.BIND_AUTO_CREATE;


/*
 * 项目名:    SmackChat
 * 包名       com.azhong.smackchat.founction.main.fragment
 * 文件名:    MessageFragment
 * 创建者:    ZSY
 * 创建时间:  2017/3/2 on 10:22
 * 描述:     TODO 消息页面
 */
public class MessageFragment extends Fragment implements AdapterView.OnItemClickListener {

    @BindView(R.id.msg_list)
    ListView msgList;
    private MessageAdapter adapter;
    private List<MsgList> msgAllList;
    private ConnectionService iConnection;
    private Subscription subscription;
    private User user;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container, false);
        ButterKnife.bind(this, view);
        bind();
        newMsg();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (user != null) {
            getList(user.getUser_id());
        }
    }

    /**
     * 绑定服务
     */
    private void bind() {
        //开启服务获得与服务器的连接
        Intent intent = new Intent(getActivity(), ConnectionService.class);
        getActivity().bindService(intent, connection, BIND_AUTO_CREATE);
    }

    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ConnectionService.LocalBinder binder = (ConnectionService.LocalBinder) service;
            iConnection = binder.getService();
            user = iConnection.GetUser();
            getList(user.getUser_id());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    /**
     * 获取消息列表
     *
     * @param userId
     */
    public void getList(int userId) {
        DbHelper dbHelper = new DbHelper(getActivity());
        msgAllList = dbHelper.getMsgAllList(userId);
        adapter = new MessageAdapter(msgAllList, getContext());
        msgList.setAdapter(adapter);
        msgList.setOnItemClickListener(this);
    }

    /**
     * 列表点击事件
     *
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra("msg_list_id", msgAllList.get(position).getMsg_list_id());
        intent.putExtra("to_name", msgAllList.get(position).getTo_name());
        startActivity(intent);
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
                        getList(user.getUser_id());
                    }
                });
    }

    @Override
    public void onDestroy() {
        if (!subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
        getActivity().unbindService(connection);
        super.onDestroy();

    }
}
