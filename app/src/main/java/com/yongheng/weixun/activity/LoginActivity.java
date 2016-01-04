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
import com.yongheng.weixun.utils.ToastUtils;

import java.util.List;

/**
 * Created by 张永恒 on 2015/12/24.
 * 登录界面的Activity
 */
public class LoginActivity extends Activity implements View.OnClickListener {

    private EditText mEtAccount;
    private EditText mEtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ((MyApplication) getApplication()).addActivity(this);
        initView();
        createShortcut();
    }

    /**
     * 创建桌面快捷图标
     */
    private void createShortcut() {
        Intent addShortcutIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        addShortcutIntent.putExtra("duplicate", false);
        addShortcutIntent.putExtra(
                Intent.EXTRA_SHORTCUT_NAME,getResources().getString(R.string.app_name));
        addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(this,
                        R.mipmap.ic_launcher));
        Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
        launcherIntent.setClass(this, SplashActivity.class);
        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        addShortcutIntent
                .putExtra(Intent.EXTRA_SHORTCUT_INTENT, launcherIntent);

        sendBroadcast(addShortcutIntent);
    }

    private void initView() {
        mEtAccount = (EditText) findViewById(R.id.et_login_tel);
        mEtPassword = (EditText) findViewById(R.id.et_login_pwd);
        Button btnLogin = (Button) findViewById(R.id.btn_login_login);
        TextView tvSignIn = (TextView) findViewById(R.id.tv_login_sign);

        btnLogin.setOnClickListener(this);
        tvSignIn.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login_login:
                String account = mEtAccount.getText().toString().trim();
                if (TextUtils.isEmpty(account)) {
                    Toast.makeText(LoginActivity.this, R.string.login_no_input_account_hint, Toast.LENGTH_SHORT).show();
                    return;
                }
                String pwd = mEtPassword.getText().toString().trim();
                if (TextUtils.isEmpty(pwd)) {
                    Toast.makeText(LoginActivity.this, R.string.login_no_input_pwd_hint, Toast.LENGTH_SHORT).show();
                    return;
                }
                login(account, pwd);
                break;

            case R.id.tv_login_sign:
                goToSignIn();
                break;
        }


    }

    /**
     * 将输入的账号和密码与服务器中的数据做匹配，匹配成功进入主界面
     *
     * @param account 用户输入的账号
     * @param pwd     用户输入的密码
     */
    private void login(final String account, final String pwd) {
        AVQuery<AVObject> query = new AVQuery<>(Constants.TABLE_ACCOUNT);   //查询服务器端的账号表
        query.whereEqualTo(Constants.FIELD_ACCOUNT_TEL, account);  //查询条件为账号名
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e != null) {
                    ToastUtils.showException(LoginActivity.this);
                    return;
                }
                if (list.size() != 0) {
                    if (pwd.equals(list.get(0).getString(Constants.FIELD_ACCOUNT_PWD))) {
                        goToMain(account);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, R.string.login_pwd_error_hint, Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(LoginActivity.this, R.string.login_signin_hint, Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void goToMain(String account) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("Account", account);
        startActivity(intent);
    }

    private void goToSignIn() {
        Intent intent = new Intent(LoginActivity.this, SignInActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        ((MyApplication) getApplication()).removeActivity(this);
        super.onDestroy();
    }

}
