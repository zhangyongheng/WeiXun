package com.yongheng.weixun.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMConversationQuery;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCreatedCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationQueryCallback;
import com.avos.avoscloud.im.v2.callback.AVIMMessagesQueryCallback;
import com.yongheng.weixun.Constants;
import com.yongheng.weixun.MyApplication;
import com.yongheng.weixun.R;
import com.yongheng.weixun.adapter.ChatListAdapter;
import com.yongheng.weixun.event.MessageEvent;
import com.yongheng.weixun.event.UpdateConversationInfoEvent;
import com.yongheng.weixun.model.MessageContentBean;
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
    private String mMyAccount;
    private String mContactsName;
    private String mContactsAccount;
    private AVIMConversation mConversation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent intent = getIntent();
        mMyAccount = ((MyApplication) getApplication()).getMyAccount();
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
        TextView tvReturn = (TextView) findViewById(R.id.tv_chat_return);
        tvReturn.setOnClickListener(this);
        TextView tvMore = (TextView) findViewById(R.id.tv_chat_more);
        tvMore.setOnClickListener(this);
        final LinearLayout llContainer = (LinearLayout) findViewById(R.id.ll_chat_container);
        llContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = llContainer.getRootView().getHeight() - llContainer.getHeight();
                if (heightDiff > 100) {
                    scrollToBottom();
                }
            }
        });

    }

    private void initData() {
        initConversation();
    }

    /**
     * 查询服务器获取与该联系人的会话，将查询到的会话对象赋值给mConversation
     */
    private void initConversation() {
        AVIMConversationQuery query = ((MyApplication) getApplication()).getMyClient().getQuery();
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
                    MessageContentBean contentBean = JSON.parseObject(message.getContent(), MessageContentBean.class);
                    msg.content = contentBean.c;
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

            case R.id.tv_chat_return:
                finish();
                break;

            case R.id.tv_chat_more:
                showMoreMenu(v);
                break;

            default:

        }
    }

    private void showMoreMenu(View v) {
        PopupMenu menu = new PopupMenu(ChatActivity.this, v);
        menu.getMenuInflater().inflate(R.menu.menu_chat_more, menu.getMenu());
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_chat_info:
                        goToInfo();
                        break;
                    case R.id.menu_chat_clear_msg:
                        break;
                    default:
                }
                return true;
            }
        });
        menu.show();
    }

    private void goToInfo() {
        Intent intent = new Intent(ChatActivity.this, InfoActivity.class);
        intent.putExtra("Account", mContactsAccount)
                .putExtra("Name", mContactsName)
                .putExtra("From", InfoActivity.FROM_CONTACTS);
        startActivity(intent);
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
        MessageContentBean contentBean = new MessageContentBean();
        contentBean.a = ((MyApplication) getApplication()).getMyAccount() + "&" +
                ((MyApplication) getApplication()).getMyName();
        contentBean.c = content;
        String jsonContent = JSON.toJSONString(contentBean);
        AVIMMessage msg = new AVIMMessage();
        msg.setContent(jsonContent);
        msg.setFrom(mMyAccount);
        mConversation.sendMessage(msg, new AVIMConversationCallback() {
            @Override
            public void done(AVIMException e) {
                if (e != null) {
                    Toast.makeText(ChatActivity.this, R.string.chat_send_fail_hint, Toast.LENGTH_SHORT).show();
                    return;
                }
                UpdateConversationInfoEvent updateInfoEvent = new UpdateConversationInfoEvent();
                updateInfoEvent.account = mContactsAccount;
                updateInfoEvent.lastMsg = content;
                EventBus.getDefault().post(updateInfoEvent);
            }
        });

    }

    private void createConversation(final CreateConversationCallBack callBack) {
        ((MyApplication) getApplication()).getMyClient().createConversation(Arrays.asList(mContactsAccount),
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
