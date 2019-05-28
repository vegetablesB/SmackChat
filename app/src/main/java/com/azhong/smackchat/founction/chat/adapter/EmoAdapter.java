package com.azhong.smackchat.founction.chat.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.azhong.smackchat.R;
import com.azhong.smackchat.founction.utils.FaceTextUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/*
 * 项目名:    SmackChat
 * 包名       com.azhong.smackchat.founction.chat.adapter
 * 文件名:    EmoAdapter
 * 创建者:    ZSY
 * 创建时间:  2017/3/9 on 15:56
 * 描述:     TODO 表情适配器
 */
public class EmoAdapter extends RecyclerView.Adapter<EmoAdapter.ViewHolder> implements View.OnClickListener {
    private LayoutInflater inflater;
    private Context context;

    public EmoAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.item_emo, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.itemView.setTag(position);
        holder.itemView.setOnClickListener(this);
        FaceTextUtils.FaceText text = FaceTextUtils.faceTexts.get(position);
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
                context.getResources().getIdentifier(text.fileName, "drawable", context.getPackageName()), options);
        holder.iv.setImageBitmap(bitmap);
    }

    @Override
    public int getItemCount() {
        return FaceTextUtils.faceTexts.size();
    }

    @Override
    public void onClick(View v) {
        if (listener != null) {
            int position = (int) v.getTag();
            listener.onItemClick(position, FaceTextUtils.faceTexts.get(position).emo);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv)
        ImageView iv;
        View itemView;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            ButterKnife.bind(this, itemView);
        }
    }

    private onItemClickListener listener;

    public void setListener(onItemClickListener listener) {
        this.listener = listener;
    }

    public interface onItemClickListener {
        void onItemClick(int position, String emo);
    }
}

