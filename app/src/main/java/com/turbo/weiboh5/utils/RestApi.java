package com.turbo.weiboh5.utils;

import android.app.Application;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheEntity;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.interceptor.HttpLoggingInterceptor;
import com.lzy.okgo.model.HttpHeaders;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import okhttp3.OkHttpClient;

/**
 * 文件名：RestApi
 * 作者：Turbo
 * 时间：2020-01-13 16:47
 * 蚁穴虽小，溃之千里。
 */
public class RestApi {

    public static void initOkGO(Application context) {
        //---------这里给出的是示例代码,告诉你可以这么传,实际使用的时候,根据需要传,不需要就不传-------------//
        HttpHeaders headers = new HttpHeaders();
        headers.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/11.1.2 Safari/605.1.15");
        //header不支持中文
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor("OkGo");
        //log打印级别，决定了log显示的详细程度
        loggingInterceptor.setPrintLevel(true ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);
        //log颜色级别，决定了log在控制台显示的颜色
        loggingInterceptor.setColorLevel(Level.INFO);
        builder.addInterceptor(loggingInterceptor);
        //全局的读取超时时间 30s
        builder.readTimeout(OkGo.DEFAULT_MILLISECONDS / 2, TimeUnit.MINUTES);
        //全局的写入超时时间 30s
        builder.writeTimeout(OkGo.DEFAULT_MILLISECONDS / 2, TimeUnit.MINUTES);
        //全局的连接超时时间 30s
        builder.connectTimeout(OkGo.DEFAULT_MILLISECONDS / 2, TimeUnit.MINUTES);

        OkGo.getInstance().init(context)
                .setOkHttpClient(builder.build())
                .setCacheMode(CacheMode.NO_CACHE)
                .setCacheTime(CacheEntity.CACHE_NEVER_EXPIRE)
                .setRetryCount(3)
                .addCommonHeaders(headers);
        //-----------------------------------------------------------------------------------//
    }
}
