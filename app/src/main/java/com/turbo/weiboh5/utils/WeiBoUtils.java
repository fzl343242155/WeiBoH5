package com.turbo.weiboh5.utils;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

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
import com.turbo.weiboh5.service.MessageService;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.Date;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 文件名：WeiBoUtils
 * 作者：Turbo
 * 时间：2020-01-20 14:51
 * 蚁穴虽小，溃之千里。
 */
public class WeiBoUtils {

    private int index = 0;
    private int threshold = 2;
    private String account;
    private String forwardContent;
    private String weiBoID;
    private String user_mid;
    private String user_st;
    private String upload_pic_id;

    private static final String TAG = "WeiBoUtils";

    public WeiBoUtils(String account, String forwardContent, String weiBoID) {
        this.account = account;
        this.forwardContent = forwardContent;
        this.weiBoID = weiBoID;
    }

    public void onForward() {
        EventBus.getDefault().post(EnumUtils.EVENT_TYPE.FORWARD_START);
        try {
            DataUtils.getInstance().select(DataUtils.getInstance().getID(account)).getCookie();
            startForward();
        } catch (Exception e) {
            sendWebSocketData(false, "14");
        }
    }

    private void startForward() {
        String cookie = DataUtils.getInstance().select(DataUtils.getInstance().getID(account)).getCookie();
        HttpServer.$().weibo_config(cookie)
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String text) {
                        User2Bean user2Bean = new Gson().fromJson(text, User2Bean.class);
                        DataBean dataBean = DataUtils.getInstance().select(DataUtils.getInstance().getID(account));
                        if (user2Bean.getData().isLogin()) {
                            dataBean.setStatus("1");
                            dataBean.setSt(user2Bean.getData().getSt());
                            onSearch();
                            onFocus(weiBoID);
                        } else {
                            dataBean.setStatus("0");
                            dataBean.setSt("");
                        }
                        DataUtils.getInstance().update(dataBean);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        EventBus.getDefault().post(EnumUtils.EVENT_TYPE.FORWARD_ERROR);
                        sendWebSocketData(false, "4");
                    }
                });
    }

    private void onSearch() {
        String st = DataUtils.getInstance().select(DataUtils.getInstance().getID(account)).getSt();
        HttpServer.$().weibo_search(weiBoID)
                .observeOn(Schedulers.io())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String text) {
                        EventBus.getDefault().post(EnumUtils.FORWARD_TYPE.FORWARD_LOG_2);
                        if (TextUtils.isEmpty(text)) {
                            return;
                        }
                        File mFile = null;
                        String id = "";

                        try {
                            JSONObject jsonObject = new JSONObject(text);
                            String ok = jsonObject.getString("ok");
                            if ("0".equals(ok)) {
                                //没有搜到结果
                                EventBus.getDefault().post(EnumUtils.FORWARD_TYPE.FORWARD_LOG_2_1);
                                sendWebSocketData(false, "2");
                                return;
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
                                        sendWebSocketData(false, "4");
                                        return;
                                    }
                                    mFile = new File(pic_path);
                                } else {
                                    //该人没有微博
                                    EventBus.getDefault().post(EnumUtils.FORWARD_TYPE.FORWARD_LOG_2_2);
                                    sendWebSocketData(false, "5");
                                    return;
                                }

                            }

                        } catch (Exception e) {
                        }
                        LogUtils.e(TAG, "id =" + id);
                        LogUtils.e(TAG, "st =" + st);

                        user_mid = id;
                        user_st = st;

                        uploadPic(id, st, mFile);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        EventBus.getDefault().post(EnumUtils.EVENT_TYPE.FORWARD_ERROR);
                        sendWebSocketData(false, "4");
                    }
                });
    }

    private void uploadPic(String id, String st, File mFile) {
        String cookie = DataUtils.getInstance().select(DataUtils.getInstance().getID(account)).getCookie();
        HttpServer.$().weibo_uploadPic(id, st, mFile, cookie)
                .observeOn(Schedulers.io())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        if (TextUtils.isEmpty(s)) {
                            EventBus.getDefault().post(EnumUtils.FORWARD_TYPE.FORWARD_LOG_4_1);
                            sendWebSocketData(false, "13");
                            return;
                        }
                        EventBus.getDefault().post(EnumUtils.FORWARD_TYPE.FORWARD_LOG_5);
                        PicBean picBean = new Gson().fromJson(s, PicBean.class);
                        String pic_id = picBean.getPic_id();
                        upload_pic_id = pic_id;
                        EventBus.getDefault().post(EnumUtils.FORWARD_TYPE.FORWARD_LOG_6);

                        forward(id, st, pic_id, "");
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        EventBus.getDefault().post(EnumUtils.EVENT_TYPE.FORWARD_ERROR);
                        sendWebSocketData(false, "4");
                    }
                });

    }

    private void forward(String id, String st, String pic_id, String code) {
        String cookie = DataUtils.getInstance().select(DataUtils.getInstance().getID(account)).getCookie();
        HttpServer.$().weibo_forward(id, st, forwardContent, pic_id, code, cookie)
                .observeOn(Schedulers.io())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String text) {
                        LogUtils.e(TAG, "结果: " + text);
                        try {
                            if (TextUtils.isEmpty(text)) {
                                EventBus.getDefault().post(EnumUtils.FORWARD_TYPE.FORWARD_LOG_7);
                                EventBus.getDefault().post(EnumUtils.EVENT_TYPE.FORWARD_ERROR);
                                sendWebSocketData(false, "4");
                                return;
                            }
                            EventBus.getDefault().post(EnumUtils.FORWARD_TYPE.FORWARD_LOG_8);
                            ForwardBean forwardBean = new Gson().fromJson(text, ForwardBean.class);

                            if (forwardBean.getOk() == 1) {
                                LogUtils.e(TAG, "转发成功");
                                KeyBean keyBean = new KeyBean();
                                keyBean.setAccount(account);
                                keyBean.setStatus(weiBoID);
                                EventBus.getDefault().post(keyBean);
                                sendWebSocketData(true);
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
                        EventBus.getDefault().post(EnumUtils.EVENT_TYPE.FORWARD_ERROR);
                        sendWebSocketData(false, "4");
                    }
                });
    }

    private void getWeiboCode() {
        String cookie = DataUtils.getInstance().select(DataUtils.getInstance().getID(account)).getCookie();
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
                            sendWebSocketData(false, "3");
                        }
                    }

                    @Override
                    public void onError(Response<Bitmap> response) {
                        super.onError(response);
                        EventBus.getDefault().post(EnumUtils.EVENT_TYPE.FORWARD_ERROR);
                        EventBus.getDefault().post(EnumUtils.FORWARD_TYPE.FORWARD_LOG_23);
                        sendWebSocketData(false, "11");
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
                                sendWebSocketData(false, "7");
                            }
                        } else {
                            EventBus.getDefault().post(EnumUtils.FORWARD_TYPE.FORWARD_LOG_16);
                            forward(user_mid, user_st, upload_pic_id, result);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        LogUtils.e(TAG, "call: error");
                        EventBus.getDefault().post(EnumUtils.EVENT_TYPE.FORWARD_ERROR);
                        EventBus.getDefault().post(EnumUtils.FORWARD_TYPE.FORWARD_LOG_22);
                        sendWebSocketData(false, "10");
                    }
                });
    }

    /**
     * 关注
     *
     * @param searchID
     */
    private void onFocus(String searchID) {
        String st = DataUtils.getInstance().select(DataUtils.getInstance().getID(account)).getSt();
        String cookie = DataUtils.getInstance().select(DataUtils.getInstance().getID(account)).getCookie();
        HttpServer.$().weibo_focus(searchID, st, cookie)
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
                        EventBus.getDefault().post(EnumUtils.EVENT_TYPE.FORWARD_ERROR);
                        sendWebSocketData(false, "4");
                    }
                });
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
     * 发送数据到服务器
     *
     * @param iSuccess true 转发成功  false 转发失败
     * @param num      转发失败的状态吗
     */
    private void sendWebSocketData(boolean iSuccess, String... num) {
        SocketActionBean bean = new SocketActionBean();
        if (iSuccess) {
            bean.setAction("android_to_server_forwardok");
        } else {
            bean.setAction("android_to_server_forward_fail");
            bean.setError(num[0]);
        }
        bean.setAccount(account);
        bean.setWeiboid(weiBoID);
        String json = new Gson().toJson(bean);
        MessageService.sendData(json);
        EventBus.getDefault().post(EnumUtils.EVENT_TYPE.DISMISSWAITING);
    }
}
