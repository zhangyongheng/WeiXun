package com.yongheng.weixun.utils;

import android.content.Context;

/**
 * Created by 张永恒 on 2015/12/30.
 * 单位转换工具类
 */

public class DensityUtils {

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}