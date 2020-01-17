package com.turbo.weiboh5.utils;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.turbo.weiboh5.R;

/**
 * 文件名：ShowDialogUtils
 * 作者：Turbo
 * 时间：2020-01-13 14:44
 * 蚁穴虽小，溃之千里。
 */
public class ShowDialogUtils {
    public static ProgressDialog mProgressDialog;

    @SuppressLint("WrongConstant")
    public static ProgressDialog loadingDialog(Context context, String hint) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.loading_dialog, null);// 得到加载view
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialog_view);// 加载布局
        ImageView process = (ImageView) v.findViewById(R.id.progress);
        TextView tvLoadingHint = (TextView) v.findViewById(R.id.tv_loading_hint);
        if (!TextUtils.isEmpty(hint))
            tvLoadingHint.setText(hint);
        final ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(process, "rotation", 0, 359);
        objectAnimator.setInterpolator(new LinearInterpolator());//不停顿
        objectAnimator.setRepeatCount(Animation.INFINITE);
        objectAnimator.setRepeatMode(Animation.RESTART);
        objectAnimator.setDuration(1000);
        objectAnimator.start();
        mProgressDialog = new ProgressDialog(context, R.style.loading_dialog_style);
        mProgressDialog.show();
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setContentView(layout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));// 设置布局
        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                objectAnimator.cancel();
            }
        });
        return mProgressDialog;
    }

    public static void dismiss() {
        if (null != mProgressDialog) mProgressDialog.dismiss();
    }
}
