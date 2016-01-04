package com.yongheng.weixun.model;

/**
 * Created by 张永恒 on 2015/12/24.
 * 联系人的数据模型
 */
public class ContactsInfo implements Comparable<ContactsInfo> {
    public String name;
    public String account;
    public String sex;

    @Override
    public int compareTo(ContactsInfo another) {
        return name.compareTo(another.name);
    }
}
