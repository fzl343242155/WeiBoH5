package com.turbo.weiboh5.bean;

/**
 * 文件名：User2Bean
 * 作者：Turbo
 * 时间：2020-01-02 11:25
 * 蚁穴虽小，溃之千里。
 */
public class User2Bean {

    /**
     * preferQuickapp : 0
     * data : {"login":true,"st":"9590c9","uid":"3299029533"}
     * ok : 1
     */

    private int preferQuickapp;
    private DataBean data;
    private int ok;

    public int getPreferQuickapp() {
        return preferQuickapp;
    }

    public void setPreferQuickapp(int preferQuickapp) {
        this.preferQuickapp = preferQuickapp;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public int getOk() {
        return ok;
    }

    public void setOk(int ok) {
        this.ok = ok;
    }

    public static class DataBean {
        /**
         * login : true
         * st : 9590c9
         * uid : 3299029533
         */

        private boolean login;
        private String st;
        private String uid;

        public boolean isLogin() {
            return login;
        }

        public void setLogin(boolean login) {
            this.login = login;
        }

        public String getSt() {
            return st;
        }

        public void setSt(String st) {
            this.st = st;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }
    }
}
