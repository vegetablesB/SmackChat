package com.azhong.smackchat.founction.group.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.azhong.smackchat.R;

import org.jivesoftware.smackx.muc.HostedRoom;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2017/3/10 0010.
 */

public class GroupListAdapter extends RecyclerView.Adapter<GroupListAdapter.GroupListViewHolder> {
    private List<HostedRoom> roomList;
    private Context context;

    public GroupListAdapter(List<HostedRoom> roomList, Context context) {
        this.roomList = roomList;
        this.context = context;
    }

    @Override
    public GroupListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_group_list, parent, false);
        return new GroupListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(GroupListViewHolder holder, final int position) {
        holder.txtGroupList.setText(roomList.get(position).getName() + "\t" + roomList.get(position).getJid());
        holder.txtGroupList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (groupListClickListener != null) {
                    groupListClickListener.onItemClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return roomList == null ? 0 : roomList.size();
    }

    class GroupListViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.txt_group_list)
        TextView txtGroupList;

        public GroupListViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
    private GroupListClickListener groupListClickListener;

    public void setGroupListClickListener(GroupListClickListener groupListClickListener) {
        this.groupListClickListener = groupListClickListener;
    }

    public interface GroupListClickListener{
        void onItemClick(int position);
    }
}
