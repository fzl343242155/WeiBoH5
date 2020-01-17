package com.turbo.weiboh5.utils;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.convert.StringConvert;
import com.lzy.okrx.adapter.ObservableBody;
import com.turbo.weiboh5.URLs;

import java.io.File;

import rx.Observable;

/**
 * @author ：fangzhiliang
 * 时间：2019/4/4 12:35 PM
 * 开心对待每一天，真心对待每一个
 */
public class HttpServer {

    private HttpServer() {
    }

    private static class SingletonHolder {
        private static final HttpServer INSTANCE = new HttpServer();
    }

    public static HttpServer $() {
        return HttpServer.SingletonHolder.INSTANCE;
    }

    /**
     * 获取st
     *
     * @return
     */
    public Observable<String> weibo_config(String cookie) {
        return OkGo.<String>get(URLs.WEIBO_CONFIG_URL)
                .removeAllHeaders()
                .headers("cookie", cookie)
                .converter(new StringConvert())
                .adapt(new ObservableBody<String>());
    }

    /**
     * 搜索
     *
     * @param uid
     * @return
     */
    public Observable<String> weibo_search(String uid) {
        return OkGo.<String>get(URLs.WEIBO_SEARCH_URL)
                .headers("Accept", "application/json, text/plain")
                .headers("MWeibo-Pwa", "1")
                .headers("Referer", "https://m.weibo.cn/")
                .headers("X-Requested-With", "XMLHttpRequest")
                .headers("Referer", "https://m.weibo.cn/profile/" + uid)
                .params("uid", uid)
                .converter(new StringConvert())
                .adapt(new ObservableBody<String>());
    }

    /**
     * 关注
     *
     * @param uid
     * @param st
     * @return
     */
    public Observable<String> weibo_focus(String uid, String st) {
        String cookie = DataUtils.getInstance().getCookie();
        return OkGo.<String>post(URLs.WEIBO_FOCUS_URL)
                .headers("Origin", "https://m.weibo.cn")
                .headers("Host", "m.weibo.cn")
                .headers("Connection", "keep-alive")
                .headers("Accept", "application/json, text/plain, */*")
                .headers("Accept-Encoding", "br, gzip, deflate")
                .headers("Referer", "https://m.weibo.cn/")
                .headers("Accept-Language", "zh-cn")
                .headers("MWeibo-Pwa", "1")
                .headers("cookie", cookie)
                .headers("X-Requested-With", "XMLHttpRequest")
                .params("uid", uid)
                .params("st", st)
                .converter(new TStringConvert())
                .adapt(new ObservableBody<String>());
    }

    /**
     * 上传图片
     *
     * @return
     */
    public Observable<String> weibo_uploadPic(String id, String st, File file) {
        String cookie = DataUtils.getInstance().getCookie();
        return OkGo.<String>post(URLs.WEIBO_UPLOADPIC_URL)
                .headers("Accept", "*/*")
                .headers("Accept-Encoding", "gzip, deflate, br")
                .headers("Accept-Language", "zh-CN,zh;q=0.9")
                .headers("mweibo-pwa", "1")
                .headers("Content-Type", "multipart/form-data")
                .headers("cookie", cookie)
                .headers("Origin", "https://m.weibo.cn")
                .headers("Referer", "https://m.weibo.cn/compose/repost?id=" + id)
                .headers("Sec-Fetch-Mode", "cors")
                .headers("Sec-Fetch-Site", "same-origin")
                .headers("x-requested-with", "XMLHttpRequest")
                .headers("x-xsrf-token", st)
                .params("pic", file, file.getName())
                .params("type", "json")
                .params("st", st)
                .converter(new TStringConvert())
                .adapt(new ObservableBody<String>());
    }

    /**
     * 转发
     *
     * @return
     */
    public Observable<String> weibo_forward(String id, String st, String content, String picid, String code) {
        String cookie = DataUtils.getInstance().getCookie();
        return OkGo.<String>post(URLs.WEIBO_FORWARD_URL)
                .headers("Accept", "*/*")
                .headers("Accept-Encoding", "gzip, deflate, br")
                .headers("Accept-Language", "zh-CN,zh;q=0.9")
                .headers("Content-Type", "application/x-www-form-urlencoded")
                .headers("mweibo-pwa", "1")
                .headers("cookie", cookie)
                .headers("Origin", "https://m.weibo.cn")
                .headers("Referer", "https://m.weibo.cn/compose/repost?id=" + id + "&pids=" + picid)
                .headers("Sec-Fetch-Mode", "cors")
                .headers("Sec-Fetch-Site", "same-origin")
                .headers("x-requested-with", "XMLHttpRequest")
                .headers("x-xsrf-token", st)
                .params("id", id)
                .params("content", content)
                .params("mid", id)
                .params("picId", picid)
                .params("st", st)
                .params("_code", code)
                .converter(new TStringConvert())
                .adapt(new ObservableBody<String>());
    }

    /**
     * 获取验证码
     *
     * @return
     */
    public Observable<String> getCode(String pic, String sign, String asign, String time) {
        return OkGo.<String>post(URLs.GETCODE)
                .params("user_id", URLs.PD_ID)
                .params("timestamp", time)
                .params("sign", sign)
                .params("asign", asign)
                .params("predict_type", "30400")
                .params("img_data", pic)
                .converter(new StringConvert())
                .adapt(new ObservableBody<String>());
    }

    /**
     * 获取余额
     *
     * @return
     */
    public Observable<String> getBalance(String sign, String tm) {
        return OkGo.<String>post(URLs.GETCODEs)
                .params("user_id", URLs.PD_ID)
                .params("timestamp", tm)
                .params("sign", sign)
                .converter(new StringConvert())
                .adapt(new ObservableBody<String>());
    }



}
