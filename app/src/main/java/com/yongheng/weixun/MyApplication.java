package com.yongheng.weixun;

import android.app.Application;

import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.im.v2.AVIMMessageManager;
import com.yongheng.weixun.handler.MyMessageHandler;

/**
 * Created by 张永恒 on 2015/12/23.
 * MyApplication
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AVOSCloud.initialize(this, "mUWwTuPRCqeka3BdfN5ryddx-gzGzoHsz", "uaRJndjyCsdJfQvOMr6wtDcr");
        AVIMMessageManager.registerDefaultMessageHandler(new MyMessageHandler());

    }

}
