package com.yongheng.weixun.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;

import com.alibaba.fastjson.JSON;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.callback.AVIMConversationQueryCallback;
import com.avos.avoscloud.im.v2.callback.AVIMMessagesQueryCallback;
import com.yongheng.weixun.Constants;
import com.yongheng.weixun.R;
import com.yongheng.weixun.activity.ChatActivity;
import com.yongheng.weixun.activity.MainActivity;
import com.yongheng.weixun.adapter.ConversationListAdapter;
import com.yongheng.weixun.event.MessageEvent;
import com.yongheng.weixun.event.RemoveConversationEvent;
import com.yongheng.weixun.event.UpdateConversationListEvent;
import com.yongheng.weixun.model.AccountInfoBean;
import com.yongheng.weixun.model.ConversationInfo;
import com.yongheng.weixun.utils.ToastUtils;

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
    private ImageView mScanLine1;
    private ImageView mScanLine2;
    private FrameLayout mScanLineContainer;
    private boolean mIsLastConversation;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_conversation, null);
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
    private void updateConversationList() {
        if (mConversationListAdapter == null) {
            return;
        }
        final List<ConversationInfo> conversationList = mConversationListAdapter.getConversationList();
        AVIMClient myClient = ((MainActivity) getContext()).getMyClient();
        myClient.getQuery().findInBackground(new AVIMConversationQueryCallback() {
            @Override
            public void done(List<AVIMConversation> list, AVIMException e) {
                if (e != null) {
                    ToastUtils.showException(getContext());
                    return;
                }
                if (list.size() == 0) {
                    onUpdateFinish();
                }
                mIsLastConversation = false;
                conversationList.clear();
                //遍历查询到的会话列表，并将数据封装到ConversationInfo对象中
                for (int i = 0; i < list.size(); i++) {
                    if (i == list.size() - 1) {
                        mIsLastConversation = true;
                    }
                    final AVIMConversation conversation = list.get(i);
                    final ConversationInfo conversationInfo = new ConversationInfo();
                    //会话的name为“自己的账号名&对方的账号名”,将对方的账号名解析出来
                    String contactsAccount = conversation.getName()
                            .replace(((MainActivity) getContext()).getMyAccount(), "")
                            .replace("&", "");
                    conversationInfo.contactsAccount = contactsAccount;
                    conversationInfo.contactsName = contactsAccount;
                    AVQuery<AVObject> query = new AVQuery<>(Constants.TABLE_ACCOUNT);
                    query.whereEqualTo(Constants.FIELD_ACCOUNT_TEL, contactsAccount);
                    query.findInBackground(new FindCallback<AVObject>() {
                        @Override
                        public void done(List<AVObject> list, AVException e) {
                            if (e != null) {
                                ToastUtils.showException(getContext());
                                return;
                            }
                            if (list.size() != 0) {
                                String nameResult = list.get(0).getString(Constants.FIELD_ACCOUNT_NAME);
                                conversationInfo.contactsName = nameResult;
                                String jsonInfo = list.get(0).getString(Constants.FIELD_ACCOUNT_INFO);
                                AccountInfoBean info = JSON.parseObject(jsonInfo, AccountInfoBean.class);
                                conversationInfo.contactsSex = info.sex;
                            }
                            //查询该会话的最后一条消息，查询成功后更新适配器中的数据
                            conversation.queryMessages(1, new AVIMMessagesQueryCallback() {
                                @Override
                                public void done(List<AVIMMessage> list, AVIMException e) {
                                    if (e != null) {
                                        ToastUtils.showException(getContext());
                                        return;
                                    }
                                    if (list != null && list.size() != 0) {
                                        conversationInfo.lastMsg = list.get(0).getContent();
                                    }
                                    conversationList.add(conversationInfo);

                                    if (mIsLastConversation) {
                                        onUpdateFinish();
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });

    }

    private void onUpdateFinish() {
        cancelScan();
        ((MainActivity) getContext()).registerMessageHandler();
        mConversationListAdapter.notifyDataSetChanged();
    }

    private void initView() {
        mConversationList = (ListView) mRootView.findViewById(R.id.lv_message_list);
        mConversationListAdapter = new ConversationListAdapter(getContext());
        mConversationList.setAdapter(mConversationListAdapter);
        mConversationList.setOnItemClickListener(this);

        mScanLine1 = (ImageView) mRootView.findViewById(R.id.iv_scan_line1);
        mScanLine2 = (ImageView) mRootView.findViewById(R.id.iv_scan_line2);
        mScanLineContainer = (FrameLayout) mRootView.findViewById(R.id.fl_scan_container);
        startScan();

    }

    private void startScan() {
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Point size = new Point();
        windowManager.getDefaultDisplay().getSize(size);
        TranslateAnimation animation1 = new TranslateAnimation(0, size.x, 0, 0);
        TranslateAnimation animation2 = new TranslateAnimation(-size.x, 0, 0, 0);
        animation1.setRepeatCount(Integer.MAX_VALUE);
        animation1.setDuration(1000);
        animation2.setRepeatCount(Integer.MAX_VALUE);
        animation2.setDuration(1000);
        mScanLine1.startAnimation(animation1);
        mScanLine2.startAnimation(animation2);

    }

    private void cancelScan() {
        mScanLine1.getAnimation().cancel();
        mScanLineContainer.setVisibility(View.GONE);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String contactsAccount = ((ConversationInfo) mConversationListAdapter.getItem(position)).contactsAccount;
        String contactsName = ((ConversationInfo) mConversationListAdapter.getItem(position)).contactsName;
        goToChat(contactsAccount, contactsName);

    }

    private void goToChat(String contactsAccount, String contactsName) {
        Intent intent = new Intent(getContext(), ChatActivity.class);
        ChatActivity.mMyClient = ((MainActivity) getContext()).getMyClient();
        intent.putExtra("MyAccount", ((MainActivity) getContext()).getMyAccount());
        intent.putExtra("ContactsAccount", contactsAccount);
        intent.putExtra("ContactsName", contactsName);
        startActivity(intent);
    }

    /**
     * 当EventBus 发布了文本消息事件时调用
     *
     * @param event 文本消息事件
     */
    public void onEvent(MessageEvent event) {
        updateConversationList();
    }

    public void onEvent(UpdateConversationListEvent event) {
        updateConversationList();
    }

    public void onEvent(RemoveConversationEvent event) {
        List<ConversationInfo> conversationList = mConversationListAdapter.getConversationList();
        for (int i = 0; i < conversationList.size(); i++) {
            if (conversationList.get(i).contactsAccount.equals(event.account)) {
                conversationList.remove(i);
            }
        }
        mConversationListAdapter.notifyDataSetChanged();
    }
}
