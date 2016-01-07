package com.yongheng.weixun.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.SaveCallback;
import com.yongheng.weixun.Constants;
import com.yongheng.weixun.MyApplication;
import com.yongheng.weixun.R;
import com.yongheng.weixun.event.UpdateContactsListEvent;
import com.yongheng.weixun.model.AccountInfoBean;
import com.yongheng.weixun.utils.ServerUtils;
import com.yongheng.weixun.utils.ToastUtils;

import de.greenrobot.event.EventBus;

/**
 * Created by 张永恒 on 2015/12/27.
 * 账号设置界面的Activity
 */
public class SetupActivity extends Activity
        implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private EditText mEtName;
    private String mAccount;
    private String mPwd;
    private EditText mEtAge;
    private EditText mEtSign;
    private String mSex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        ((MyApplication) getApplication()).addActivity(this);
        Intent intent = getIntent();
        mAccount = intent.getStringExtra("Account");
        mPwd = intent.getStringExtra("Pwd");
        initView();
    }

    private void initView() {
        TextView tvConfirm = (TextView) findViewById(R.id.tv_setup_confirm);
        mEtName = (EditText) findViewById(R.id.et_setup_name);
        RadioGroup rgSex = (RadioGroup) findViewById(R.id.rg_setup_sex);
        mEtAge = (EditText) findViewById(R.id.et_setup_age);
        mEtSign = (EditText) findViewById(R.id.et_setup_sign);

        tvConfirm.setOnClickListener(this);
        rgSex.setOnCheckedChangeListener(this);
        rgSex.check(R.id.rb_setup_male);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_setup_confirm) {
            final String name = mEtName.getText().toString().trim();
            final String age = mEtAge.getText().toString().trim();
            final String sign = mEtSign.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                Toast.makeText(SetupActivity.this, R.string.setup_name_hint, Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(mSex)) {
                Toast.makeText(SetupActivity.this, R.string.setup_sex_hint, Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(age)) {
                Toast.makeText(SetupActivity.this, R.string.setup_age_hint, Toast.LENGTH_SHORT).show();
                return;
            }

            setup(name, age, sign);

        }
    }

    private void setup(String name, String age, String sign) {
        AccountInfoBean bean = new AccountInfoBean();
        bean.sex = mSex;
        bean.age = age;
        bean.sign = sign;
        String jsonInfo = JSON.toJSONString(bean);

        AVObject account = new AVObject(Constants.TABLE_ACCOUNT);
        account.put(Constants.FIELD_ACCOUNT_TEL, mAccount);
        account.put(Constants.FIELD_ACCOUNT_PWD, mPwd);
        account.put(Constants.FIELD_ACCOUNT_NAME, name);
        account.put(Constants.FIELD_ACCOUNT_INFO, jsonInfo);
        account.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if (e != null) {
                    ToastUtils.showException(SetupActivity.this);
                    return;
                }
                initContacts();
                Toast.makeText(SetupActivity.this, R.string.sign_in_success_hint, Toast.LENGTH_SHORT).show();
                goToMain();
            }
        });
    }

    private void initContacts() {
        ServerUtils.addContacts(SetupActivity.this, mAccount, Constants.ServiceAccount,
                new ServerUtils.ServerUtilsCallBack() {
                    @Override
                    public void onFinish() {
                        EventBus.getDefault().post(new UpdateContactsListEvent());
                    }

                    @Override
                    public void onError() {
                    }
                });

    }

    private void goToMain() {
        Intent intent = new Intent(SetupActivity.this, MainActivity.class)
                .putExtra("Account", mAccount);
        startActivity(intent);

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_setup_male:
                mSex = "male";
                break;
            case R.id.rb_setup_female:
                mSex = "female";
                break;
            default:
        }
    }

    @Override
    protected void onDestroy() {
        ((MyApplication) getApplication()).removeActivity(this);
        super.onDestroy();
    }

}
