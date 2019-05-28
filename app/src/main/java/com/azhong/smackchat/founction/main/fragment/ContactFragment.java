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
import android.widget.ExpandableListView;

import com.azhong.smackchat.R;
import com.azhong.smackchat.common.db.DbHelper;
import com.azhong.smackchat.common.db.bean.MsgList;
import com.azhong.smackchat.common.db.bean.User;
import com.azhong.smackchat.founction.chat.activity.ChatActivity;
import com.azhong.smackchat.founction.main.adapter.ContactAdapter;
import com.azhong.smackchat.founction.user.bean.UserBean;
import com.azhong.smackchat.service.ConnectionService;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.Context.BIND_AUTO_CREATE;


/*
 * 项目名:    SmackChat
 * 包名       com.azhong.smackchat.founction.main.fragment
 * 文件名:    ContactFragment
 * 创建者:    ZSY
 * 创建时间:  2017/3/2 on 10:23
 * 描述:     TODO
 */
public class ContactFragment extends Fragment {

    @BindView(R.id.expand_list)
    ExpandableListView expandList;
    private ConnectionService service;
    private List<UserBean> contact;
    private User user;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact, container, false);
        ButterKnife.bind(this, view);
        bind();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        expandList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                String friendName = contact.get(groupPosition).getDetails().get(childPosition).getUserIp();
                MsgList msgList = new DbHelper(getActivity()).checkMsgList(user.getUser_id(), friendName);
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra("msg_list_id", msgList.getMsg_list_id());
                intent.putExtra("to_name", msgList.getTo_name());
                startActivity(intent);
                return false;
            }
        });
    }

    private void bind() {
        //开启服务获得与服务器的连接
        Intent intent = new Intent(getActivity(), ConnectionService.class);
        getActivity().bindService(intent, connection, BIND_AUTO_CREATE);
    }

    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            ConnectionService.LocalBinder binder = (ConnectionService.LocalBinder) iBinder;
            service = binder.getService();
            contact = service.getContact();
            expandList.setGroupIndicator(null);
            ContactAdapter adapter = new ContactAdapter(contact, getActivity());
            expandList.setAdapter(adapter);
            user = service.GetUser();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
}
