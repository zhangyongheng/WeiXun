package com.yongheng.weixun;

import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.AVIMMessageHandler;
import com.yongheng.weixun.event.MessageEvent;
import com.yongheng.weixun.event.MessageNotificationEvent;

import de.greenrobot.event.EventBus;

/**
 * Created by 张永恒 on 2015/12/23.
 * 消息处理器
 */
public class MyMessageHandler extends AVIMMessageHandler {
    @Override
    public void onMessage(AVIMMessage message, AVIMConversation conversation, AVIMClient client) {
        MessageEvent imTextEvent = new MessageEvent();
        imTextEvent.message = message;
        imTextEvent.conversation = conversation;
        EventBus.getDefault().post(imTextEvent);
        MessageNotificationEvent notificationEvent = new MessageNotificationEvent();
        notificationEvent.message = message;
        EventBus.getDefault().post(notificationEvent);

    }

    public void onMessageReceipt(AVIMMessage message, AVIMConversation conversation, AVIMClient client) {
    }
}