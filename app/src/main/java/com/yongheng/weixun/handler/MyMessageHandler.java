package com.yongheng.weixun.handler;

import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.AVIMMessageHandler;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;
import com.yongheng.weixun.event.IMTextEvent;

import de.greenrobot.event.EventBus;

/**
 * Created by 张永恒 on 2015/12/23.
 * 消息处理器
 */
public class MyMessageHandler extends AVIMMessageHandler {
    @Override
    public void onMessage(AVIMMessage message, AVIMConversation conversation, AVIMClient client) {
        if (message instanceof AVIMTextMessage) {
            IMTextEvent event = new IMTextEvent();
            event.message = message;
            event.conversation = conversation;
            //EventBus发布一条事件
            EventBus.getDefault().post(event);
        }

    }

    public void onMessageReceipt(AVIMMessage message, AVIMConversation conversation, AVIMClient client) {

    }
}