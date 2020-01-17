package com.turbo.weiboh5.utils;

import com.lzy.okgo.convert.Converter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import okhttp3.Response;

/**
 * 文件名：TStringConvert
 * 作者：Turbo
 * 时间：2020-01-02 14:20
 * 蚁穴虽小，溃之千里。
 */
public class TStringConvert implements Converter<String> {

    @Override
    public String convertResponse(Response response) throws Throwable {
        String result = "";
        InputStream inputStream = response.body().byteStream();
        result = readResponseString(inputStream, "gzip");
        return result;
    }

    /**
     * 读取服务器返回数据
     *
     * @param inputStream 服务器返回读入流
     * @return 返回字符串
     */
    private static String readResponseString(InputStream inputStream, String encoding) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len = 0;
        try {
            // 处理GZIP压缩
            if (encoding != null && encoding.equals("gzip"))
                inputStream = new GZIPInputStream(inputStream);
            while ((len = inputStream.read(data)) != -1)
                byteArrayOutputStream.write(data, 0, len);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String ret = new String(byteArrayOutputStream.toByteArray());
        return ret;

    }
}
