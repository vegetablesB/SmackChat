package com.azhong.smackchat.founction.chat.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.azhong.smackchat.R;
import com.azhong.smackchat.common.db.bean.Msg;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/*
 * 项目名:    SmackChat
 * 包名       com.azhong.smackchat.adapter
 * 文件名:    ChatAdapter
 * 创建者:    ZSY
 * 创建时间:  2017/3/3 on 9:19
 * 描述:     TODO 聊天适配器
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private List<Msg> list;
    private LayoutInflater inflater;

    public ChatAdapter(List<Msg> list, Context context) {
        this.list = list;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case Msg.FRIENDS_MSG:
                return new ViewHolder(inflater.inflate(R.layout.char_item_friends, parent, false));
            case Msg.SELF_MSG:
                return new ViewHolder(inflater.inflate(R.layout.char_item_self, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.content.setText(list.get(position).getMsg_content());
        holder.name.setText(list.get(position).getFrom_name());
    }

    @Override
    public int getItemViewType(int position) {
        return list.get(position).getFrom_type();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.content)
        TextView content;
        @BindView(R.id.name)
        TextView name;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
