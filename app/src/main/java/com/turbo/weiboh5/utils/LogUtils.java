package com.turbo.weiboh5.utils;

import android.util.Log;

/**
 * 文件名：LogUtils
 * 作者：Turbo
 * 时间：2020-01-13 17:20
 * 蚁穴虽小，溃之千里。
 */
public class LogUtils {

    private static final boolean isOpen = true;

    public static void v(String TAG, String msg) {
        if (isOpen) {
            Log.v(TAG, msg);
        }
    }
    public static void d(String TAG, String msg) {
        if (isOpen) {
            Log.d(TAG, msg);
        }
    }
    public static void i(String TAG, String msg) {
        if (isOpen) {
            Log.i(TAG, msg);
        }
    }
    public static void e(String TAG, String msg) {
        if (isOpen) {
            Log.e(TAG, msg);
        }
    }
    public static void w(String TAG, String msg) {
        if (isOpen) {
            Log.w(TAG, msg);
        }
    }
}
