package com.yongheng.weixun.activity;

import android.app.Activity;
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
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;
import com.yongheng.weixun.R;
import com.yongheng.weixun.adapter.ChatListAdapter;
import com.yongheng.weixun.model.MessageInfo;

import java.util.Arrays;
import java.util.List;

/**
 * Created by 张永恒 on 2015/12/23.
 * 聊天界面的Activity
 */
public class ChatActivity extends Activity implements View.OnClickListener {


    private TextView mTvTitle;
    private ListView mLvChatList;
    private EditText mEtInput;
    private Button mBtnSend;
    private ChatListAdapter mChatListAdapter;
    public static AVIMClient mMyClient;
    public static String mMyName;
    public static String mContactsName;
    private AVIMConversation mConversation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initView();
        initData();

    }

    private void initView() {
        mTvTitle = (TextView) findViewById(R.id.tv_chat_title);
        mLvChatList = (ListView) findViewById(R.id.lv_chat_list);
        mEtInput = (EditText) findViewById(R.id.et_chat_input);
        mBtnSend = (Button) findViewById(R.id.btn_chat_send);

        mTvTitle.setText(mContactsName);
        mBtnSend.setOnClickListener(this);
        mChatListAdapter = new ChatListAdapter(this);
        mLvChatList.setAdapter(mChatListAdapter);

    }

    private void initData() {
        initConversation();
    }

    /**
     * 查询服务器获取与该联系人的会话，将查询到的会话对象赋值给mConversation,如果没有则创建会话
     */
    private void initConversation() {
        AVIMConversationQuery query = mMyClient.getQuery();
        //查询条件为成员列表中包含自己和该联系人，m为成员列表的字段
        query.whereContainsAll("m", Arrays.asList(mContactsName, mMyName));
        query.findInBackground(new AVIMConversationQueryCallback() {
            @Override
            public void done(List<AVIMConversation> list, AVIMException e) {
                if (e == null) {
                    if (list.size() == 0) {
                        mMyClient.createConversation(Arrays.asList(mContactsName),
                                mMyName + "&" + mContactsName, null, new AVIMConversationCreatedCallback() {
                                    @Override
                                    public void done(AVIMConversation avimConversation, AVIMException e) {
                                        mConversation = avimConversation;
                                    }
                                });
                    } else {
                        mConversation = list.get(0);
                    }
                    updateMessageRecord();

                }
            }
        });

    }

    /**
     * 从服务器查询最近十条的消息，更新消息记录
     */
    private void updateMessageRecord() {
        if (mConversation == null) {
            return;
        }
        mConversation.queryMessages(10, new AVIMMessagesQueryCallback() {
            @Override
            public void done(List<AVIMMessage> list, AVIMException e) {
                if (e != null) {
                    return;
                }
                List<MessageInfo> messageList = mChatListAdapter.getMessageList();
                messageList.clear();
                for (AVIMMessage message : list) {
                    if (message instanceof AVIMTextMessage) {
                        AVIMTextMessage textMessage = (AVIMTextMessage) message;
                        MessageInfo msg = new MessageInfo();
                        msg.content = textMessage.getText();
                        if (message.getFrom().equals(mMyName)) {
                            msg.type = MessageInfo.TYPE_OUT;
                        } else {
                            msg.type = MessageInfo.TYPE_IN;
                        }
                        messageList.add(msg);
                    }
                }
                mChatListAdapter.notifyDataSetChanged();

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_chat_send:
                String content = mEtInput.getText().toString().trim();
                if (!TextUtils.isEmpty(content)) {
                    mEtInput.setText("");
                    sendTextMessage(content);
                    MessageInfo msg = new MessageInfo();
                    msg.content = content;
                    msg.type = MessageInfo.TYPE_OUT;
                    mChatListAdapter.getMessageList().add(msg);
                    mChatListAdapter.notifyDataSetChanged();
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
    private void sendTextMessage(String content) {
        if (mConversation == null) {
            Toast.makeText(ChatActivity.this, R.string.chat_send_fail_hint, Toast.LENGTH_SHORT).show();
            return;
        }
        AVIMTextMessage msg = new AVIMTextMessage();
        msg.setText(content);
        msg.setFrom(mMyName);
        mConversation.sendMessage(msg, new AVIMConversationCallback() {
            @Override
            public void done(AVIMException e) {
            }
        });

    }

}
