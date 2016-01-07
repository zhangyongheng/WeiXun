package com.yongheng.weixun.event;

/**
 * Created by 张永恒 on 2016/1/6.
 * 更新会话列表中某回话显示的最后一条消息的事件
 */
public class UpdateConversationInfoEvent {
    public String account;
    public String lastMsg;
}
