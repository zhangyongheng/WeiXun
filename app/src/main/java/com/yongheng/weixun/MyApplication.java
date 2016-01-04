package com.yongheng.weixun;

import android.app.Activity;
import android.app.Application;

import com.avos.avoscloud.AVOSCloud;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 张永恒 on 2015/12/23.
 * MyApplication
 */
public class MyApplication extends Application {

    private List<Activity> mActivityList;

    @Override
    public void onCreate() {
        super.onCreate();
        AVOSCloud.initialize(this, "mUWwTuPRCqeka3BdfN5ryddx-gzGzoHsz", "uaRJndjyCsdJfQvOMr6wtDcr");

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
