package com.turbo.weiboh5.service;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.gson.Gson;
import com.turbo.weiboh5.URLs;
import com.turbo.weiboh5.bean.DataBean;
import com.turbo.weiboh5.bean.KeyBean;
import com.turbo.weiboh5.bean.SocketActionBean;
import com.turbo.weiboh5.bean.User2Bean;
import com.turbo.weiboh5.utils.DataUtils;
import com.turbo.weiboh5.utils.EnumUtils;
import com.turbo.weiboh5.utils.HttpServer;
import com.turbo.weiboh5.utils.JWebSocketClient;
import com.turbo.weiboh5.utils.LogUtils;
import com.turbo.weiboh5.utils.WeiBoUtils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rx.functions.Action1;
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

    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(new Runnable() {
            @Override
            public void run() {
                startRotation();
            }
        }).start();
    }

    public static void sendData(String json) {
        if (client != null && client.isOpen()) {
            LogUtils.e(TAG, "sendData: " + json);
            client.send(json);
        } else {
            LogUtils.e(TAG, "sendData: client = null or client.isopen = false");
            EventBus.getDefault().post(EnumUtils.EVENT_TYPE.SERVER_ERROR);
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
                }
                try {
                    client.connectBlocking();
                } catch (Exception e) {
                    e.printStackTrace();
                    client = null;
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

                    bean = new SocketActionBean();
                    bean.setAction("android_to_server_deleteaccountok");
                    bean.setAccount(d_account);
                    MessageService.sendData(new Gson().toJson(bean));
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
                    ExecutorService executorService = Executors.newCachedThreadPool();//创建一个可缓存线程池，如果线程池长度超过处理需要，可灵活回收空闲线程，若无可回收，则新建线程。
                    executorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            WeiBoUtils weiBoUtils = new WeiBoUtils(f_account_n, forwardcontent_n, weiboid_n);
                            weiBoUtils.onForward();
                        }
                    });
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
