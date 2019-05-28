package com.azhong.smackchat.founction.main.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.azhong.smackchat.R;
import com.azhong.smackchat.founction.user.bean.UserBean;

import java.util.List;

/*
 * 项目名:    SmackChat
 * 包名       com.azhong.smackchat.adapter
 * 文件名:    ContactAdapter
 * 创建者:    ZSY
 * 创建时间:  2017/3/2 on 11:50
 * 描述:     TODO 联系人分组适配器
 */
public class ContactAdapter extends BaseExpandableListAdapter {
    private List<UserBean> list;
    private Context context;

    public ContactAdapter(List<UserBean> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getGroupCount() {
        return list.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return list.get(groupPosition).getDetails().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return list.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return list.get(groupPosition).getDetails().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_contact_item, parent, false);
        TextView tv = (TextView) view.findViewById(R.id.name);
        ImageView imageView = (ImageView) view.findViewById(R.id.iv);
        if (isExpanded) {
            imageView.setImageResource(R.drawable.open);
        }else{
            imageView.setImageResource(R.drawable.close);
        }
        tv.setText(list.get(groupPosition).getGroupName());
        return view;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        TextView view = (TextView) LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, null);
        view.setText(list.get(groupPosition).getDetails().get(childPosition).getPickName());
        return view;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
