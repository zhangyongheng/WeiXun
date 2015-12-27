package com.yongheng.weixun.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.nineoldandroids.view.ViewHelper;
import com.yongheng.weixun.R;
import com.yongheng.weixun.fragment.ContactsFragment;
import com.yongheng.weixun.fragment.FindFragment;
import com.yongheng.weixun.fragment.ConversationFragment;
import com.yongheng.weixun.utils.AnimUtils;
import com.yongheng.weixun.widget.DragLayout;

/**
 * Created by 张永恒 on 2015/12/22.
 * 主界面Activity
 */

public class MainActivity extends FragmentActivity implements TabHost.OnTabChangeListener {

    private static final String TAG_MESSAGE = "message";
    private static final String TAG_CONTACT = "contact";
    private static final String TAG_FIND = "find";
    private DragLayout mDragLayout;
    private ImageView mIvTitleIcon;
    private FragmentTabHost mTabHost;
    private TextView mTvAppBarTitle;
    private AVIMClient mMyClient;
    private String mMyName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMyName = getIntent().getStringExtra("account");
        initView();
        onLine();
    }

    /**
     * 登录到服务器，登陆成功后更新会话列表
     */
    private void onLine() {
        mMyClient = AVIMClient.getInstance(mMyName);
        mMyClient.open(new AVIMClientCallback() {
            @Override
            public void done(AVIMClient client, AVIMException e) {
                if (e != null) {
                    Toast.makeText(MainActivity.this, R.string.main_login_fail_hint, Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(MainActivity.this, R.string.main_login_success_hint, Toast.LENGTH_SHORT).show();
                ConversationFragment conversationFragment = getMessageFragment();
                conversationFragment.updateConversationList();
            }
        });
    }

    /**
     * 退出登录
     */
    private void offLine() {
        if (mMyClient != null) {
            mMyClient.close(new AVIMClientCallback() {
                @Override
                public void done(AVIMClient avimClient, AVIMException e) {
                    Toast.makeText(MainActivity.this, R.string.main_offline_hint, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private ConversationFragment getMessageFragment() {
        return (ConversationFragment) getSupportFragmentManager().findFragmentByTag(TAG_MESSAGE);
    }

    private void initView() {
        //初始化DragLayout
        mDragLayout = (DragLayout) findViewById(R.id.dl_main_container);
        mDragLayout.setDragListener(new DragLayout.DragListener() {
            @Override
            public void onOpen() {
            }

            @Override
            public void onClose() {
                AnimUtils.startShakeAnimation(MainActivity.this, mIvTitleIcon);
            }

            @Override
            public void onDrag(float percent) {
                ViewHelper.setAlpha(mIvTitleIcon, 1 - percent);
            }
        });


        mIvTitleIcon = (ImageView) findViewById(R.id.iv_appbar_ic);
        mIvTitleIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mDragLayout.open();
            }
        });

        mTvAppBarTitle = (TextView) findViewById(R.id.tv_appbar_title);

        //初始化TabHost
        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.real_tabcontent);

        View indicator = View.inflate(this, R.layout.widget_tab_item, null);
        ((ImageView) indicator.findViewById(R.id.iv_tab_item_icon)).setImageResource(R.drawable.tab_msg_bg_selector);
        TabHost.TabSpec messageSpec = mTabHost.newTabSpec(TAG_MESSAGE).setIndicator(indicator);
        mTabHost.addTab(messageSpec, ConversationFragment.class, null);

        indicator = View.inflate(this, R.layout.widget_tab_item, null);
        ((ImageView) indicator.findViewById(R.id.iv_tab_item_icon)).setImageResource(R.drawable.tab_contact_bg_selector);
        TabHost.TabSpec contactSpec = mTabHost.newTabSpec(TAG_CONTACT).setIndicator(indicator);
        mTabHost.addTab(contactSpec, ContactsFragment.class, null);

        indicator = View.inflate(this, R.layout.widget_tab_item, null);
        ((ImageView) indicator.findViewById(R.id.iv_tab_item_icon)).setImageResource(R.drawable.tab_find_bg_selector);
        TabHost.TabSpec findSpec = mTabHost.newTabSpec(TAG_FIND).setIndicator(indicator);
        mTabHost.addTab(findSpec, FindFragment.class, null);

        mTabHost.setOnTabChangedListener(this);

    }


    @Override
    public void onTabChanged(String tabId) {
        switch (tabId) {
            case TAG_MESSAGE:
                mTvAppBarTitle.setText(R.string.main_tab_conversation);
                break;

            case TAG_CONTACT:
                mTvAppBarTitle.setText(R.string.main_tab_contacts);
                break;

            case TAG_FIND:
                mTvAppBarTitle.setText(R.string.main_tab_find);
                break;
            default:

        }
    }

    public AVIMClient getMyClient() {
        return mMyClient;
    }

    public String getMyName() {
        return mMyName;
    }

    @Override
    protected void onDestroy() {
        offLine();
        super.onDestroy();
    }

}
