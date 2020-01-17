package com.turbo.weiboh5.bean;

/**
 * 文件名：ForwardBean
 * 作者：Turbo
 * 时间：2020-01-02 15:01
 * 蚁穴虽小，溃之千里。
 */
public class ForwardBean {

    /**
     * ok : 0
     * errno : 20031
     * msg : 请输入验证码
     * error_type : captcha
     */

    private int ok;
    private String errno;
    private String msg;
    private String error_type;

    public int getOk() {
        return ok;
    }

    public void setOk(int ok) {
        this.ok = ok;
    }

    public String getErrno() {
        return errno;
    }

    public void setErrno(String errno) {
        this.errno = errno;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getError_type() {
        return error_type;
    }

    public void setError_type(String error_type) {
        this.error_type = error_type;
    }
}
