package com.turbo.weiboh5.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 文件名：SharedPreferencesUtil
 * 作者：Turbo
 * 时间：2019-12-20 17:50
 * 蚁穴虽小，溃之千里。
 */
public class SharedPreferencesUtils {

    public static final String mTAG = "mqbj";
    // 创建一个写入器
    private static SharedPreferences mPreferences;
    private static SharedPreferences.Editor mEditor;
    private static SharedPreferencesUtils mSharedPreferencesUtil;

    // 构造方法
    public SharedPreferencesUtils(Context context) {
        mPreferences = context.getSharedPreferences(mTAG, Context.MODE_PRIVATE);
        mEditor = mPreferences.edit();
    }

    // 单例模式
    public static SharedPreferencesUtils getInstance(Context context) {
        if (mSharedPreferencesUtil == null) {
            mSharedPreferencesUtil = new SharedPreferencesUtils(context);
        }
        return mSharedPreferencesUtil;
    }

    // 存入数据
    public void putSP(String key, String value) {
        mEditor.putString(key, value);
        mEditor.commit();
    }

    // 获取数据
    public String getSP(String key) {
        return mPreferences.getString(key, "");
    }

    // 移除数据
    public void removeSP(String key) {
        mEditor.remove(key);
        mEditor.commit();
    }
}
