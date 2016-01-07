package com.yongheng.weixun.utils;

import android.content.Context;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.DeleteCallback;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.SaveCallback;
import com.yongheng.weixun.Constants;
import com.yongheng.weixun.R;

import java.util.Arrays;
import java.util.List;

/**
 * Created by 张永恒 on 2016/1/6.
 * 操作服务器端的工具类
 */
public class ServerUtils {

    /**
     * 添加联系人
     *
     * @param context
     * @param myAccount
     * @param account
     * @param callBack
     */
    public static void addContacts(Context context, String myAccount, String account, ServerUtilsCallBack callBack) {
        addContactsFromSelf(context, myAccount, account, callBack);
        addContactsFromOther(context, myAccount, account);
    }

    private static void addContactsFromSelf(final Context context, final String myAccount,
                                            final String account, final ServerUtilsCallBack callBack) {
        final AVObject object = new AVObject(Constants.TABLE_CONTACTS);
        AVQuery<AVObject> query = new AVQuery<>(Constants.TABLE_CONTACTS);
        query.whereEqualTo(Constants.FIELD_CONTACTS_NAME, myAccount);
        query.whereEqualTo(Constants.FIELD_CONTACTS_CONTACTS, account);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e != null) {
                    callBack.onError();
                    return;
                }
                if (list.size() > 0) {
                    Toast.makeText(context, R.string.add_already_exist_hint, Toast.LENGTH_SHORT).show();
                } else {
                    object.put(Constants.FIELD_CONTACTS_NAME, myAccount);
                    object.put(Constants.FIELD_CONTACTS_CONTACTS, account);
                    object.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(AVException e) {
                            if (e != null) {
                                ToastUtils.showException(context);
                                return;
                            }
                            callBack.onFinish();
                        }
                    });

                }

            }
        });
    }

    private static void addContactsFromOther(final Context context, final String myAccount,
                                             final String account) {
        final AVObject object = new AVObject(Constants.TABLE_CONTACTS);
        AVQuery<AVObject> query = new AVQuery<>(Constants.TABLE_CONTACTS);
        query.whereEqualTo(Constants.FIELD_CONTACTS_NAME, account);
        query.whereEqualTo(Constants.FIELD_CONTACTS_CONTACTS, myAccount);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e != null) {
                    return;
                }
                if (list.size() == 0) {
                    object.put(Constants.FIELD_CONTACTS_NAME, account);
                    object.put(Constants.FIELD_CONTACTS_CONTACTS, myAccount);
                    object.saveInBackground();
                }
            }
        });
    }

    /**
     * 删除自己联系人列表中的联系人
     *
     * @param myAccount
     * @param account
     * @param callBack
     */
    public static void deleteContactsFromSelf(String myAccount, String account, final ServerUtilsCallBack callBack) {
        AVQuery<AVObject> query = new AVQuery<>(Constants.TABLE_CONTACTS);
        query.whereEqualTo(Constants.FIELD_CONTACTS_NAME, myAccount);
        query.whereEqualTo(Constants.FIELD_CONTACTS_CONTACTS, account);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e != null) {
                    callBack.onError();
                    return;
                }
                for (AVObject object : list) {
                    object.deleteInBackground(new DeleteCallback() {
                        @Override
                        public void done(AVException e) {
                            if (e != null) {
                                callBack.onError();
                                return;
                            }
                            callBack.onFinish();
                        }
                    });
                }
            }
        });
    }

    /**
     * 删除对方联系人列表中的联系人
     *
     * @param myAccount
     * @param account
     * @param callBack
     */
    public static void deleteContactsFromOther(String myAccount, String account, final ServerUtilsCallBack callBack) {
        AVQuery<AVObject> query = new AVQuery<>(Constants.TABLE_CONTACTS);
        query.whereEqualTo(Constants.FIELD_CONTACTS_NAME, account);
        query.whereEqualTo(Constants.FIELD_CONTACTS_CONTACTS, myAccount);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e != null) {
                    callBack.onError();
                    return;
                }
                for (AVObject object : list) {
                    object.deleteInBackground();
                }
                callBack.onFinish();
            }
        });
    }

    /**
     * 删除会话
     *
     * @param myAccount
     * @param account
     * @param callBack
     */
    public static void deleteConversation(String myAccount, String account, final ServerUtilsCallBack callBack) {
        AVQuery<AVObject> query = new AVQuery<>(Constants.TABLE_CONVERSATION);
        query.whereContainedIn(Constants.FIELD_CONVERSATION_MEMBER,
                Arrays.asList(myAccount, account));
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e != null) {
                    callBack.onError();
                    return;
                }
                for (AVObject object : list) {
                    object.deleteInBackground();
                }
                callBack.onError();
            }
        });

    }

    public interface ServerUtilsCallBack {
        void onFinish();

        void onError();
    }

}
