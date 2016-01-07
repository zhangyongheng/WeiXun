package com.yongheng.weixun.activity;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.AVIMMessageManager;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.nineoldandroids.view.ViewHelper;
import com.yongheng.weixun.Constants;
import com.yongheng.weixun.MyApplication;
import com.yongheng.weixun.MyMessageHandler;
import com.yongheng.weixun.R;
import com.yongheng.weixun.event.MessageNotificationEvent;
import com.yongheng.weixun.event.UpdateConversationListEvent;
import com.yongheng.weixun.fragment.ContactsFragment;
import com.yongheng.weixun.fragment.ConversationFragment;
import com.yongheng.weixun.fragment.FindFragment;
import com.yongheng.weixun.model.AccountInfoBean;
import com.yongheng.weixun.model.MessageContentBean;
import com.yongheng.weixun.utils.AnimUtils;
import com.yongheng.weixun.utils.ToastUtils;
import com.yongheng.weixun.widget.DragLayout;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by 张永恒 on 2015/12/22.
 * 主界面Activity
 */

public class MainActivity extends FragmentActivity implements TabHost.OnTabChangeListener, OnClickListener {

    private static final String TAG_CONVERSATION = "message";
    private static final String TAG_CONTACT = "contact";
    private static final String TAG_FIND = "find";
    private DragLayout mDragLayout;
    private FragmentTabHost mTabHost;
    private ImageView mIvTitleIcon;
    private TextView mTvAppBarTitle;
    private boolean mHasOpen;
    private TextView mTvAdd;
    private AccountInfoBean mMyInfo;
    private TextView mTvSign;
    private TextView mTvName;
    private List<Integer> mNotificationIds;
    private PopupWindow mPwMainMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);
        AVIMMessageManager.registerDefaultMessageHandler(new MyMessageHandler());
        ((MyApplication) getApplication()).finishAllActivity();
        ((MyApplication) getApplication()).setMyAccount(getIntent().getStringExtra("Account"));
        ((MyApplication) getApplication()).setMyName(getIntent().getStringExtra("Account"));
        initView();
        onLine();

    }

    /**
     * 登录到服务器，登陆成功后更新会话列表
     */
    private void onLine() {
        ((MyApplication) getApplication()).setMyClient(
                AVIMClient.getInstance(((MyApplication) getApplication()).getMyAccount()));
        ((MyApplication) getApplication()).getMyClient().open(new AVIMClientCallback() {
            @Override
            public void done(AVIMClient client, AVIMException e) {
                if (e != null) {
                    Toast.makeText(MainActivity.this, R.string.main_login_fail_hint, Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(MainActivity.this, R.string.main_login_success_hint, Toast.LENGTH_SHORT).show();
                EventBus.getDefault().post(new UpdateConversationListEvent());
                fetchMyInfo();
            }
        });
    }


    /**
     * 退出登录
     */
    private void offLine() {
        if (((MyApplication) getApplication()).getMyClient() != null) {
            ((MyApplication) getApplication()).getMyClient().close(new AVIMClientCallback() {
                @Override
                public void done(AVIMClient avimClient, AVIMException e) {
                    Toast.makeText(MainActivity.this, R.string.main_offline_hint, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * 从服务器获取个人信息
     */
    private void fetchMyInfo() {
        AVQuery<AVObject> query = new AVQuery<>(Constants.TABLE_ACCOUNT);
        query.whereEqualTo(Constants.FIELD_ACCOUNT_TEL, ((MyApplication) getApplication()).getMyAccount());
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e != null) {
                    ToastUtils.showException(MainActivity.this);
                    return;
                }
                ((MyApplication) getApplication()).setMyName(list.get(0).getString(Constants.FIELD_ACCOUNT_NAME));
                String jsonInfo = list.get(0).getString(Constants.FIELD_ACCOUNT_INFO);
                mMyInfo = JSON.parseObject(jsonInfo, AccountInfoBean.class);
                updateTitleIcon();
                updateLeftData();

            }
        });

    }

    /**
     * 更新左侧菜单的数据
     */
    private void updateLeftData() {
        mTvName.setText(((MyApplication) getApplication()).getMyName());
        if (mMyInfo != null) {
            if (!TextUtils.isEmpty(mMyInfo.sign)) {
                mTvSign.setText(mMyInfo.sign);
            }
            ImageView ic = (ImageView) findViewById(R.id.iv_left_ic);
            if ("female".equals(mMyInfo.sex)) {
                ic.setImageResource(R.mipmap.user_female_ic);
            } else {
                ic.setImageResource(R.mipmap.user_male_ic);
            }
        }
    }

    /**
     * 更新标题栏上的图标
     */
    private void updateTitleIcon() {
        if (mMyInfo != null) {
            switch (mMyInfo.sex) {
                case "male":
                    mIvTitleIcon.setImageResource(R.mipmap.user_male_ic);
                    break;
                case "female":
                    mIvTitleIcon.setImageResource(R.mipmap.user_female_ic);
                    break;
                default:
                    mIvTitleIcon.setImageResource(R.mipmap.user_male_ic);
            }
        }
    }

    private void initView() {
        initDragLayout();
        initAppBar();
        initTab();
        initLeft();
    }

    private void initLeft() {
        mTvSign = (TextView) findViewById(R.id.tv_left_sign);
        mTvSign.setOnClickListener(this);
        mTvName = (TextView) findViewById(R.id.tv_left_name);
        RelativeLayout rlTop = (RelativeLayout) findViewById(R.id.rl_left_top);
        rlTop.setOnClickListener(this);
    }

    private void initAppBar() {
        mTvAppBarTitle = (TextView) findViewById(R.id.tv_bar_title);
        mIvTitleIcon = (ImageView) findViewById(R.id.iv_bar_ic);
        mIvTitleIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mDragLayout.open();
            }
        });
        mTvAdd = (TextView) findViewById(R.id.tv_main_add);
        mTvAdd.setOnClickListener(this);
        mTvAppBarTitle.setText(R.string.main_tab_conversation);
    }

    private void initDragLayout() {
        mDragLayout = (DragLayout) findViewById(R.id.dl_main_container);
        mDragLayout.setDragListener(new DragLayout.DragListener() {
            @Override
            public void onOpen() {
                mHasOpen = true;
            }

            @Override
            public void onClose() {
                mHasOpen = false;
                AnimUtils.startShakeAnimation(MainActivity.this, mIvTitleIcon);
            }

            @Override
            public void onDrag(float percent) {
                ViewHelper.setAlpha(mIvTitleIcon, 1 - percent);
            }
        });
    }

    private void initTab() {
        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.real_tabcontent);
        mTabHost.getTabWidget().setDividerDrawable(android.R.color.transparent);

        View indicator = View.inflate(this, R.layout.widget_tab_item, null);
        ((ImageView) indicator.findViewById(R.id.iv_tab_item_icon)).setImageResource(R.drawable.tab_msg_bg_selector);
        TabHost.TabSpec messageSpec = mTabHost.newTabSpec(TAG_CONVERSATION).setIndicator(indicator);
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
            case TAG_CONVERSATION:
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


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_main_add:
                goToAdd();
                break;
            case R.id.tv_left_sign:
                break;
            case R.id.rl_left_top:
                goToInfo();
                break;
            case R.id.btn_main_menu_cancel:
                mPwMainMenu.dismiss();
                break;
            case R.id.btn_main_menu_exit:
                mPwMainMenu.dismiss();
                showExitDialog();
                break;
            case R.id.btn_main_menu_help:
                mPwMainMenu.dismiss();
                break;
            case R.id.btn_main_menu_update:
                mPwMainMenu.dismiss();
                break;

            default:
        }
    }

    private void goToAdd() {
        Intent intent = new Intent(MainActivity.this, AddActivity.class);
        startActivity(intent);
    }

    private void goToInfo() {
        Intent intent = new Intent(MainActivity.this, InfoActivity.class)
                .putExtra("Account", ((MyApplication) getApplication()).getMyAccount())
                .putExtra("Name", ((MyApplication) getApplication()).getMyName());
        if (mMyInfo != null) {
            intent.putExtra("Info", mMyInfo);
        }
        startActivity(intent);
    }

    private void showNotification(String jsonContent) {
        MessageContentBean contentBean = JSON.parseObject(jsonContent, MessageContentBean.class);
        String content = contentBean.c;
        String account = contentBean.a.split("&")[0];
        String name = contentBean.a.split("&")[1];
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentText(name)
                .setContentTitle(name)
                .setContentText(content)
                .setTicker(name + getString(R.string.notification_ticker))
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(
                        MainActivity.this, 1, new Intent(MainActivity.this, ChatActivity.class)
                                .putExtra("ContactsName", name).putExtra("ContactsAccount", account),
                        PendingIntent.FLAG_CANCEL_CURRENT));
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        int id = Integer.valueOf(account.substring(0, 3) + account.substring(6, 10));
        if (mNotificationIds == null) {
            mNotificationIds = new ArrayList<>();
        }
        if (!mNotificationIds.contains(id)) {
            mNotificationIds.add(id);
        }
        manager.notify(id, builder.build());
    }

    private void cancelAllNotification() {
        if (mNotificationIds != null) {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            for (int id : mNotificationIds) {
                manager.cancel(id);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mHasOpen) {
            mDragLayout.close();
        } else {
            showExitDialog();
        }
    }

    private void showExitDialog() {
        new AlertDialog.Builder(this).setMessage(R.string.dialog_exit_hint)
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setPositiveButton(R.string.dialog_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).create().show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            showMainMenu();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showMainMenu() {
        LayoutInflater inflater = getLayoutInflater();
        View menuView = inflater.inflate(R.layout.widget_main_popup_menu, null);
        Button btnCancel = (Button) menuView.findViewById(R.id.btn_main_menu_cancel);
        Button btnExit = (Button) menuView.findViewById(R.id.btn_main_menu_exit);
        btnCancel.setOnClickListener(this);
        btnExit.setOnClickListener(this);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        double screenWidth = metrics.widthPixels * 0.9;

        mPwMainMenu = new PopupWindow(menuView, (int) screenWidth,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        mPwMainMenu.setFocusable(true);
        mPwMainMenu.setOutsideTouchable(true);
        mPwMainMenu.setBackgroundDrawable(getResources().getDrawable(
                android.R.color.transparent));
        mPwMainMenu.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams params = getWindow().getAttributes();
                params.alpha = 1.0f;
                getWindow().setAttributes(params);
            }
        });

        mPwMainMenu.showAtLocation(getWindow().getDecorView(),
                android.view.Gravity.BOTTOM, 0, 0);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.alpha = 0.5f;
        getWindow().setAttributes(params);
    }


    @Override
    protected void onDestroy() {
        cancelAllNotification();
        offLine();
        AVIMMessageManager.unregisterMessageHandler(AVIMMessage.class, new MyMessageHandler());
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public void onEvent(MessageNotificationEvent event) {
        String jsonContent = event.message.getContent();
        showNotification(jsonContent);
    }

}
