package com.azhong.smackchat.common.base;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.azhong.smackchat.R;

/*
 * 项目名:    SmackChat
 * 包名       com.azhong.smackchat.activity
 * 文件名:    BaseActivity
 * 创建者:    ZSY
 * 创建时间:  2017/3/2 on 08:58
 * 描述:     TODO 基类
 */
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * 显示底部提示框
     *
     * @param view 点击的按钮
     * @param msg  显示的内容
     */
    public void showSnackBar(View view, String msg) {
        final Snackbar snackBar = Snackbar.make(view, msg, Snackbar.LENGTH_SHORT);
        //snackBar.getView().setBackgroundColor(0xfff44336);
        snackBar.setAction("删除", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //消息上的按钮被点击的事件
                snackBar.dismiss();
            }
        }).show();
    }

    /**
     * 初始化ToolBar
     *
     * @param title  ToolBar中间的标题
     * @param isBack 是否显示左边返回箭头
     * @return Toolbar
     */
    public Toolbar initToolBar(boolean isBack, String title) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        TextView tv = (TextView) findViewById(R.id.tool_bar_title);
        tv.setText(title);
        if (isBack) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
        return toolbar;
    }



}
