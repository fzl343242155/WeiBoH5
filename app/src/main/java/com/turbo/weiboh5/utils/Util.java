package com.turbo.weiboh5.utils;

import android.util.Base64;
import android.util.Log;

import java.security.MessageDigest;

public class Util {

    public static class HttpResp {
        public int ret_code;
        public double cust_val;
        public String err_msg;
        public String req_id;
        public String rsp_data;
        public String pred_resl;
    }

    // 为避免引入复杂的json包，这里简单实现一个，只用来能解析从网络的回包中的指定字段的内容
    public static class JsonHelper {
        public String json;
        public int next_idx;

        public JsonHelper(String json) {
            this.json = json;
            this.next_idx = 0;
        }

        public void Skip() {
            while (next_idx < json.length()) {
                char c = json.charAt(next_idx);
                if ((c <= 32) || (c == '\\')) {
                    next_idx++;
                } else
                    break;
            }
        }

        public String NextSToken() {
            //int start = next_idx;
            String ret = "";
            while (next_idx < json.length()) {
                char c = json.charAt(next_idx);
                if (c == '\"')
                    break;
                if ((c == '\\') && (next_idx + 1 < json.length())) {
                    if (json.charAt(next_idx + 1) == '\\') {
                        ret += '\\';
                        next_idx += 2;
                        continue;
                    }
                    if (json.charAt(next_idx + 1) == '\"') {
                        ret += '\"';
                        next_idx += 2;
                        continue;
                    }
                }
                ret += c;
                next_idx++;
            }
            return ret;
        }

        public String NextNToken() {
            String ret = "";
            while (next_idx < json.length()) {
                char c = json.charAt(next_idx);
                if (c == '\\') {
                    next_idx++;
                    continue;
                }
                if ((c < '0' || c > '9') && c != '.') {
                    // not number
                    break;
                }
                ret += c;
                next_idx++;
            }
            return ret;
        }

        public void Key2Val(HttpResp rsp, String key, String val) {
            if (key.equals("RetCode")) {
                rsp.ret_code = Integer.parseInt(val);
            } else if (key.equals("ErrMsg")) {
                rsp.err_msg = val;
            } else if (key.equals("RequestId")) {
                rsp.req_id = val;
            } else if (key.equals("RspData")) {
                rsp.rsp_data = val;
            } else if (key.equals("result")) {
                rsp.pred_resl = val;
            } else if (key.equals("cust_val")) {
                rsp.cust_val = Double.parseDouble(val);
            }
        }

        public void Parse(HttpResp rsp) {
            //rsp.ret_code    = -1;
            next_idx = 0;
            String key = "";
            String sval = "";
            for (next_idx = 0; next_idx < json.length(); ) {
                Skip();
                char c = json.charAt(next_idx);
                switch (c) {
                    case ':':
                    case ',':
                        break;
                    case '[':
                    case '{':
                    case '}':
                    case ']':
                        // not support here
                        break;
                    case '\"':
                        next_idx++;
                        sval = NextSToken();
                        Skip();
                        if (next_idx >= json.length())
                            break;
                        if (json.charAt(next_idx + 1) == ':') {
                            key = sval;
                            next_idx++;
                            continue;
                        }
                        // key to val
                        Key2Val(rsp, key, sval);
                        key = "";
                        break;
                    case '+':
                    case '-':
                    case '.':
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                        // is number
                        sval = NextNToken();
                        // key to val
                        Key2Val(rsp, key, sval);
                        key = "";
                        break;
                    case 'n':
                    case 'N':
                        sval = json.substring(next_idx, 4).toLowerCase();
                        if (!sval.equals("null")) {
                            //error
                            break;
                        }
                        sval = "";
                        next_idx += 4;
                        // key to val
                        Key2Val(rsp, key, sval);
                        key = "";
                        break;
                    default:
                        break;
                }
                next_idx++;
            }
        }
    }

    public static String ToHex(byte[] arr) {
        StringBuffer md5str = new StringBuffer();
        int digital;
        for (int i = 0; i < arr.length; i++) {
            digital = arr[i];
            if (digital < 0) {
                digital += 256;
            }
            if (digital < 16) {
                md5str.append("0");
            }
            md5str.append(Integer.toHexString(digital));
        }
        return md5str.toString().toLowerCase();
    }

    public static String CalcMd5(String src) {
        String md5str = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] input = src.getBytes();
            byte[] buff = md.digest(input);
            md5str = ToHex(buff);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return md5str;
    }

    public static String CalcBase64(byte[] data) {
        String s = "";
        if (data != null) {
            s = Base64.encodeToString(data, Base64.DEFAULT);
        }
        return s;
    }

    public static String CalcSign(String id, String key, String tm) {
        String chk1 = CalcMd5(tm + key);
        String sum = CalcMd5(id + tm + chk1);
        return sum;
    }

    public static HttpResp ParseHttpResp(String resl) {
        Log.d("DEbug", "resl: " + resl);
        HttpResp resp = new HttpResp();
        resp.ret_code = -1;
        JsonHelper json = new JsonHelper(resl);
        json.Parse(resp);
        if (!resp.rsp_data.isEmpty()) {
            JsonHelper rjson = new JsonHelper(resp.rsp_data);
            rjson.Parse(resp);
        }
        return resp;
    }


}
