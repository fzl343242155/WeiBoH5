package com.turbo.weiboh5.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.BitmapCallback;
import com.lzy.okgo.model.Response;
import com.turbo.weiboh5.TurboApplication;
import com.turbo.weiboh5.URLs;
import com.turbo.weiboh5.bean.DataBean;
import com.turbo.weiboh5.bean.EventBean;
import com.turbo.weiboh5.bean.ForwardBean;
import com.turbo.weiboh5.bean.KeyBean;
import com.turbo.weiboh5.bean.PicBean;
import com.turbo.weiboh5.bean.SocketActionBean;
import com.turbo.weiboh5.bean.User2Bean;
import com.turbo.weiboh5.utils.AndroidUtil;
import com.turbo.weiboh5.utils.DataUtils;
import com.turbo.weiboh5.utils.EnumUtils;
import com.turbo.weiboh5.utils.HttpServer;
import com.turbo.weiboh5.utils.JWebSocketClient;
import com.turbo.weiboh5.utils.LogUtils;
import com.turbo.weiboh5.utils.SharedPreferencesUtils;
import com.turbo.weiboh5.utils.Util;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 文件名：MessageService
 * 作者：Turbo
 * 时间：2020-01-13 16:47
 * 蚁穴虽小，溃之千里。
 */
public class MessageService extends Service {

    private static final String TAG = "turbo";

    private static JWebSocketClient client;
    private Context mContext;

    private String f_account;
    private String forwardcontent;
    private String weiboid;
    private String id;
    private String pic_id;

