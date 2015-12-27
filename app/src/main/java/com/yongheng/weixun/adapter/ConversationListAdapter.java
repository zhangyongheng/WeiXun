package com.yongheng.weixun.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yongheng.weixun.R;
import com.yongheng.weixun.model.ConversationInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 张永恒 on 2015/12/22.
 * 消息界面会话列表的适配器
 */
public class ConversationListAdapter extends BaseAdapter {

    Context mContext;
    List<ConversationInfo> mConversationList;

    public ConversationListAdapter(Context context) {
        mContext = context;
    }

    public List<ConversationInfo> getConversationList() {
        if (mConversationList == null) {
            mConversationList = new ArrayList<>();
        }
        return mConversationList;
    }

    @Override
    public int getCount() {
        if (mConversationList != null) {
            return mConversationList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return mConversationList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHold viewHold;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.widget_list_item_conversation, null);
            viewHold = new ViewHold();
            viewHold.icon = (ImageView) convertView.findViewById(R.id.iv_conversation_icon);
            viewHold.title = (TextView) convertView.findViewById(R.id.tv_conversation_title);
            viewHold.msg = (TextView) convertView.findViewById(R.id.tv_conversation_msg);
            convertView.setTag(viewHold);

        } else {
            viewHold = (ViewHold) convertView.getTag();
        }

        viewHold.icon.setImageResource(R.mipmap.ic_launcher);
        viewHold.title.setText(mConversationList.get(position).contactsName);
        viewHold.msg.setText(mConversationList.get(position).lastMsg);

        return convertView;
    }

    class ViewHold {
        public ImageView icon;
        public TextView title;
        public TextView msg;
    }

}
