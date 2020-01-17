package com.turbo.weiboh5.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 文件名：DataBean
 * 作者：Turbo
 * 时间：2020-01-13 16:52
 * 蚁穴虽小，溃之千里。
 */
@Entity
public class DataBean {
    @Id
    private Long id;
    @Property
    private String account;
    @Property
    private String cookie;
    @Property
    private String st;
    @Property
    private String status;
    @Generated(hash = 112901056)
    public DataBean(Long id, String account, String cookie, String st,
            String status) {
        this.id = id;
        this.account = account;
        this.cookie = cookie;
        this.st = st;
        this.status = status;
    }
    @Generated(hash = 908697775)
    public DataBean() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getAccount() {
        return this.account;
    }
    public void setAccount(String account) {
        this.account = account;
    }
    public String getCookie() {
        return this.cookie;
    }
    public void setCookie(String cookie) {
        this.cookie = cookie;
    }
    public String getSt() {
        return this.st;
    }
    public void setSt(String st) {
        this.st = st;
    }
    public String getStatus() {
        return this.status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}
