package com.turbo.weiboh5.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by John_Doe on 2018/8/8.
 */

public class AndroidUtil {


    // 通过url得到图片的Bitmap数据
    public Bitmap GetUrlImage(String img_url){
        URL url;
        HttpURLConnection connection = null;
        Bitmap bitmap = null;
        try{
            url = new URL(img_url);
            connection = (HttpURLConnection)url.openConnection();
            connection.setConnectTimeout(6000);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            InputStream is = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
        }catch (Exception e){
            e.printStackTrace();
        }
        return bitmap;
    }

    // Bitmap数据转成byte数据为借口提交数据使用
    public byte[] getBytesByBitmap(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] datas = baos.toByteArray();
        return datas;
    }
}
