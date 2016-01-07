package com.yongheng.weixun.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.yongheng.weixun.Constants;
import com.yongheng.weixun.MyApplication;
import com.yongheng.weixun.R;
import com.yongheng.weixun.event.AddContactsEvent;
import com.yongheng.weixun.event.UpdateContactsListEvent;
import com.yongheng.weixun.utils.ServerUtils;
import com.yongheng.weixun.utils.ToastUtils;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by 张永恒 on 2015/12/27.
 * 添加联系人界面的Activity
 */
public class AddActivity extends Activity implements View.OnClickListener {

    private EditText mEtAccount;
    private String mMyAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        EventBus.getDefault().register(this);
        mMyAccount = ((MyApplication) getApplication()).getMyAccount();
        initView();
    }

    private void initView() {
        mEtAccount = (EditText) findViewById(R.id.et_add_account);
        ImageView ivAdd = (ImageView) findViewById(R.id.iv_add_add);
        ivAdd.setOnClickListener(this);
        TextView tvReturn = (TextView) findViewById(R.id.tv_add_return);
        tvReturn.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_add_add) {
            String account = mEtAccount.getText().toString().trim();
            if (TextUtils.isEmpty(account)) {
                Toast.makeText(AddActivity.this, R.string.add_no_input_hint, Toast.LENGTH_SHORT).show();
                return;
            }
            add(account);
        }
        if (v.getId() == R.id.tv_add_return) {
            finish();
        }
    }

    /**
     * 添加联系人
     *
     * @param account 要添加的联系人
     */
    private void add(final String account) {
        AVQuery<AVObject> query = new AVQuery<>(Constants.TABLE_ACCOUNT);
        query.whereEqualTo(Constants.FIELD_ACCOUNT_TEL, account);
        if (account.equals(mMyAccount)) {
            Toast.makeText(AddActivity.this, R.string.add_self_hint, Toast.LENGTH_SHORT).show();
            return;
        }
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e != null) {
                    Toast.makeText(AddActivity.this, R.string.add_error_hint, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (list.size() > 0) {
                    mEtAccount.setText("");
                    goToInfo(account);

                } else {
                    Toast.makeText(AddActivity.this, R.string.add_no_found_hint, Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void goToInfo(String account) {
        Intent intent = new Intent(AddActivity.this, InfoActivity.class)
                .putExtra("Account", account)
                .putExtra("From", InfoActivity.FROM_ADD);
        startActivity(intent);

    }


    public void onEvent(AddContactsEvent event) {
        ServerUtils.addContacts(AddActivity.this, mMyAccount, event.account,
                new ServerUtils.ServerUtilsCallBack() {
                    @Override
                    public void onFinish() {
                        Toast.makeText(AddActivity.this, R.string.add_success_hint, Toast.LENGTH_SHORT).show();
                        EventBus.getDefault().post(new UpdateContactsListEvent());
                    }

                    @Override
                    public void onError() {
                        ToastUtils.showException(AddActivity.this);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
