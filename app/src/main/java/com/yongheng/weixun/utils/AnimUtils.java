package com.yongheng.weixun.utils;

import android.content.Context;
import android.view.View;

import com.yongheng.weixun.R;

/**
 * Created by 张永恒 on 2015/12/22.
 * 动画工具类
 */
public class AnimUtils {

    /**
     * 开启摇动动画
     *
     * @param context
     * @param view
     */
    public static void startShakeAnimation(Context context, View view) {
        view.startAnimation(android.view.animation.AnimationUtils.loadAnimation(context, R.anim.shake));
    }


}
