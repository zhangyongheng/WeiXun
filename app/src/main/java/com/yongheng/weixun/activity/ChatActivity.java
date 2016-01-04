package com.yongheng.weixun.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMConversationQuery;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCreatedCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationQueryCallback;
import com.avos.avoscloud.im.v2.callback.AVIMMessagesQueryCallback;
import com.yongheng.weixun.Constants;
import com.yongheng.weixun.R;
import com.yongheng.weixun.adapter.ChatListAdapter;
import com.yongheng.weixun.event.MessageEvent;
import com.yongheng.weixun.model.MessageInfo;
import com.yongheng.weixun.utils.ToastUtils;

import java.util.Arrays;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by 张永恒 on 2015/12/23.
 * 聊天界面的Activity
 */
public class ChatActivity extends Activity implements View.OnClickListener {


    private ListView mLvChatList;
    private EditText mEtInput;
    private ChatListAdapter mChatListAdapter;
    public static AVIMClient mMyClient;
    private String mMyAccount;
    private String mContactsName;
    private String mContactsAccount;
    private AVIMConversation mConversation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent intent = getIntent();
        mMyAccount = intent.getStringExtra("MyAccount");
        mContactsName = intent.getStringExtra("ContactsName");
        mContactsAccount = intent.getStringExtra("ContactsAccount");
        EventBus.getDefault().register(this);

        initView();
        initData();

    }

    private void initView() {
        TextView tvTitle = (TextView) findViewById(R.id.tv_chat_title);
        Button btnSend = (Button) findViewById(R.id.btn_chat_send);
        mLvChatList = (ListView) findViewById(R.id.lv_chat_list);
        mEtInput = (EditText) findViewById(R.id.et_chat_input);
        tvTitle.setText(mContactsName);
        btnSend.setOnClickListener(this);
        mChatListAdapter = new ChatListAdapter(this);
        mLvChatList.setAdapter(mChatListAdapter);

    }

    private void initData() {
        initConversation();
    }

    /**
     * 查询服务器获取与该联系人的会话，将查询到的会话对象赋值给mConversation
     */
    private void initConversation() {
        AVIMConversationQuery query = mMyClient.getQuery();
        //查询条件为成员列表中包含自己和该联系人，m为成员列表的字段
        query.whereContainsAll(Constants.FIELD_CONVERSATION_MEMBER, Arrays.asList(mContactsAccount, mMyAccount));
        query.findInBackground(new AVIMConversationQueryCallback() {
            @Override
            public void done(List<AVIMConversation> list, AVIMException e) {
                if (e != null) {
                    ToastUtils.showException(ChatActivity.this);
                    return;
                }
                if (list.size() > 0) {
                    mConversation = list.get(0);
                }
                updateMessageRecord();
            }
        });

    }

    /**
     * 从服务器查询最近10条的消息，更新消息记录
     */
    private void updateMessageRecord() {
        if (mConversation == null) {
            return;
        }
        mConversation.queryMessages(10, new AVIMMessagesQueryCallback() {
            @Override
            public void done(List<AVIMMessage> list, AVIMException e) {
                if (e != null) {
                    ToastUtils.showException(ChatActivity.this);
                    return;
                }
                List<MessageInfo> messageList = mChatListAdapter.getMessageList();
                messageList.clear();
                for (AVIMMessage message : list) {
                    MessageInfo msg = new MessageInfo();
                    msg.content = message.getContent();
                    if (message.getFrom().equals(mMyAccount)) {
                        msg.type = MessageInfo.TYPE_OUT;
                    } else {
                        msg.type = MessageInfo.TYPE_IN;
                    }
                    messageList.add(msg);
                }
                mChatListAdapter.notifyDataSetChanged();
                scrollToBottom();

            }
        });
    }

    /**
     * 消息列表滚动到底部
     */
    private void scrollToBottom() {
        mLvChatList.setSelection(mChatListAdapter.getCount() - 1);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_chat_send:
                String content = mEtInput.getText().toString().trim();
                mEtInput.setText("");
                if (!TextUtils.isEmpty(content)) {
                    sendTextMessage(content);
                    MessageInfo msg = new MessageInfo();
                    msg.content = content;
                    msg.type = MessageInfo.TYPE_OUT;
                    mChatListAdapter.getMessageList().add(msg);
                    mChatListAdapter.notifyDataSetChanged();
                    scrollToBottom();
                }

                break;
            default:

        }
    }

    /**
     * 发送文本消息
     *
     * @param content 消息内容
     */
    private void sendTextMessage(final String content) {
        if (mConversation == null) {
            createConversation(new CreateConversationCallBack() {
                @Override
                public void onFinish() {
                    sendTextMessage(content);
                }
            });
            return;
        }
        AVIMMessage msg = new AVIMMessage();
        msg.setContent(content);
        msg.setFrom(mMyAccount);
        mConversation.sendMessage(msg, new AVIMConversationCallback() {
            @Override
            public void done(AVIMException e) {
                if (e != null) {
                    Toast.makeText(ChatActivity.this, R.string.chat_send_fail_hint, Toast.LENGTH_SHORT).show();
                    return;
                }
                EventBus.getDefault().post(new MessageEvent());
            }
        });

    }

    private void createConversation(final CreateConversationCallBack callBack) {
        mMyClient.createConversation(Arrays.asList(mContactsAccount),
                mMyAccount + "&" + mContactsAccount, null, new AVIMConversationCreatedCallback() {
                    @Override
                    public void done(AVIMConversation avimConversation, AVIMException e) {
                        if (e != null) {
                            ToastUtils.showException(ChatActivity.this);
                            return;
                        }
                        mConversation = avimConversation;
                        callBack.onFinish();
                    }
                });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    interface CreateConversationCallBack {
        void onFinish();
    }

    public void onEvent(MessageEvent event) {
        updateMessageRecord();
    }

}
