package com.yongheng.weixun.utils;

import android.content.Context;
import android.widget.Toast;

import com.yongheng.weixun.R;

/**
 * Created by 张永恒 on 2015/12/29.
 * Toast工具类
 */
public class ToastUtils {

    public static void showException(Context context) {
        Toast.makeText(context, R.string.common_exception_error, Toast.LENGTH_SHORT).show();
    }

}
