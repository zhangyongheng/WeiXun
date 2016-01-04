package com.yongheng.weixun.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.yongheng.weixun.R;
import com.yongheng.weixun.model.ContactsInfo;
import com.yongheng.weixun.utils.StringMatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 张永恒 on 2015/12/24.
 * 联系人列表的适配器
 */
public class ContactListAdapter extends BaseAdapter implements SectionIndexer {

    private String mSections = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private ArrayList<ContactsInfo> mContactsInfoList;
    Context mContext;

    public ContactListAdapter(Context context) {
        mContext = context;
    }

    public List<ContactsInfo> getContactsInfoList() {
        if (mContactsInfoList == null) {
            mContactsInfoList = new ArrayList<>();
        }
        return mContactsInfoList;
    }

    @Override
    public int getPositionForSection(int section) {
        for (int i = section; i >= 0; i--) {
            for (int j = 0; j < getCount(); j++) {
                if (i == 0) {
                    for (int k = 0; k <= 9; k++) {
                        if (StringMatcher.match(String.valueOf(
                                mContactsInfoList.get(j).name.charAt(0)), String.valueOf(k)))
                            return j;
                    }
                } else {
                    if (StringMatcher.match(String.valueOf(
                            mContactsInfoList.get(j).name.charAt(0)), String.valueOf(mSections.charAt(i))))
                        return j;
                }
            }
        }
        return 0;
    }

    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }

    @Override
    public Object[] getSections() {
        String[] sections = new String[mSections.length()];
        for (int i = 0; i < mSections.length(); i++)
            sections[i] = String.valueOf(mSections.charAt(i));
        return sections;
    }

    @Override
    public int getCount() {
        if (mContactsInfoList != null) {
            return mContactsInfoList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return mContactsInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHold viewHold;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.list_item_contact, null);
            viewHold = new ViewHold();
            viewHold.name = (TextView) convertView.findViewById(R.id.tv_item_contacts_name);
            viewHold.ic = (ImageView) convertView.findViewById(R.id.iv_contacts_ic);
            convertView.setTag(viewHold);

        } else {
            viewHold = (ViewHold) convertView.getTag();
        }

        viewHold.name.setText(mContactsInfoList.get(position).name);
        switch (mContactsInfoList.get(position).sex) {
            case "female":
                viewHold.ic.setImageResource(R.mipmap.user_female_ic);
                break;
            case "male":
                viewHold.ic.setImageResource(R.mipmap.user_male_ic);
                break;
            default:
                viewHold.ic.setImageResource(R.mipmap.user_male_ic);
        }

        return convertView;
    }

    class ViewHold {
        public TextView name;
        public ImageView ic;
    }
}
