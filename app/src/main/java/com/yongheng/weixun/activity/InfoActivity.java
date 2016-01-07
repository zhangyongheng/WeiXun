package com.yongheng.weixun.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.yongheng.weixun.Constants;
import com.yongheng.weixun.R;
import com.yongheng.weixun.event.AddContactsEvent;
import com.yongheng.weixun.event.DeleteContactsEvent;
import com.yongheng.weixun.event.RemoveConversationEvent;
import com.yongheng.weixun.event.StartChatEvent;
import com.yongheng.weixun.model.AccountInfoBean;
import com.yongheng.weixun.utils.ToastUtils;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by 张永恒 on 2015/12/30.
 * 联系人详细信息界面的Activity
 */
public class InfoActivity extends Activity implements View.OnClickListener {

    public final static String FROM_SELF = "self";
    public final static String FROM_CONTACTS = "contacts";
    public final static String FROM_ADD = "add";

    private AccountInfoBean mInfoBean;
    private String mName;
    private String mAccount;
    private TextView mTvName;
    private TextView mTvSex;
    private TextView mTvAge;
    private TextView mTvAccount;
    private String mFrom;
    private ImageView mIvIcon;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        Intent intent = getIntent();
        mAccount = intent.getStringExtra("Account");
        mName = intent.getStringExtra("Name");
        mInfoBean = (AccountInfoBean) intent.getSerializableExtra("Info");
        mFrom = intent.getStringExtra("From");

        initView();
        initData();

    }

    private void initData() {
        mTvAccount.setText(getString(R.string.info_account) + mAccount);
        if (!TextUtils.isEmpty(mName)) {
            mTvName.setText(mName);
        } else {
            fetchName();
        }
        if (mInfoBean != null) {
            showInfo();
        } else {
            mTvSex.setVisibility(View.INVISIBLE);
            mTvAge.setVisibility(View.INVISIBLE);
            fetchInfo();
        }

    }

    /**
     * 从服务器获取该账号的昵称
     */
    private void fetchName() {
        AVQuery<AVObject> query = new AVQuery<>(Constants.TABLE_ACCOUNT);
        query.whereEqualTo(Constants.FIELD_ACCOUNT_TEL, mAccount);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e != null) {
                    ToastUtils.showException(InfoActivity.this);
                    return;
                }
                mName = list.get(0).getString(Constants.FIELD_ACCOUNT_NAME);
                mTvName.setText(mName);
            }
        });
    }

    private void showInfo() {
        switch (mInfoBean.sex) {
            case "male":
                mTvSex.setText(R.string.info_male);
                mIvIcon.setImageResource(R.mipmap.user_male_ic);
                break;
            case "female":
                mTvSex.setText(R.string.info_female);
                mIvIcon.setImageResource(R.mipmap.user_female_ic);
                break;
            default:
        }
        mTvAge.setText(mInfoBean.age + getString(R.string.info_age));
    }

    /**
     * 从服务器获取该帐号的个人信息
     */
    private void fetchInfo() {
        AVQuery<AVObject> query = new AVQuery<>(Constants.TABLE_ACCOUNT);
        query.whereEqualTo(Constants.FIELD_ACCOUNT_TEL, mAccount);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e != null) {
                    ToastUtils.showException(InfoActivity.this);
                    return;
                }
                if (list.size() == 0) {
                    return;
                }
                String jsonInfo = list.get(0).getString(Constants.FIELD_ACCOUNT_INFO);
                if (!TextUtils.isEmpty(jsonInfo)) {
                    mInfoBean = JSON.parseObject(jsonInfo, AccountInfoBean.class);
                    if (mInfoBean != null) {
                        showInfo();
                        mTvSex.setVisibility(View.VISIBLE);
                        mTvAge.setVisibility(View.VISIBLE);
                    }
                }

            }
        });


    }

    private void initView() {
        TextView tvReturn = (TextView) findViewById(R.id.tv_info_return);
        mTvName = (TextView) findViewById(R.id.tv_info_name);
        mTvSex = (TextView) findViewById(R.id.tv_info_sex);
        mTvAge = (TextView) findViewById(R.id.tv_info_age);
        mTvAccount = (TextView) findViewById(R.id.tv_info_account);
        mIvIcon = (ImageView) findViewById(R.id.iv_info_ic);
        tvReturn.setOnClickListener(this);
        if (FROM_CONTACTS.equals(mFrom)) {
            TextView tvChat = (TextView) findViewById(R.id.tv_info_chat);
            tvChat.setVisibility(View.VISIBLE);
            tvChat.setOnClickListener(this);
            TextView tvMore = (TextView) findViewById(R.id.tv_info_more);
            tvMore.setVisibility(View.VISIBLE);
            tvMore.setOnClickListener(this);
        }
        if (FROM_ADD.equals(mFrom)) {
            TextView tvAdd = (TextView) findViewById(R.id.tv_info_add);
            tvAdd.setVisibility(View.VISIBLE);
            tvAdd.setOnClickListener(this);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_info_return:
                finish();
                break;
            case R.id.tv_info_chat:
                StartChatEvent chatEvent = new StartChatEvent();
                chatEvent.contactsAccount = mAccount;
                chatEvent.contactsName = mName;
                EventBus.getDefault().post(chatEvent);
                finish();
                break;
            case R.id.tv_info_add:
                AddContactsEvent addEvent = new AddContactsEvent();
                addEvent.account = mAccount;
                EventBus.getDefault().post(addEvent);
                finish();
                break;
            case R.id.tv_info_more:
                showMoreMenu(v);
                break;

            default:
        }
    }

    /**
     * 显示“更多”菜单
     *
     * @param v PopupMenu的父容器
     */
    private void showMoreMenu(View v) {
        PopupMenu menu = new PopupMenu(InfoActivity.this, v);
        menu.getMenuInflater().inflate(R.menu.menu_info_more, menu.getMenu());
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_del_contacts:
                        showDelContactsDialog();
                        break;
                    default:
                }
                return true;
            }
        });
        menu.show();

    }

    private void showDelContactsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(InfoActivity.this);
        builder.setMessage(R.string.dialog_delete_contacts_hint)
                .setCancelable(false)
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setPositiveButton(R.string.dialog_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DeleteContactsEvent delEvent = new DeleteContactsEvent();
                        delEvent.account = mAccount;
                        EventBus.getDefault().post(delEvent);
                        RemoveConversationEvent remEvent = new RemoveConversationEvent();
                        remEvent.account = mAccount;
                        EventBus.getDefault().post(remEvent);
                        finish();
                    }
                });
        builder.create().show();
    }

}
