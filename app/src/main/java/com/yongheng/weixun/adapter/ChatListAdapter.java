package com.yongheng.weixun.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.yongheng.weixun.R;
import com.yongheng.weixun.model.MessageInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 张永恒 on 2015/12/23.
 * 聊天界面消息记录的适配器
 */
public class ChatListAdapter extends BaseAdapter {

    private List<MessageInfo> mMessageList;
    private Context mContext;


    public ChatListAdapter(Context context) {
        mContext = context;
    }

    public List<MessageInfo> getMessageList() {
        if (mMessageList == null) {
            mMessageList = new ArrayList<>();
        }
        return mMessageList;
    }

    @Override
    public int getCount() {
        if (mMessageList != null) {
            return mMessageList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return mMessageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            if (getItemViewType(position) == 0) {
                viewHolder = new ViewHolder();
                convertView = View.inflate(mContext, R.layout.widget_list_item_chat_out, null);
                viewHolder.tvContent = (TextView) convertView.findViewById(R.id.tv_chat_message);
                convertView.setTag(viewHolder);

            } else if (getItemViewType(position) == 1) {
                viewHolder = new ViewHolder();
                convertView = View.inflate(mContext, R.layout.widget_list_item_chat_in, null);
                viewHolder.tvContent = (TextView) convertView.findViewById(R.id.tv_chat_message);
                convertView.setTag(viewHolder);
            }
        }
        viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.tvContent.setText(mMessageList.get(position).content);

        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        if (mMessageList != null) {
            return mMessageList.get(position).type;
        }
        return super.getItemViewType(position);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    class ViewHolder {
        TextView tvContent;
    }
}
