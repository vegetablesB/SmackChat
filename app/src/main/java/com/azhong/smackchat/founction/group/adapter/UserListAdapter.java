package com.azhong.smackchat.founction.group.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.azhong.smackchat.R;
import com.azhong.smackchat.founction.user.bean.UserBean;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/*
 * 项目名:    SmackChat
 * 包名       com.azhong.smackchat.founction.group.adapter
 * 文件名:    UserListAdapter
 * 创建者:    CYS
 * 创建时间:  2017/3/13 0013 on 15:59
 * 描述:     已添加好友列表适配器
 */
public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserListViewHolder> {
    private List<UserBean.UserBeanDetails> list;
    private Context context;

    public UserListAdapter(List<UserBean.UserBeanDetails> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public UserListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_user_list, parent,false);
        return new UserListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(UserListViewHolder holder, final int position) {
        holder.txtUser.setText(list.get(position).getPickName());
        holder.txtUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (userListClickListener != null) {
                    userListClickListener.onUserClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    class UserListViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.txt_user_list)
        TextView txtUser;

        public UserListViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private UserListClickListener userListClickListener;

    public void setUserListClickListener(UserListClickListener userListClickListener) {
        this.userListClickListener = userListClickListener;
    }

    public interface UserListClickListener {
        void onUserClick(int position);
    }
}
