package com.yongheng.weixun.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.yongheng.weixun.Constants;
import com.yongheng.weixun.MyApplication;
import com.yongheng.weixun.R;

import java.util.List;

/**
 * Created by 张永恒 on 2015/12/27.
 * 注册界面的Activity
 */
public class SignInActivity extends Activity implements View.OnClickListener {

    private EditText mEtPwd;
    private EditText mEtTel;
    private EditText mEtComfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        ((MyApplication) getApplication()).addActivity(this);
        initView();
    }


    private void initView() {
        mEtTel = (EditText) findViewById(R.id.et_signin_tel);
        mEtPwd = (EditText) findViewById(R.id.et_signin_pwd);
        mEtComfirm = (EditText) findViewById(R.id.et_signin_confirm);
        Button btnSignIn = (Button) findViewById(R.id.btn_signin_signin);
        btnSignIn.setOnClickListener(this);
        TextView tvReturn = (TextView) findViewById(R.id.tv_sign_in_return);
        tvReturn.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_signin_signin) {
            String tel = mEtTel.getText().toString().trim().replace(" ", "");
            if (TextUtils.isEmpty(tel)) {
                Toast.makeText(SignInActivity.this, R.string.sign_in_no_input_account_hint, Toast.LENGTH_SHORT).show();
                return;
            }
            if (tel.length() != 11) {
                Toast.makeText(SignInActivity.this, R.string.sign_in_error_input_account_hint, Toast.LENGTH_SHORT).show();
                return;
            }
            String pwd = mEtPwd.getText().toString().trim().replace(" ", "");
            if (TextUtils.isEmpty(pwd)) {
                Toast.makeText(SignInActivity.this, R.string.sign_in_no_input_pwd_hint, Toast.LENGTH_SHORT).show();
                return;
            }
            String confirmPwd = mEtComfirm.getText().toString().trim().replace(" ", "");
            if (TextUtils.isEmpty(confirmPwd)) {
                Toast.makeText(SignInActivity.this, R.string.sign_in_no_input_confirm_hint, Toast.LENGTH_SHORT).show();
                return;
            }
            if (!confirmPwd.equals(pwd)) {
                Toast.makeText(SignInActivity.this, R.string.sign_in_error_confirm_hint, Toast.LENGTH_SHORT).show();
                return;
            }

            signIn(tel, pwd);
        }
        if (v.getId() == R.id.tv_sign_in_return) {
            finish();
        }
    }

    private void signIn(final String tel, final String pwd) {
        AVQuery<AVObject> query = new AVQuery<>(Constants.TABLE_ACCOUNT);
        query.whereEqualTo(Constants.FIELD_ACCOUNT_TEL, tel);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e != null) {
                    Toast.makeText(SignInActivity.this, R.string.sign_in_fail_hint, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (list.size() > 0) {
                    Toast.makeText(SignInActivity.this, R.string.sign_in_signed_hint, Toast.LENGTH_SHORT).show();
                    return;
                }
                goToSetup(tel, pwd);
            }
        });

    }

    private void goToSetup(String account, String pwd) {
        Intent intent = new Intent(SignInActivity.this, SetupActivity.class)
                .putExtra("Account", account)
                .putExtra("Pwd", pwd);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        ((MyApplication) getApplication()).removeActivity(this);
        super.onDestroy();
    }

}
