package com.yongheng.weixun.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.yongheng.weixun.Constants;
import com.yongheng.weixun.MyApplication;
import com.yongheng.weixun.R;
import com.yongheng.weixun.activity.ChatActivity;
import com.yongheng.weixun.activity.InfoActivity;
import com.yongheng.weixun.adapter.ContactListAdapter;
import com.yongheng.weixun.event.DeleteContactsEvent;
import com.yongheng.weixun.event.StartChatEvent;
import com.yongheng.weixun.event.UpdateContactsListEvent;
import com.yongheng.weixun.model.AccountInfoBean;
import com.yongheng.weixun.model.ContactsInfo;
import com.yongheng.weixun.utils.ServerUtils;
import com.yongheng.weixun.utils.ToastUtils;
import com.yongheng.weixun.widget.IndexableListView;

import java.util.Collections;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by 张永恒 on 2015/12/22.
 * 联系人Tab页的Fragment
 */
public class ContactsFragment extends Fragment implements AdapterView.OnItemClickListener {

    private View mRootView;

    private IndexableListView mLvContactList;
    private ContactListAdapter mContactListAdapter;
    private ProgressBar mProgressBar;
    private boolean mIsLastContacts;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_contact, null);
        }
        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mLvContactList == null) {
            initView();
            initData();
        }
    }

    private void initData() {
        updateContactsList();

    }

    /**
     * 查询服务器更新联系人列表
     */
    private void updateContactsList() {
        AVQuery<AVObject> contactsQuery = new AVQuery<>(Constants.TABLE_CONTACTS);
        contactsQuery.whereEqualTo(Constants.FIELD_CONTACTS_NAME, ((MyApplication) getActivity().getApplication()).getMyAccount());
        contactsQuery.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(final List<AVObject> list, AVException e) {
                if (e != null) {
                    ToastUtils.showException(getContext());
                    return;
                }
                final List<ContactsInfo> contactsInfoList = mContactListAdapter.getContactsInfoList();
                mIsLastContacts = false;
                contactsInfoList.clear();
                if (list.size() == 0) {
                    mProgressBar.setVisibility(View.GONE);
                    mContactListAdapter.notifyDataSetChanged();
                }
                for (int i = 0; i < list.size(); i++) {
                    if (i == list.size() - 1) {
                        mIsLastContacts = true;
                    }
                    final ContactsInfo contactsInfo = new ContactsInfo();
                    contactsInfo.account = list.get(i).getString(Constants.FIELD_CONTACTS_CONTACTS);
                    contactsInfo.name = contactsInfo.account;
                    final String[] contactsName = new String[]{contactsInfo.account};
                    AVQuery<AVObject> accountQuery = new AVQuery<>(Constants.TABLE_ACCOUNT);
                    accountQuery.whereEqualTo(Constants.FIELD_ACCOUNT_TEL, contactsInfo.account);
                    accountQuery.findInBackground(new FindCallback<AVObject>() {
                        @Override
                        public void done(List<AVObject> list2, AVException e) {
                            if (e != null) {
                                return;
                            }
                            if (list2.size() > 0) {
                                contactsName[0] = list2.get(0).getString(Constants.FIELD_ACCOUNT_NAME);
                                contactsInfo.name = contactsName[0];
                                String jsonInfo = list2.get(0).getString("info");
                                AccountInfoBean info = JSON.parseObject(jsonInfo, AccountInfoBean.class);
                                contactsInfo.sex = info.sex;
                            }
                            contactsInfoList.add(contactsInfo);

                            if (mIsLastContacts) {
                                Collections.sort(contactsInfoList);
                                mContactListAdapter.notifyDataSetChanged();
                                mProgressBar.setVisibility(View.GONE);
                            }

                        }
                    });
                }
            }
        });
    }

    private void initView() {
        mLvContactList = (IndexableListView) mRootView.findViewById(R.id.ilv_contact_list);
        mContactListAdapter = new ContactListAdapter(getContext());
        mLvContactList.setAdapter(mContactListAdapter);
        mLvContactList.setOnItemClickListener(this);
        mProgressBar = (ProgressBar) mRootView.findViewById(R.id.pb_contacts_progress);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        String contactsName = mContactListAdapter.getContactsInfoList().get(position).name;
        String contactsAccount = mContactListAdapter.getContactsInfoList().get(position).account;
        goToInfo(contactsAccount, contactsName);
    }

    private void goToChat(String contactsAccount, String contactsName) {
        Intent intent = new Intent(getContext(), ChatActivity.class)
                .putExtra("MyAccount", ((MyApplication) getActivity().getApplication()).getMyAccount())
                .putExtra("ContactsAccount", contactsAccount)
                .putExtra("ContactsName", contactsName);
        startActivity(intent);
    }

    private void goToInfo(String contactsAccount, String contactsName) {
        Intent intent = new Intent(getContext(), InfoActivity.class)
                .putExtra("Account", contactsAccount)
                .putExtra("Name", contactsName)
                .putExtra("From", InfoActivity.FROM_CONTACTS);
        startActivity(intent);
    }

    private void deleteContacts(String account) {
        ServerUtils.deleteContactsFromSelf(((MyApplication) getActivity().getApplication()).getMyAccount(),
                account, new ServerUtils.ServerUtilsCallBack() {
                    @Override
                    public void onFinish() {
                        Toast.makeText(getContext(), R.string.contacts_delete_success_hint, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError() {
                        Toast.makeText(getContext(), R.string.contacts_delete_fail_hint, Toast.LENGTH_SHORT).show();
                        updateContactsList();
                    }
                });

        ServerUtils.deleteContactsFromOther(((MyApplication) getActivity().getApplication()).getMyAccount(),
                account, new ServerUtils.ServerUtilsCallBack() {
                    @Override
                    public void onFinish() {
                    }

                    @Override
                    public void onError() {
                    }
                });

        ServerUtils.deleteConversation(((MyApplication) getActivity().getApplication()).getMyAccount(),
                account, new ServerUtils.ServerUtilsCallBack() {
                    @Override
                    public void onFinish() {
                    }

                    @Override
                    public void onError() {
                    }
                });
    }


    public void onEvent(UpdateContactsListEvent event) {
        updateContactsList();
    }

    public void onEvent(StartChatEvent event) {
        goToChat(event.contactsAccount, event.contactsName);
    }

    public void onEvent(DeleteContactsEvent event) {
        deleteContacts(event.account);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
