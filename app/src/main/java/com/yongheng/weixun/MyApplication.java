package com.yongheng.weixun;

import android.app.Activity;
import android.app.Application;

import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.im.v2.AVIMClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 张永恒 on 2015/12/23.
 * MyApplication
 */
public class MyApplication extends Application {

    private List<Activity> mActivityList;
    private String mMyAccount;
    private String mMyName;
    private AVIMClient mMyClient;

    @Override
    public void onCreate() {
        super.onCreate();
        AVOSCloud.initialize(this, "mUWwTuPRCqeka3BdfN5ryddx-gzGzoHsz", "uaRJndjyCsdJfQvOMr6wtDcr");

    }

    public String getMyAccount() {
        return mMyAccount;
    }

    public void setMyAccount(String mMyAccount) {
        this.mMyAccount = mMyAccount;
    }

    public String getMyName() {
        return mMyName;
    }

    public void setMyName(String mMyName) {
        this.mMyName = mMyName;
    }

    public AVIMClient getMyClient() {
        return mMyClient;
    }

    public void setMyClient(AVIMClient mMyClient) {
        this.mMyClient = mMyClient;
    }

    public void addActivity(Activity activity) {
        if (mActivityList == null) {
            mActivityList = new ArrayList<>();
        }
        mActivityList.add(activity);
    }

    public void removeActivity(Activity activity) {
        if (mActivityList == null) {
            mActivityList = new ArrayList<>();
        }
        mActivityList.remove(activity);
    }

    public void finishAllActivity() {
        if (mActivityList == null) {
            mActivityList = new ArrayList<>();
        }
        for (Activity a : mActivityList) {
            a.finish();
        }
        mActivityList.clear();

    }


}
