package com.turbo.weiboh5;

/**
 * 文件名：URLs
 * 作者：Turbo
 * 时间：2020-01-13 16:47
 * 蚁穴虽小，溃之千里。
 */
public class URLs {

    //-----------------------------------websocket-----------------------------------//
    public static final String SOCKET_URL = "ws://192.168.40.17:10020/websocket";
//    public static final String SOCKET_URL = "ws://gatewaynew.bflyzx.com:10020/websocket";

    //-----------------------------------微博-----------------------------------//
    public static final String WEIBO_CONFIG_URL = "https://m.weibo.cn/api/config";

    public static final String WEIBO_SEARCH_URL = "https://m.weibo.cn/profile/info";
    public static final String WEIBO_FOCUS_URL = "https://m.weibo.cn/api/friendships/create";

    public static final String WEIBO_UPLOADPIC_URL = "https://m.weibo.cn/api/statuses/uploadPic";
    public static final String WEIBO_FORWARD_URL = "https://m.weibo.cn/api/statuses/repost";

    public static final String WEIBO_CODE_URL = "https://m.weibo.cn/api/captcha/show?t" + System.currentTimeMillis();

    public static final String GETCODE = "http://pred.fateadm.com/api/capreg";
    public static final String GETCODEs = "http://pred.fateadm.com/api/custval";

    //-----------------------------------打码平台-----------------------------------//
    public static final String PD_ID = "119630";
    public static final String PD_KEY = "Q0JlyTzaeABSLB69tUMnwpKCv7S2m3/i";

    public static final String APP_ID = "319630";
    public static final String APP_KEY = "K6nzUhO6A+fg5evcSTBk08ESya0anH1I";

    //-----------------------------------常量-----------------------------------//
    public static final String USERDATA_KEY = "USERDATA_KEY";
    public static final String CURRENT_USER = "CURRENT_USER";
    public static final String FORWARD_USER = "FORWARD_USER";
    public static final String PIC_PATH = "PIC_PATH";
    public static final String CLIENT_USER = "CLIENT_USER";


}
