package com.azhong.smackchat.founction.main.activity;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.azhong.smackchat.R;
import com.azhong.smackchat.common.base.BaseActivity;
import com.azhong.smackchat.common.db.bean.User;
import com.azhong.smackchat.common.rxbus.RxBus;
import com.azhong.smackchat.common.rxbus.event.FriendListenerEvent;
import com.azhong.smackchat.founction.group.activity.GroupChatActivity;
import com.azhong.smackchat.founction.group.activity.GroupListActivity;
import com.azhong.smackchat.founction.main.fragment.ContactFragment;
import com.azhong.smackchat.founction.main.fragment.MessageFragment;
import com.azhong.smackchat.service.ConnectionService;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class MainActivity extends BaseActivity implements TabLayout.OnTabSelectedListener {

    @BindView(R.id.viewpager)
    ViewPager viewpager;
    @BindView(R.id.tab_layout)
    TabLayout tabLayout;
    @BindView(R.id.tool_bar_title)
    TextView title;
    private int image[] = {R.drawable.tab_msg_bg, R.drawable.tab_contact_bg};
    private List<Fragment> list;

    private Subscription subscription;
    private ConnectionService service;
    private String requestName;//请求的用户
    private XMPPTCPConnection connection;
    private User mUser;//当前登录用户
    private boolean isError = false;//标志是否加入聊天室出错
    private Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initToolBar(false, "消息");
        list = new ArrayList<>();
        list.add(new MessageFragment());
        list.add(new ContactFragment());

        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        MyFragmentPagerAdapter adapter = new MyFragmentPagerAdapter();
        viewpager.setOffscreenPageLimit(image.length);
        viewpager.setAdapter(adapter);
        tabLayout.setTabsFromPagerAdapter(adapter);
        tabLayout.setupWithViewPager(viewpager);
        for (int i = 0; i < image.length; i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            View view = getLayoutInflater().inflate(R.layout.tab, null);
            ImageView imageView = (ImageView) view.findViewById(R.id.iv);
            imageView.setImageResource(image[i]);
            //设置自定义的tab布局
            tab.setCustomView(view);
        }
        tabLayout.setOnTabSelectedListener(this);
        onTabSelected(tabLayout.getTabAt(1));
        onTabSelected(tabLayout.getTabAt(0));


        bindService();
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        int position = tab.getPosition();
        viewpager.setCurrentItem(position, false);
        switch (position) {
            case 0:
                title.setText("消息");
                break;
            case 1:
                title.setText("联系人");
                break;
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {
        MyFragmentPagerAdapter() {
            super(MainActivity.this.getSupportFragmentManager());
        }

        @Override
        public Fragment getItem(int position) {
            return list.get(position);
        }

        @Override
        public int getCount() {
            return list.size();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_group, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.get_has_group:
                Intent intent2 = new Intent(this, GroupListActivity.class);
                startActivity(intent2);
                break;
            case R.id.add_friend:
                service.addFriend("qz", "", null);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 绑定服务
     */
    public void bindService() {
        //开启服务获得与服务器的连接
        Intent intent = new Intent(this, ConnectionService.class);
        bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder iBinder) {
                ConnectionService.LocalBinder binder = (ConnectionService.LocalBinder) iBinder;
                service = binder.getService();
                connection = service.getConnection();
                //添加好友申请监听
                service.requestListener();
                //添加聊天室邀请监听
                setGroupInviteListener();
                mUser = service.GetUser();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, BIND_AUTO_CREATE);
        startService(intent);
        RequestListener();
    }

    private void setGroupInviteListener() {
        MultiUserChatManager
                .getInstanceFor(connection)
                .addInvitationListener(new InvitationListener() {
                    @Override
                    public void invitationReceived(XMPPConnection conn, final MultiUserChat room, final String inviter, final String reason, final String password, Message message) {
                        //进入聊天室对话框
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                showGroupInviteDialog(room.getRoom(), inviter, reason, password);
                            }
                        });
                    }
                });
    }

    private void showGroupInviteDialog(final String room, String inviter, String reason, final String password) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(inviter.substring(0, inviter.indexOf("@")) +
                "邀请您加入" + room.substring(0, room.indexOf("@")))
                .setMessage(reason)
                .setPositiveButton("接受", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        joinChatRoom(room.substring(0, room.indexOf("@")), mUser.getUser_name(), password);
                        if (!isError) {
                            Intent intent = new Intent();
                            intent.setClass(MainActivity.this, GroupChatActivity.class);
                            intent.putExtra("GroupName", room.substring(0, room.indexOf("@")));
                            intent.putExtra("JID", room);
                            startActivity(intent);
                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    /**
     * 加入一个群聊聊天室
     *
     * @param roomName 聊天室名字
     * @param nickName 用户在聊天室中的昵称
     * @param password 聊天室密码
     * @return
     */
    public MultiUserChat joinChatRoom(String roomName, String nickName, String password) {

        try {
            if (!service.isConnected()) {
                Toast.makeText(this, "服务器连接失败，请先连接服务器", Toast.LENGTH_SHORT).show();
                return null;
            }
            // 使用XMPPConnection创建一个MultiUserChat窗口
            MultiUserChat muc = MultiUserChatManager.getInstanceFor(connection).
                    getMultiUserChat(roomName + "@conference." + connection.getServiceName());
            // 聊天室服务将会决定要接受的历史记录数量
            DiscussionHistory history = new DiscussionHistory();
            history.setMaxChars(0);
            // history.setSince(new Date());
            // 用户加入聊天室
            muc.join(nickName, password);
            isError = false;
            return muc;
        } catch (XMPPException | SmackException e) {
            isError = true;
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "加入失败" + e.getMessage(), Toast.LENGTH_LONG).show();
            return null;
        }
    }

    /**
     * 观察请求状态
     */
    public void RequestListener() {
        subscription = RxBus.getInstance().toObserverable(FriendListenerEvent.class).
                observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<FriendListenerEvent>() {
                    @Override
                    public void call(FriendListenerEvent friendListenerEvent) {
                        requestName = friendListenerEvent.getRequestName();
                        if ("MainActivity".equals(friendListenerEvent.getReciverClass())) {
                            if ("subscrib".equals(friendListenerEvent.getRequestType())) {
                                //收到好友请求
                                showDialog("好友申请", "账号为" + requestName + "发来一条好友申请");
                            } else if ("subscribed".equals(friendListenerEvent.getRequestType())) {
                                //通过好友请求
                                showDialog("通过了好友请求", "账号为" + requestName + "通过了您的好友请求");
                            } else if ("unsubscribe".equals(friendListenerEvent.getRequestType())) {
                                //拒绝好友请求
                                showDialog("拒绝了好友请求", "账号为" + requestName + "拒绝了您的好友请求并且将你从列表中移除");
                            }
                        }
                    }
                });
    }

    public void showDialog(String title, String content) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(content).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                service.accept(requestName);
                dialog.dismiss();
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                service.refuse(requestName);
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    @Override
    protected void onDestroy() {
        if (!subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
        super.onDestroy();
    }
}
