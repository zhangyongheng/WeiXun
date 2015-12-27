package com.yongheng.weixun.model;

/**
 * Created by 张永恒 on 2015/12/23.
 * 消息的数据模型
 */
public class MessageInfo {
    public int type;
    public String content;

    public static final int TYPE_OUT = 0;
    public static final int TYPE_IN = 1;

}
