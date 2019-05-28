package com.azhong.smackchat.founction.main.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.azhong.smackchat.R;
import com.azhong.smackchat.common.db.bean.MsgList;

import java.util.List;


/*
 * 项目名:    SmackChat
 * 包名       com.azhong.smackchat.adapter
 * 文件名:    MessageAdapter
 * 创建者:    ZSY
 * 创建时间:  2017/3/2 on 15:21
 * 描述:     TODO 消息适配器
 */
public class MessageAdapter extends BaseAdapter {

    private List<MsgList> list;
    private Context context;

    public MessageAdapter(List<MsgList> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_msg_item, null);
        TextView name = (TextView) view.findViewById(R.id.friends_name);
        TextView msg = (TextView) view.findViewById(R.id.friends_msg);
        String from = list.get(position).getTo_name().split("@")[0];
        name.setText(from);
        msg.setText(list.get(position).getLast_msg());
        return view;
    }
}
