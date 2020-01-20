package com.turbo.weiboh5.bean;

import java.util.List;

/**
 * 文件名：SocketActionBean
 * 作者：Turbo
 * 时间：2020-01-10 17:39
 * 蚁穴虽小，溃之千里。
 */
public class SocketActionBean {

    private String action;
    private String type;
    private String username;
    private String account;
    private String pcid;
    private String weiboid;
    private String id;
    private String error;
    private List<KeyBean> data;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPcid() {
        return pcid;
    }

    public void setPcid(String pcid) {
        this.pcid = pcid;
    }

    public List<KeyBean> getData() {
        return data;
    }

    public void setData(List<KeyBean> data) {
        this.data = data;
    }

    public String getWeiboid() {
        return weiboid;
    }

    public void setWeiboid(String weiboid) {
        this.weiboid = weiboid;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
