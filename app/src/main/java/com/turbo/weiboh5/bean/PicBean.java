package com.turbo.weiboh5.bean;

/**
 * 文件名：PicBean
 * 作者：Turbo
 * 时间：2020-01-02 14:39
 * 蚁穴虽小，溃之千里。
 */
public class PicBean {


    /**
     * pic_id : c4a3321dly1gai7j5abmnj20cf08zgvd
     * thumbnail_pic : http://wx1.sinaimg.cn/thumbnail/c4a3321dly1gai7j5abmnj20cf08zgvd.jpg
     * bmiddle_pic : http://wx1.sinaimg.cn/bmiddle/c4a3321dly1gai7j5abmnj20cf08zgvd.jpg
     * original_pic : http://wx1.sinaimg.cn/large/c4a3321dly1gai7j5abmnj20cf08zgvd.jpg
     */

    private String pic_id;
    private String thumbnail_pic;
    private String bmiddle_pic;
    private String original_pic;

    public String getPic_id() {
        return pic_id;
    }

    public void setPic_id(String pic_id) {
        this.pic_id = pic_id;
    }

    public String getThumbnail_pic() {
        return thumbnail_pic;
    }

    public void setThumbnail_pic(String thumbnail_pic) {
        this.thumbnail_pic = thumbnail_pic;
    }

    public String getBmiddle_pic() {
        return bmiddle_pic;
    }

    public void setBmiddle_pic(String bmiddle_pic) {
        this.bmiddle_pic = bmiddle_pic;
    }

    public String getOriginal_pic() {
        return original_pic;
    }

    public void setOriginal_pic(String original_pic) {
        this.original_pic = original_pic;
    }
}
