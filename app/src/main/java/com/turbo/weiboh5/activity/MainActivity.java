package com.turbo.weiboh5.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;
import com.turbo.weiboh5.R;
import com.turbo.weiboh5.TurboApplication;
import com.turbo.weiboh5.URLs;
import com.turbo.weiboh5.adapter.UserAdapter;
import com.turbo.weiboh5.bean.DataBean;
import com.turbo.weiboh5.bean.EventBean;
import com.turbo.weiboh5.bean.SocketActionBean;
import com.turbo.weiboh5.service.MessageService;
import com.turbo.weiboh5.utils.DataUtils;
import com.turbo.weiboh5.utils.EnumUtils;
import com.turbo.weiboh5.utils.FileUtil;
import com.turbo.weiboh5.utils.KeyboardUtils;
import com.turbo.weiboh5.utils.LogUtils;
import com.turbo.weiboh5.utils.SharedPreferencesUtils;
import com.turbo.weiboh5.utils.ShowDialogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 文件名：MainActivity
 * 作者：Turbo
 * 时间：2020-01-13 16:47
 * 蚁穴虽小，溃之千里。
 */
public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    @BindView(R.id.et_name)
    EditText etName;
    @BindView(R.id.lv_content)
    ListView lvContent;
    @BindView(R.id.tv_log)
    TextView tvLog;
    @BindView(R.id.tv_empty)
    TextView tvEmpty;
    @BindView(R.id.tv_num)
    TextView tvNum;
    @BindView(R.id.tv_pic_path)
    TextView tvPicPath;
    @BindView(R.id.btn_login)
    Button btnLogin;

    private UserAdapter mUserAdapter;
    private Context mContext;
    private StringBuilder stringBuilder;
    private static ProgressDialog mProgressDialog;
    private int index = 0;
    public static final int RC_CHOOSE_PHOTO = 2;
    private String name;
    private boolean conned = true;
    private int conn_num = 0;
    private Timer timer = null;
    private boolean ManualStop = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();
        EventBus.getDefault().register(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//保持屏幕常亮
        stringBuilder = new StringBuilder();
        mContext = MainActivity.this;
        ButterKnife.bind(this);
        init();
    }

    /**
     * 权限申请
     */
    private void checkPermission() {
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        String rationale = getResources().getString(R.string.title1);
        Permissions.Options options = new Permissions.Options()
                .setSettingsText(getResources().getString(R.string.setting))
                .setSettingsDialogMessage(getResources().getString(R.string.title2))
                .setRationaleDialogTitle(getResources().getString(R.string.title3))
                .setSettingsDialogTitle(getResources().getString(R.string.title3));

        Permissions.check(this, permissions, rationale, options, new PermissionHandler() {
            @Override
            public void onGranted() {
            }

            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(mContext, MessageService.class);
        startService(intent);
    }

    private void init() {
        String name = SharedPreferencesUtils.getInstance(TurboApplication.getApp()).getSP(URLs.CLIENT_USER);
        etName.setText(name);
        mUserAdapter = new UserAdapter(mContext);
        lvContent.setAdapter(mUserAdapter);
        List<DataBean> list = DataUtils.getInstance().selectAll();
        mUserAdapter.setAdapterData(list);
        if (list.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            lvContent.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            lvContent.setVisibility(View.VISIBLE);
        }

        // 设置内容可滚动
        tvLog.setMovementMethod(ScrollingMovementMethod.getInstance());

        String filePath = SharedPreferencesUtils.getInstance(TurboApplication.getApp()).getSP(URLs.PIC_PATH); //获取当前用户
        tvPicPath.setText(filePath);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventBean bean) {
        dismissWaiting();
        Toast.makeText(mContext, bean.getMsg(), Toast.LENGTH_SHORT).show();
        String current_user = SharedPreferencesUtils.getInstance(TurboApplication.getApp()).getSP(URLs.CURRENT_USER); //获取当前用户
        String user = SharedPreferencesUtils.getInstance(TurboApplication.getApp()).getSP(URLs.FORWARD_USER); //获取当前用户
        SocketActionBean socketActionBean = new SocketActionBean();
        socketActionBean.setAction("android_to_server_forward_fail");
        socketActionBean.setAccount(current_user);
        socketActionBean.setWeiboid(user);
        socketActionBean.setError(bean.getErrno());
        String json = new Gson().toJson(bean);
        MessageService.sendData(json);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EnumUtils.FORWARD_TYPE type) {

        String current_user = SharedPreferencesUtils.getInstance(TurboApplication.getApp()).getSP(URLs.CURRENT_USER); //获取当前用户
        String user = SharedPreferencesUtils.getInstance(TurboApplication.getApp()).getSP(URLs.FORWARD_USER); //获取当前用户
        SocketActionBean bean;
        String json;

        switch (type) {
            case FORWARD_LOG_1:
                stringBuilder.append("开始转发微博\n");
                setText(stringBuilder);
                FileUtil.writeWBLogFile("开始转发微博（log1）\n");
                break;
            case FORWARD_LOG_2:
                stringBuilder.append("根据ID搜索微博成功\n");
                setText(stringBuilder);
                FileUtil.writeWBLogFile("根据ID搜索微博成功（log2）\n");
                break;
            case FORWARD_LOG_2_1:
                dismissWaiting();
                stringBuilder.append("根据ID没有搜到结果\n");
                setText(stringBuilder);
                FileUtil.writeWBLogFile("根据ID没有搜到结果（log2-1）\n");

                bean = new SocketActionBean();
                bean.setAction("android_to_server_forward_fail");
                bean.setAccount(current_user);
                bean.setWeiboid(user);
                bean.setError("2");
                json = new Gson().toJson(bean);
                MessageService.sendData(json);
                break;
            case FORWARD_LOG_2_2:
                dismissWaiting();
                String weiboID = SharedPreferencesUtils.getInstance(TurboApplication.getApp()).getSP(URLs.FORWARD_USER);
                stringBuilder.append("该号 " + weiboID + " 没有一条微博\n");
                setText(stringBuilder);
                FileUtil.writeWBLogFile("该号 " + weiboID + "没有一条微博（log2-2）\n");

                bean = new SocketActionBean();
                bean.setAction("android_to_server_forward_fail");
                bean.setAccount(current_user);
                bean.setWeiboid(user);
                bean.setError("5");
                json = new Gson().toJson(bean);
                MessageService.sendData(json);
                break;
            case FORWARD_LOG_3:
                stringBuilder.append("根据搜索结果获取重要参数成功\n");
                setText(stringBuilder);
                FileUtil.writeWBLogFile("根据搜索结果获取重要参数成功（log3）\n");
                break;
            case FORWARD_LOG_4:
                stringBuilder.append("数据准备成功，开始上传图片\n");
                setText(stringBuilder);
                FileUtil.writeWBLogFile("数据准备成功，开始上传图片（log4）\n");
                break;
            case FORWARD_LOG_4_1:
                dismissWaiting();
                stringBuilder.append("该手机没有读写sd卡权限\n");
                setText(stringBuilder);
                FileUtil.writeWBLogFile("该手机没有读写sd卡权限（log4-1）\n");
                bean = new SocketActionBean();
                bean.setAction("android_to_server_forward_fail");
                bean.setAccount(current_user);
                bean.setWeiboid(user);
                bean.setError("13");
                json = new Gson().toJson(bean);
                MessageService.sendData(json);
                break;
            case FORWARD_LOG_5:
                stringBuilder.append("上传成功，获取图片ID地址\n");
                setText(stringBuilder);
                FileUtil.writeWBLogFile("上传成功，获取图片ID地址（log5）\n");
                break;
            case FORWARD_LOG_6:
                stringBuilder.append("开始转发微博\n");
                setText(stringBuilder);
                FileUtil.writeWBLogFile("开始转发微博（log6）\n");
                break;
            case FORWARD_LOG_7:
                dismissWaiting();
                stringBuilder.append("转发结果为空\n");
                setText(stringBuilder);
                FileUtil.writeWBLogFile("转发结果为空（log7）\n");
                break;
            case FORWARD_LOG_8:
                stringBuilder.append("获取到了转发的结果\n");
                setText(stringBuilder);
                FileUtil.writeWBLogFile("获取到了转发的结果（log8）\n");
                break;
            case FORWARD_LOG_9:
                stringBuilder.append("根据转发结果，判断出需要输入验证码\n");
                setText(stringBuilder);
                FileUtil.writeWBLogFile("根据转发结果，判断出需要输入验证码（log9）\n");
                break;
            case FORWARD_LOG_10:
                dismissWaiting();
                stringBuilder.append("转发结果失败的其他信息\n");
                setText(stringBuilder);
                FileUtil.writeWBLogFile("转发结果失败的其他信息（log10）\n");
                break;
            case FORWARD_LOG_11:
                stringBuilder.append("获取到验证码\n");
                setText(stringBuilder);
                FileUtil.writeWBLogFile("获取到验证码（log11）\n");
                break;
            case FORWARD_LOG_12:
                dismissWaiting();
                stringBuilder.append("验证码获取失败\n");
                setText(stringBuilder);
                FileUtil.writeWBLogFile("验证码获取失败（log12）\n");

                bean = new SocketActionBean();
                bean.setAction("android_to_server_forward_fail");
                bean.setAccount(current_user);
                bean.setWeiboid(user);
                bean.setError("3");
                json = new Gson().toJson(bean);
                MessageService.sendData(json);
                break;
            case FORWARD_LOG_13:
                dismissWaiting();
                stringBuilder.append("打码平台余额不足\n");
                setText(stringBuilder);
                FileUtil.writeWBLogFile("打码平台余额不足（log13）\n");
                break;
            case FORWARD_LOG_14:
                stringBuilder.append("开始获取打码平台结果\n");
                setText(stringBuilder);
                FileUtil.writeWBLogFile("开始获取打码平台结果（log14）\n");
                break;
            case FORWARD_LOG_15:
                dismissWaiting();
                stringBuilder.append("打码失败\n");
                setText(stringBuilder);
                FileUtil.writeWBLogFile("打码失败（log15）\n");
                bean = new SocketActionBean();
                bean.setAction("android_to_server_forward_fail");
                bean.setAccount(current_user);
                bean.setWeiboid(user);
                bean.setError("7");
                json = new Gson().toJson(bean);
                MessageService.sendData(json);
                break;
            case FORWARD_LOG_16:
                stringBuilder.append("打码成功\n");
                setText(stringBuilder);
                FileUtil.writeWBLogFile("打码成功（log16）\n");
                break;
            case FORWARD_LOG_17:
                stringBuilder.append("再次执行转发任务\n");
                setText(stringBuilder);
                FileUtil.writeWBLogFile("再次执行转发任务（log17）\n");
                break;
            case FORWARD_LOG_18:
                break;
            case FORWARD_LOG_19:
                dismissWaiting();
                stringBuilder.append("转发失败\n");
                setText(stringBuilder);
                FileUtil.writeWBLogFile("转发失败（log19）\n");
                bean = new SocketActionBean();
                bean.setAction("android_to_server_forward_fail");
                bean.setAccount(current_user);
                bean.setWeiboid(user);
                bean.setError("4");
                json = new Gson().toJson(bean);
                MessageService.sendData(json);
                break;
            case FORWARD_LOG_20:
                dismissWaiting();
                stringBuilder.append("转发结果解析异常\n");
                setText(stringBuilder);
                FileUtil.writeWBLogFile("转发结果解析异常（log20）\n");
                bean = new SocketActionBean();
                bean.setAction("android_to_server_forward_fail");
                bean.setAccount(current_user);
                bean.setWeiboid(user);
                bean.setError("8");
                json = new Gson().toJson(bean);
                MessageService.sendData(json);
                break;
            case FORWARD_LOG_21:
                dismissWaiting();
                stringBuilder.append("转发网络问题\n");
                setText(stringBuilder);
                FileUtil.writeWBLogFile("转发网络问题（log21）\n");
                bean = new SocketActionBean();
                bean.setAction("android_to_server_forward_fail");
                bean.setAccount(current_user);
                bean.setWeiboid(user);
                bean.setError("9");
                json = new Gson().toJson(bean);
                MessageService.sendData(json);
                break;
            case FORWARD_LOG_22:
                dismissWaiting();
                stringBuilder.append("打码接口网络问题\n");
                setText(stringBuilder);
                FileUtil.writeWBLogFile("打码接口网络问题（log22）\n");
                bean = new SocketActionBean();
                bean.setAction("android_to_server_forward_fail");
                bean.setAccount(current_user);
                bean.setWeiboid(user);
                bean.setError("10");
                json = new Gson().toJson(bean);
                MessageService.sendData(json);
                break;
            case FORWARD_LOG_23:
                dismissWaiting();
                stringBuilder.append("获取验证码接口网络问题\n");
                setText(stringBuilder);
                FileUtil.writeWBLogFile("获取验证码接口网络问题（log23）\n");
                bean = new SocketActionBean();
                bean.setAction("android_to_server_forward_fail");
                bean.setAccount(current_user);
                bean.setWeiboid(user);
                bean.setError("11");
                json = new Gson().toJson(bean);
                MessageService.sendData(json);
                break;
            case FORWARD_LOG_24:
                dismissWaiting();
                stringBuilder.append("搜索 传图 转发 网络问题\n");
                setText(stringBuilder);
                FileUtil.writeWBLogFile("搜索 传图 转发 网络问题（log24）\n");
                bean = new SocketActionBean();
                bean.setAction("android_to_server_forward_fail");
                bean.setAccount(current_user);
                bean.setWeiboid(user);
                bean.setError("12");
                json = new Gson().toJson(bean);
                MessageService.sendData(json);
                break;
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EnumUtils.EVENT_TYPE type) {
        String current_user = SharedPreferencesUtils.getInstance(TurboApplication.getApp()).getSP(URLs.CURRENT_USER); //获取当前用户
        String user = SharedPreferencesUtils.getInstance(TurboApplication.getApp()).getSP(URLs.FORWARD_USER); //获取当前用户
        SocketActionBean bean;
        String json;
        switch (type) {
            case LOGIN_SUCCESS:
                conned = true;
                stringBuilder.append("登录成功\n");
                setText(stringBuilder);
                SharedPreferencesUtils.getInstance(TurboApplication.getApp()).putSP(URLs.CLIENT_USER, name);
                FileUtil.writeWBLogFile("登录成功\n");
                setBtnType();
                break;
            case SEARCH_SUCCESS:
                break;
            case FOCUS_SUCCESS:
                stringBuilder.append("关注成功\n");
                setText(stringBuilder);
                FileUtil.writeWBLogFile("关注成功\n");
                break;
            case FORWARD_SUCCESS:
                dismissWaiting();
                index++;
                tvNum.setText(index + "");
                String log = current_user + " 成功转发 " + user + " 的第一条微博";
                stringBuilder.append(log + "\n");
                setText(stringBuilder);
                FileUtil.writeWBLogFile(log);
                bean = new SocketActionBean();
                bean.setAction("android_to_server_forwardok");
                bean.setAccount(current_user);
                bean.setWeiboid(user);

                json = new Gson().toJson(bean);
                MessageService.sendData(json);
                break;
            case FORWARD_ERROR:
                dismissWaiting();

                bean = new SocketActionBean();
                bean.setAction("android_to_server_forward_fail");
                bean.setAccount(current_user);
                bean.setWeiboid(user);
                bean.setError("4");
                json = new Gson().toJson(bean);
                MessageService.sendData(json);
                break;
            case LOGIN_ERROR:
                conned = false;
                stringBuilder.append("登录失败,名字重复,请重新输入名字\n");
                setText(stringBuilder);
                FileUtil.writeWBLogFile("登录失败,名字重复,请重新输入名字\n");
                setBtnType();
                etName.setText("");
                break;
            case UPLOAD_SUCCESS:
                break;
            case CODE:
                break;
            case REFRESH_DATA:
                List<DataBean> list = DataUtils.getInstance().selectAll();
                if (list.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    lvContent.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    lvContent.setVisibility(View.VISIBLE);
                }
                mUserAdapter.setAdapterData(list);
                break;
            case FORWARD_START:
                showWaiting();
                break;
            case CONN_ERROR:
                conned = false;
                stringBuilder.append("连接断开，请重新登录\n");
                setText(stringBuilder);
                FileUtil.writeWBLogFile("连接断开，请重新登录\n");
                setBtnType();
                break;
            case PIC_NULL:
                stringBuilder.append("请选择图片\n");
                setText(stringBuilder);
                FileUtil.writeWBLogFile("请选择图片\n");
                break;
            case SERVER_ERROR:
                conned = false;
                setBtnType();
                if (timer == null) {
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            // 逻辑处理
                            Intent intent = new Intent(mContext, MessageService.class);
                            startService(intent);
                        }
                    }, 0, 5000);
                }
                break;
            case SERVER_OPEN:
                if (ManualStop) {
                    login();
                }
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
                break;
            case DISMISSWAITING:
                dismissWaiting();
                break;
        }
    }

    /**
     * 设置按钮状态
     */
    private void setBtnType() {
        if (conned) {
            btnLogin.setText("断开");
        } else {
            btnLogin.setText("登录");
        }
    }

    private void setText(StringBuilder stringBuilder) {
        if (stringBuilder.length() > 2000) {
            stringBuilder.delete(0, 1000);
        }
        tvLog.setText(stringBuilder.toString());
    }

    public void showWaiting() {
        if (mProgressDialog == null) {
            mProgressDialog = ShowDialogUtils.loadingDialog(MainActivity.this, "");
        } else {
            mProgressDialog.show();
        }
    }

    public void dismissWaiting() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @OnClick({R.id.btn_login, R.id.btn_select_pic})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                if (conned) {
                    ManualStop = false;
                    MessageService.socket_close();
                } else {
                    ManualStop = true;
                    login();
                }
                break;
            case R.id.btn_select_pic:
                Intent intentToPickPic = new Intent(Intent.ACTION_PICK, null);
                intentToPickPic.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intentToPickPic, RC_CHOOSE_PHOTO);
                break;
        }
    }

    private void login() {
        name = etName.getText().toString().trim();
        if (!TextUtils.isEmpty(name)) {
            SocketActionBean bean = new SocketActionBean();
            bean.setAction("login");
            bean.setUsername(name);
            bean.setType("c");
            String json = new Gson().toJson(bean);
            MessageService.sendData(json);
        } else {
            Toast.makeText(mContext, "请输入用户名", Toast.LENGTH_SHORT).show();
            conned = false;
            setBtnType();
        }
        KeyboardUtils.hideSoftInput(MainActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RC_CHOOSE_PHOTO:
                Uri uri = data.getData();
                String filePath = FileUtil.getFilePathByUri(this, uri);

                if (!TextUtils.isEmpty(filePath)) {
                    LogUtils.e(TAG, "onActivityResult: " + filePath);
                    tvPicPath.setText(filePath);
                    SharedPreferencesUtils.getInstance(TurboApplication.getApp()).putSP(URLs.PIC_PATH, filePath); //获取当前用户
                }
                break;
        }
    }
}