    private int index = 0;
    private int threshold = 2;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = MessageService.this;
    }

    public static void sendData(String json) {
        if (client != null && client.isOpen()) {
            LogUtils.e(TAG, "sendData: " + json);
            client.send(json);
        } else {
            LogUtils.e(TAG, "sendData: client = null or client.isopen = false");
        }
    }

    public static void socket_close() {
        if (client != null && client.isOpen()) {
            try {
                client.closeBlocking();
                client = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        connect();
        return super.onStartCommand(intent, flags, startId);
    }

    private void connect() {
        LogUtils.e(TAG, "MessageService  connect");
        URI uri = URI.create(URLs.SOCKET_URL);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (client == null) {
                    client = new JWebSocketClient(uri) {
                        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                        @Override
                        public void onMessage(String message) {
                            //message就是接收到的消息
                            LogUtils.e(TAG, "接收到的消息 = " + message);
                            ProcessData(message);
                        }
                    };
                    try {
                        client.connectBlocking();
                        startRotation();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }

    /**
     * 处理数据
     */
    private void ProcessData(String json) {
        SocketActionBean bean;
        try {
            JSONObject jsonObject = new JSONObject(json);
            String ation = jsonObject.getString("action");
            switch (ation) {
                case "login":  //登录成功
                    EventBus.getDefault().post(EnumUtils.EVENT_TYPE.LOGIN_SUCCESS);
                    break;
                case "error": // 登录失败
                    EventBus.getDefault().post(EnumUtils.EVENT_TYPE.LOGIN_ERROR);
                    break;
                case "server_to_android_register_weibo": //插入一个微博账号
                    JSONObject data_object = jsonObject.getJSONObject("data");
                    String account = data_object.getString("account");
                    JSONObject cookie = data_object.getJSONObject("cookie");
                    StringBuilder stringBuilder = new StringBuilder();
                    HashMap<String, String> map = JsonObjectToHashMap(cookie);
                    for (String keyString : map.keySet()) {
                        String str = keyString + "=" + map.get(keyString) + "; ";
                        stringBuilder.append(str);
                    }

                    String cookie_str = stringBuilder.substring(0, stringBuilder.length() - 2);
                    DataBean dataBean = new DataBean();
                    dataBean.setAccount(account);
                    dataBean.setCookie(cookie_str);
                    DataUtils.getInstance().insert(dataBean);

                    //返回数据
                    bean = new SocketActionBean();
                    bean.setAction("android_to_server_registerok");
                    bean.setAccount(account);
                    MessageService.sendData(new Gson().toJson(bean));

                    // 通知界面刷新
                    EventBus.getDefault().post(EnumUtils.EVENT_TYPE.REFRESH_DATA);
                    // 开启轮训
                    start_Timer(account, cookie_str);
                    break;
                case "server_to_android_delete_account": //删除一个微博账号
                    JSONObject data_d_object = jsonObject.getJSONObject("data");
                    String d_account = data_d_object.getString("account");
                    DataUtils.getInstance().delete(DataUtils.getInstance().getID(d_account));
                    break;
                case "server_to_android_queryaccountlist": //查询所有微博号 返回一个列表
                    String pcid = jsonObject.getString("data");
                    List<DataBean> list = DataUtils.getInstance().selectAll();

                    List<KeyBean> list_key = new ArrayList<>();
                    for (DataBean d : list) {
                        KeyBean keyBean = new KeyBean();
                        keyBean.setAccount(d.getAccount());
                        keyBean.setStatus(d.getStatus());
                        list_key.add(keyBean);
                    }

                    //返回数据
                    bean = new SocketActionBean();
                    bean.setAction("android_to_server_accountlist");
                    bean.setPcid(pcid);
                    bean.setData(list_key);
                    MessageService.sendData(new Gson().toJson(bean));
                    break;
                case "server_to_android_forward": //转发微博
                    JSONObject data_f_object = jsonObject.getJSONObject("data");
                    String f_account_n = data_f_object.getString("account");
                    String forwardcontent_n = data_f_object.getString("forwardcontent");
                    String weiboid_n = data_f_object.getString("weiboid");
                    //存值
                    SharedPreferencesUtils.getInstance(TurboApplication.getApp()).putSP(URLs.CURRENT_USER, f_account_n);
                    SharedPreferencesUtils.getInstance(TurboApplication.getApp()).putSP(URLs.FORWARD_USER, weiboid_n);
                    //通知界面 转圈
                    EventBus.getDefault().post(EnumUtils.EVENT_TYPE.FORWARD_START);

                    f_account = f_account_n;
                    forwardcontent = forwardcontent_n;
                    weiboid = weiboid_n;

                    String cookies = DataUtils.getInstance().getCookie();
                    //网络请求
                    HttpServer.$().weibo_config(cookies)
                            .subscribeOn(Schedulers.io())
                            .subscribe(new Action1<String>() {
                                @Override
                                public void call(String text) {
                                    LogUtils.e(TAG, "start_Timer: account = " + f_account_n + "     结果 = " + text);
                                    User2Bean user2Bean = new Gson().fromJson(text, User2Bean.class);
                                    DataBean dataBean = DataUtils.getInstance().select(DataUtils.getInstance().getID(f_account_n));
                                    if (user2Bean.getData().isLogin()) {
                                        dataBean.setStatus("1");
                                        dataBean.setSt(user2Bean.getData().getSt());
                                        onForward();
                                        onFocus(weiboid);
                                    } else {
                                        dataBean.setStatus("0");
                                        dataBean.setSt("");
                                    }
                                    DataUtils.getInstance().update(dataBean);
                                }
                            }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {

                                }
                            });


                    break;
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关注
     *
     * @param searchID
     */
    public static void onFocus(String searchID) {
        String st = DataUtils.getInstance().getSt();
        HttpServer.$().weibo_focus(searchID, st)
                .observeOn(Schedulers.io())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String text) {
                        try {
                            ForwardBean forwardBean = new Gson().fromJson(text, ForwardBean.class);
                            if (forwardBean.getOk() == 1) {
                                EventBus.getDefault().post(EnumUtils.EVENT_TYPE.FOCUS_SUCCESS);
                            }
                        } catch (Exception e) {

                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
    }


    private void onForward() {
        EventBus.getDefault().post(EnumUtils.FORWARD_TYPE.FORWARD_LOG_1);
        String st = DataUtils.getInstance().getSt();
        HttpServer.$().weibo_search(weiboid)
                .flatMap(new Func1<String, Observable<String>>() {
                    @Override
                    public Observable<String> call(String text) {
                        EventBus.getDefault().post(EnumUtils.FORWARD_TYPE.FORWARD_LOG_2);
                        if (TextUtils.isEmpty(text)) {
                            return null;
                        }
                        File mFile = null;
                        try {
                            JSONObject jsonObject = new JSONObject(text);
                            String ok = jsonObject.getString("ok");
                            if ("0".equals(ok)) {
                                //没有搜到结果
                                EventBus.getDefault().post(EnumUtils.FORWARD_TYPE.FORWARD_LOG_2_1);
                                return null;
                            } else {
                                JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("statuses");
                                if (jsonArray.length() > 0) {
                                    JSONObject json = (JSONObject) jsonArray.get(0);
                                    id = json.getString("id");
                                    EventBus.getDefault().post(EnumUtils.FORWARD_TYPE.FORWARD_LOG_3);

                                    String pic_path = SharedPreferencesUtils.getInstance(TurboApplication.getApp()).getSP(URLs.PIC_PATH);
                                    if (TextUtils.isEmpty(pic_path)) {
                                        EventBus.getDefault().post(EnumUtils.EVENT_TYPE.PIC_NULL);
                                        EventBus.getDefault().post(EnumUtils.EVENT_TYPE.FORWARD_ERROR);
                                        return null;
                                    }
                                    mFile = new File(pic_path);
                                } else {
                                    //该人没有微博
                                    EventBus.getDefault().post(EnumUtils.FORWARD_TYPE.FORWARD_LOG_2_2);
                                    return null;
                                }

                            }

                        } catch (Exception e) {
                        }
                        LogUtils.e(TAG, "id =" + id);
                        LogUtils.e(TAG, "st =" + st);
                        EventBus.getDefault().post(EnumUtils.FORWARD_TYPE.FORWARD_LOG_4);
                        return HttpServer.$().weibo_uploadPic(id, st, mFile);
                    }
                })
                .flatMap(new Func1<String, Observable<String>>() {
                    @Override
                    public Observable<String> call(String s) {
                        if (TextUtils.isEmpty(s)) {
                            EventBus.getDefault().post(EnumUtils.FORWARD_TYPE.FORWARD_LOG_4_1);
                            return null;
                        }
                        EventBus.getDefault().post(EnumUtils.FORWARD_TYPE.FORWARD_LOG_5);
                        PicBean picBean = new Gson().fromJson(s, PicBean.class);
                        pic_id = picBean.getPic_id();
                        EventBus.getDefault().post(EnumUtils.FORWARD_TYPE.FORWARD_LOG_6);
                        return HttpServer.$().weibo_forward(id, st, forwardcontent, pic_id, "");
                    }
                })
                .observeOn(Schedulers.io())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String text) {
                        LogUtils.e(TAG, "结果: " + text);
                        try {
                            if (TextUtils.isEmpty(text)) {
                                EventBus.getDefault().post(EnumUtils.FORWARD_TYPE.FORWARD_LOG_7);
                                EventBus.getDefault().post(EnumUtils.EVENT_TYPE.FORWARD_ERROR);
                                return;
                            }
                            EventBus.getDefault().post(EnumUtils.FORWARD_TYPE.FORWARD_LOG_8);
                            ForwardBean forwardBean = new Gson().fromJson(text, ForwardBean.class);

                            if (forwardBean.getOk() == 1) {
                                EventBus.getDefault().post(EnumUtils.EVENT_TYPE.FORWARD_SUCCESS);
                            } else {
                                if ("20031".equals(forwardBean.getErrno())) {
                                    EventBus.getDefault().post(EnumUtils.FORWARD_TYPE.FORWARD_LOG_9);
                                    //请输入验证码
                                    getWeiboCode();
                                } else {
                                    EventBean eventBean = new EventBean();
                                    eventBean.setErrno(forwardBean.getErrno());
                                    eventBean.setMsg(forwardBean.getMsg());
                                    EventBus.getDefault().post(eventBean);
                                    EventBus.getDefault().post(EnumUtils.FORWARD_TYPE.FORWARD_LOG_10);
                                }
                            }
                        } catch (Exception e) {
                            LogUtils.e(TAG, "Exception: " + e.toString());
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        LogUtils.e(TAG, "Throwable: " + throwable.toString());
                        EventBus.getDefault().post(EnumUtils.FORWARD_TYPE.FORWARD_LOG_24);
                    }
                });
    }

    /**
     * 获取转发微博所需要的验证码
     */
    private void getWeiboCode() {
        String cookie = DataUtils.getInstance().getCookie();
        OkGo.<Bitmap>get(URLs.WEIBO_CODE_URL)
                .headers("cookie", cookie)
                .execute(new BitmapCallback() {
                    @Override
                    public void onSuccess(Response<Bitmap> response) {
                        Bitmap bitmap = response.body();
                        if (bitmap != null) {
                            EventBus.getDefault().post(EnumUtils.FORWARD_TYPE.FORWARD_LOG_11);
                            getCode(bitmap);
                        } else {
                            EventBus.getDefault().post(EnumUtils.FORWARD_TYPE.FORWARD_LOG_12);
                            EventBus.getDefault().post(EnumUtils.EVENT_TYPE.FORWARD_ERROR);
                        }
                    }

                    @Override
                    public void onError(Response<Bitmap> response) {
                        super.onError(response);
                        EventBus.getDefault().post(EnumUtils.EVENT_TYPE.FORWARD_ERROR);
                        EventBus.getDefault().post(EnumUtils.FORWARD_TYPE.FORWARD_LOG_23);
                    }
                });
    }

    /**
     * 打码平台------获取验证码
     *
     * @param bitmap
     */
    private void getCode(Bitmap bitmap) {
        byte[] img_data = new AndroidUtil().getBytesByBitmap(bitmap);
        String pic = Util.CalcBase64(img_data);

        long cur_tm = new Date().getTime() / 1000;
        String stm = String.valueOf(cur_tm);

        String sign = Util.CalcSign(URLs.PD_ID, URLs.PD_KEY, stm);
        String asign = Util.CalcSign(URLs.APP_ID, URLs.APP_KEY, stm);

        HttpServer.$().getBalance(sign, stm)
                .flatMap(new Func1<String, Observable<String>>() {
                    @Override
                    public Observable<String> call(String s) {
                        String cust_val = parsingJson(s, "cust_val");
                        if (Integer.parseInt(cust_val) > 10) {
                            EventBus.getDefault().post(EnumUtils.FORWARD_TYPE.FORWARD_LOG_14);
                            return HttpServer.$().getCode(pic, sign, asign, stm);
                        } else {
                            EventBus.getDefault().post(EnumUtils.FORWARD_TYPE.FORWARD_LOG_13);
                            EventBean eventBean = new EventBean();
                            eventBean.setErrno("6");
                            eventBean.setMsg("打码平台余额不足，请充值");
                            EventBus.getDefault().post(eventBean);
                            return null;
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        LogUtils.e(TAG, "打码结果: " + s);
                        String result = parsingJson(s, "result");
                        if (TextUtils.isEmpty(result)) {
                            if (index < threshold) {
                                index++;
                                getWeiboCode();
                            } else {
                                EventBus.getDefault().post(EnumUtils.FORWARD_TYPE.FORWARD_LOG_15);
                            }
                        } else {
                            EventBus.getDefault().post(EnumUtils.FORWARD_TYPE.FORWARD_LOG_16);
                            onForward2(result);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        LogUtils.e(TAG, "call: error");
                        EventBus.getDefault().post(EnumUtils.EVENT_TYPE.FORWARD_ERROR);
                        EventBus.getDefault().post(EnumUtils.FORWARD_TYPE.FORWARD_LOG_22);
                    }
                });
    }

    private void onForward2(String code) {
        EventBus.getDefault().post(EnumUtils.FORWARD_TYPE.FORWARD_LOG_17);
        String st = DataUtils.getInstance().getSt();
        HttpServer.$().weibo_forward(id, st, forwardcontent, pic_id, code)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String text) {
                        try {
                            JSONObject jsonObject = new JSONObject(text);
                            String ok = jsonObject.getString("ok");
                            if ("1".equals(ok)) {
                                Log.e(TAG, "call: 转发成功");
                                EventBus.getDefault().post(EnumUtils.EVENT_TYPE.FORWARD_SUCCESS);
                            } else {
                                Log.e(TAG, "call: 转发失败");
                                EventBus.getDefault().post(EnumUtils.FORWARD_TYPE.FORWARD_LOG_19);
                                EventBus.getDefault().post(EnumUtils.EVENT_TYPE.FORWARD_ERROR);
                                onForward();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "call: 转发失败   error " + e.toString());
                            EventBus.getDefault().post(EnumUtils.FORWARD_TYPE.FORWARD_LOG_20);
                            EventBus.getDefault().post(EnumUtils.EVENT_TYPE.FORWARD_ERROR);
                        }

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        EventBus.getDefault().post(EnumUtils.FORWARD_TYPE.FORWARD_LOG_21);
                        EventBus.getDefault().post(EnumUtils.EVENT_TYPE.FORWARD_ERROR);
                    }
                });
    }

    /**
     * 获取所有微博账号 开启定时 轮训config接口 获取账号对应的st
     */
    private void startRotation() {
        List<DataBean> list = DataUtils.getInstance().selectAll();
        if (list.isEmpty()) {
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            DataBean dataBean = list.get(i);
            try {
                int sleepTime = getRandom();
                Thread.sleep(sleepTime * 30 * 1000);
                if ("1".equals(dataBean.getStatus())) {
                    start_Timer(dataBean.getAccount(), dataBean.getCookie());
                }
            } catch (Exception e) {

            }
        }
    }

    private int getRandom() {
        return (int) (Math.random() * 10) + 1;
    }


    /**
     * 启动定时器
     *
     * @param account
     * @param cookie
     */
    private void start_Timer(String account, String cookie) {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                //网络请求
                HttpServer.$().weibo_config(cookie)
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Action1<String>() {
                            @Override
                            public void call(String text) {
                                LogUtils.e(TAG, "start_Timer: account = " + account + "     结果 = " + text);
                                User2Bean user2Bean = new Gson().fromJson(text, User2Bean.class);
                                DataBean dataBean = DataUtils.getInstance().select(DataUtils.getInstance().getID(account));
                                if (user2Bean.getData().isLogin()) {
                                    dataBean.setStatus("1");
                                    dataBean.setSt(user2Bean.getData().getSt());
                                } else {
                                    dataBean.setStatus("0");
                                    dataBean.setSt("");
                                }
                                DataUtils.getInstance().update(dataBean);
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {

                            }
                        });
            }
        };

        timer.schedule(task, 0, 5 * 60 * 1000);
    }

    /**
     * 解析打码平台数据 获取验证码
     *
     * @param json
     * @return
     */
    private String parsingJson(String json, String key) {
        String result = "";
        try {
            JSONObject jsonObject = new JSONObject(json);

            String retCode = jsonObject.getString("RetCode");
            if ("0".equals(retCode)) {
                String result_json = jsonObject.getString("RspData");
                if (TextUtils.isEmpty(result_json)) {
                    LogUtils.e(TAG, "打码平台 打码失败");
                } else {
                    JSONObject jsonResultObject = new JSONObject(result_json);
                    result = jsonResultObject.getString(key);
                    LogUtils.e(TAG, "getCode: result = " + result);
                }
            } else {
                String ErrMsg = jsonObject.getString("ErrMsg");
                LogUtils.e(TAG, "getCode: ErrMsg = " + ErrMsg);
            }

        } catch (Exception e) {

        }
        return result;
    }

    /**
     * 将JSONObject对象转换为HashMap<String,String>
     *
     * @param jsonObj
     * @return
     * @throws JSONException
     */
    public HashMap<String, String> JsonObjectToHashMap(JSONObject jsonObj) throws JSONException {
        HashMap<String, String> data = new HashMap<String, String>();
        Iterator it = jsonObj.keys();
        while (it.hasNext()) {
            String key = String.valueOf(it.next().toString());
            String value = (String) jsonObj.get(key).toString();
            data.put(key, value);
        }
        System.out.println(data);
        return data;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
