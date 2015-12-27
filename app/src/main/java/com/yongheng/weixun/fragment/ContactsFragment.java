package com.yongheng.weixun.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.yongheng.weixun.Constants;
import com.yongheng.weixun.R;
import com.yongheng.weixun.activity.ChatActivity;
import com.yongheng.weixun.activity.MainActivity;
import com.yongheng.weixun.adapter.ContactListAdapter;
import com.yongheng.weixun.model.ContactsInfo;
import com.yongheng.weixun.widget.IndexableListView;

import java.util.Collections;
import java.util.List;

/**
 * Created by 张永恒 on 2015/12/22.
 * 联系人Tab页的Fragment
 */
public class ContactsFragment extends Fragment implements AdapterView.OnItemClickListener {

    private View mRootView;

    private IndexableListView mLvContactList;
    private ContactListAdapter mContactListAdapter;

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
        contactsQuery.whereEqualTo(Constants.FIELD_CONTACTS_NAME, ((MainActivity) getContext()).getMyName());
        contactsQuery.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                if (e != null) {
                    return;
                }
                for (AVObject object : list) {
                    String contactsName = object.getString(Constants.FIELD_CONTACTS_CONTACTS);
                    ContactsInfo contactsInfo = new ContactsInfo();
                    contactsInfo.name = contactsName;
                    List<ContactsInfo> contactsInfoList = mContactListAdapter.getContactsInfoList();
                    contactsInfoList.add(contactsInfo);
                    Collections.sort(contactsInfoList);
                    mContactListAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void initView() {
        mLvContactList = (IndexableListView) mRootView.findViewById(R.id.ilv_contact_list);
        mContactListAdapter = new ContactListAdapter(getContext());
        mLvContactList.setAdapter(mContactListAdapter);
        mLvContactList.setOnItemClickListener(this);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        String contactsName = mContactListAdapter.getContactsInfoList().get(position).name;
        goToChat(contactsName);
    }

    private void goToChat(String contactsName) {
        Intent intent = new Intent(getContext(), ChatActivity.class);
        ChatActivity.mMyClient = ((MainActivity) getContext()).getMyClient();
        ;
        ChatActivity.mMyName = ((MainActivity) getContext()).getMyName();
        ;
        ChatActivity.mContactsName = contactsName;
        startActivity(intent);
    }


}
