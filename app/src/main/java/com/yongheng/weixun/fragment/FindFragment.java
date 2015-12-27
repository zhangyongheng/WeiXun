package com.yongheng.weixun.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yongheng.weixun.R;

/**
 * Created by 张永恒 on 2015/12/22.
 * 发现Tab页的Fragment
 */
public class FindFragment extends Fragment {

    private View mRootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_find, null);
        }
        return mRootView;
    }
}
