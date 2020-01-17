package com.turbo.weiboh5.bean;

/**
 * 文件名：EventBean
 * 作者：Turbo
 * 时间：2020-01-03 09:57
 * 蚁穴虽小，溃之千里。
 */
public class EventBean {
    private String msg;
    private String errno;

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
}
