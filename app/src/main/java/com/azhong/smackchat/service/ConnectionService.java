package com.azhong.smackchat.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.azhong.smackchat.common.db.DbHelper;
import com.azhong.smackchat.common.db.bean.User;
import com.azhong.smackchat.common.rxbus.RxBus;
import com.azhong.smackchat.common.rxbus.event.FriendListenerEvent;
import com.azhong.smackchat.common.rxbus.event.HandleEvent;
import com.azhong.smackchat.common.utils.LogUtil;
import com.azhong.smackchat.founction.user.bean.UserBean;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterGroup;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.offline.OfflineMessageManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/*
 * 项目名:    SmackChat
 * 包名       com.azhong.smackchat
 * 文件名:    ConnectionService
 * 创建者:    ZSY
 * 创建时间:  2017/3/1 on 13:49
 * 描述:     TODO 获得连接
 */
public class ConnectionService extends Service {

    public static final String SERVER_NAME = "144.168.59.134";//主机名
    public static final String SERVER_IP = "144.168.59.134";//ip
    public static final int PORT = 5222;//端口
    private XMPPTCPConnection connection;
    private DbHelper dbHelper;
    private User user;//用户信息

    /**
     * 获取用户信息
     *
     * @return
     */
    public User GetUser() {
        if (user != null) {
            return user;
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        dbHelper = new DbHelper(this);
        return super.onStartCommand(intent, flags, startId);
    }

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public ConnectionService getService() {
            return ConnectionService.this;
        }
    }

