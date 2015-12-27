package com.yongheng.weixun.event;

import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMMessage;

/**
 * Created by 张永恒 on 2015/12/23.
 * 文本消息事件类
 */
public class IMTextEvent {
    public AVIMMessage message;
    public AVIMConversation conversation;
}
