package com.yongheng.weixun.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.callback.AVIMConversationQueryCallback;
import com.avos.avoscloud.im.v2.callback.AVIMMessagesQueryCallback;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;
import com.yongheng.weixun.R;
import com.yongheng.weixun.activity.ChatActivity;
import com.yongheng.weixun.activity.MainActivity;
import com.yongheng.weixun.adapter.ConversationListAdapter;
import com.yongheng.weixun.event.IMTextEvent;
import com.yongheng.weixun.model.ConversationInfo;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by 张永恒 on 2015/12/22.
 * 消息Tab页的Fragment
 */
public class ConversationFragment extends Fragment implements AdapterView.OnItemClickListener {

    private View mRootView;
    private ListView mConversationList;
    private ConversationListAdapter mConversationListAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_message, null);
        }
        return mRootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mConversationList == null) {
            initView();
        }
    }

    /**
     * 查询服务器更新会话列表
     */
    public void updateConversationList() {
        if (mConversationListAdapter == null) {
            return;
        }
        final List<ConversationInfo> conversationList = mConversationListAdapter.getConversationList();
        ((MainActivity) getContext()).getMyClient().getQuery().findInBackground(
                new AVIMConversationQueryCallback() {
                    @Override
                    public void done(List<AVIMConversation> list, AVIMException e) {
                        if (e != null) {
                            return;
                        }
                        conversationList.clear();
                        //遍历查询到的会话列表，并将数据封装到ConversationInfo对象中
                        for (AVIMConversation conversation : list) {
                            final ConversationInfo conversationInfo = new ConversationInfo();
                            //会话的name为“自己的账号名&对方的账号名”,将对方的账号名解析出来
                            String contactsName = conversation.getName()
                                    .replace(((MainActivity) getContext()).getMyName(), "")
                                    .replace("&", "");
                            conversationInfo.contactsName = contactsName;
                            //查询该会话的最后一条消息，查询成功后更新适配器中的数据
                            conversation.queryMessages(1, new AVIMMessagesQueryCallback() {
                                @Override
                                public void done(List<AVIMMessage> list, AVIMException e) {
                                    if (e != null) {
                                        return;
                                    }
                                    if (list.size() != 0) {
                                        if (list.get(0) instanceof AVIMTextMessage) {
                                            conversationInfo.lastMsg = ((AVIMTextMessage) list.get(0)).getText();
                                        }
                                    }
                                    conversationList.add(conversationInfo);
                                    mConversationListAdapter.notifyDataSetChanged();
                                }
                            });
                        }

                    }
                });

    }

    private void initView() {
        mConversationList = (ListView) mRootView.findViewById(R.id.lv_message_list);
        mConversationListAdapter = new ConversationListAdapter(getContext());
        mConversationList.setAdapter(mConversationListAdapter);
        mConversationList.setOnItemClickListener(this);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(getContext(), ChatActivity.class);
        ChatActivity.mMyClient = ((MainActivity) getContext()).getMyClient();
        ChatActivity.mMyName = ((MainActivity) getContext()).getMyName();
        ChatActivity.mContactsName = ((ConversationInfo)
                mConversationListAdapter.getItem(position)).contactsName;
        startActivity(intent);

    }

    /**
     * 当EventBus 发布了文本消息事件时调用
     *
     * @param event 文本消息事件
     */
    public void onEvent(IMTextEvent event) {
        updateConversationList();
    }
}