    /**
     * 获得与服务器的连接
     *
     * @return
     */
    public XMPPTCPConnection getConnection() {
        try {
            if (connection == null) {
                XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                        .setHost(SERVER_IP)//服务器IP地址
                        //服务器端口
                        .setPort(PORT)
                        //设置登录状态
                        .setSendPresence(false)
                        //服务器名称
                        .setServiceName(SERVER_NAME)
                        //是否开启安全模式
                        .setSecurityMode(XMPPTCPConnectionConfiguration.SecurityMode.disabled)
                        //是否开启压缩
                        .setCompressionEnabled(false)
                        //开启调试模式
                        .setDebuggerEnabled(true).build();
                connection = new XMPPTCPConnection(config);
                connection.connect();
            }
            return connection;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connection;
    }


    /**
     * 初始化聊天消息监听
     */
    public void initListener() {
        ChatManager manager = ChatManager.getInstanceFor(connection);
        //设置信息的监听
        final ChatMessageListener messageListener = new ChatMessageListener() {
            @Override
            public void processMessage(Chat chat, Message message) {
                //当消息返回为空的时候，表示用户正在聊天窗口编辑信息并未发出消息
                if (!TextUtils.isEmpty(message.getBody())) {
                    try {
                        JSONObject object = new JSONObject(message.getBody());
                        String type = object.getString("type");
                        String data = object.getString("data");
                        LogUtil.d("TAG", data);
                        message.setFrom(message.getFrom().split("/")[0]);
                        message.setBody(data);
                        dbHelper.insertOneMsg(user.getUser_id(), message.getFrom(), data, System.currentTimeMillis() + "", message.getFrom(), 2);
                        RxBus.getInstance().post(message);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        ChatManagerListener chatManagerListener = new ChatManagerListener() {

            @Override
            public void chatCreated(Chat chat, boolean arg1) {
                chat.addMessageListener(messageListener);
            }
        };
        manager.addChatListener(chatManagerListener);
    }

    /**
     * 是否连接成功
     *
     * @return
     */
    public boolean isConnected() {
        if (connection == null) {
            return false;
        }
        if (!connection.isConnected()) {
            try {
                connection.connect();
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    /**
     * 登录
     *
     * @param userName
     * @param password
     */
    public void login(final String userName, final String password) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                getConnection();
                boolean connection = isConnected();
                if (!connection) {
                    return;
                }
                try {
                    ConnectionService.this.connection.login(userName, password);//登录
                    user = dbHelper.SetUser(userName + "@106.14.20.176", password);//插入数据库
                    getOfflineMessage();//一上线获取离线消息
                    initListener();//登录成功开启消息监听
                    RxBus.getInstance().post(new HandleEvent("LoginActivity", true));
                } catch (Exception e) {
                    e.printStackTrace();
                    RxBus.getInstance().post(new HandleEvent("LoginActivity", false));
                }
            }
        }).start();
    }

    /**
     * 一上线获取离线消息
     * 设置登录状态为在线
     */
    private void getOfflineMessage() {
        OfflineMessageManager offlineManager = new OfflineMessageManager(connection);
        try {
            List<Message> list = offlineManager.getMessages();
            for (Message message : list) {
                message.setFrom(message.getFrom().split("/")[0]);
                JSONObject object = new JSONObject(message.getBody());
                String type = object.getString("type");
                String data = object.getString("data");
                //保存离线信息
                dbHelper.insertOneMsg(user.getUser_id(), message.getFrom(), data, System.currentTimeMillis() + "", message.getFrom(), 2);
            }
            //删除离线消息
            offlineManager.deleteMessages();
            //将状态设置成在线
            Presence presence = new Presence(Presence.Type.available);
            connection.sendStanza(presence);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获得所有联系人
     */
    public List<UserBean> getContact() {
        List<UserBean> list = new ArrayList<>();
        if (connection != null) {
            Roster roster = Roster.getInstanceFor(connection);
            Collection<RosterGroup> groups = roster.getGroups();
            List<UserBean.UserBeanDetails> detail = new ArrayList<>();
            for (RosterGroup group : groups) {
                UserBean userBean = new UserBean();
                userBean.setGroupName(group.getName());
                List<RosterEntry> entries = group.getEntries();
                for (RosterEntry entry : entries) {
                    UserBean.UserBeanDetails user = new UserBean.UserBeanDetails();
                    user.setUserIp(entry.getUser());
                    user.setPickName(entry.getName());
                    user.setType(entry.getType());
                    user.setStatus(entry.getStatus());
                    detail.add(user);
                    userBean.setDetails(detail);
                }
                list.add(userBean);
            }
        }
        return list;
    }


    /**
     * 获取指定好友用户信息
     *
     * @param user 用户名
     * @return
     */

    public RosterEntry getUserInfo(String user) {
        if (isConnected()) {
            return Roster.getInstanceFor(connection).getEntry(user);
        } else {
            throw new NullPointerException("服务器连接失败，请先连接服务器");
        }
    }

    /**
     * 添加好友
     *
     * @param account   帐号
     * @param nickName  昵称
     * @param groupName 组名
     */
    public boolean addFriend(String account, String nickName, String[] groupName) {
        try {
            Roster.getInstanceFor(connection).createEntry(account + "@" + SERVER_IP, "", groupName);
            Log.e("TAG", account + "@" + SERVER_IP + "/smack");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 好友信息监听
     */
    public void requestListener() {
        //条件过滤
        StanzaFilter filter = new AndFilter();
        StanzaListener listener = new StanzaListener() {
            @Override
           public void processPacket(Stanza packet) {
                Presence p = (Presence) packet;
                Log.e("TAG", "--" + p.getFrom() + "--" + p.getType());
                if (p.getType().toString().equals("subscrib")) {
                    RxBus.getInstance().post(new FriendListenerEvent(p.getFrom(), "subscrib", "MainActivity"));
                } else if (p.getType().toString().equals("subscribed")) {
                    RxBus.getInstance().post(new FriendListenerEvent(p.getFrom(), "subscribed", "MainActivity"));
                } else if (p.getType().toString().equals("unsubscribe")) {
                    RxBus.getInstance().post(new FriendListenerEvent(p.getFrom(), "unsubscribe", "MainActivity"));
                }
           }
        };
        connection.addAsyncStanzaListener(listener, filter);
    }

    /**
     * 拒绝好友申请
     *
     * @param userId 用户id
     */
    public void refuse(String userId) {
        Presence presence = new Presence(Presence.Type.unsubscribe);
        presence.setTo(userId);
        try {
            connection.sendStanza(presence);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 接收好友申请
     *
     * @param userId 用户id
     */
    public void accept(String userId) {
        Presence presence = new Presence(Presence.Type.subscribe);
        presence.setTo(userId);
        try {
            connection.sendStanza(presence);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建一个新用户
     *
     * @param username 用户名
     * @param password 密码
     * @param attr     一些用户资料
     * @see AccountManager
     */
    public void registerAccount(String username, String password, Map<String, String> attr) {
        getConnection();
        isConnected();
        AccountManager manager = AccountManager.getInstance(connection);
        try {
            if (attr == null) {
                manager.createAccount(username, password);
            } else {
                manager.createAccount(username, password, attr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
