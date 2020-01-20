package com.turbo.weiboh5.utils;

/**
 * 文件名：EnumUtils
 * 作者：Turbo
 * 时间：2019-12-31 12:40
 * 蚁穴虽小，溃之千里。
 */
public class EnumUtils {
    /**
     * 账号系统登录的类型
     */
    public enum EVENT_TYPE {
        LOGIN_SUCCESS, LOGIN_ERROR, SEARCH_SUCCESS, FOCUS_SUCCESS, FORWARD_SUCCESS, FORWARD_ERROR, UPLOAD_SUCCESS, CODE, REFRESH_DATA, FORWARD_START, CONN_ERROR,
        PIC_NULL, SERVER_ERROR, SERVER_OPEN,DISMISSWAITING
    }

    public enum FORWARD_TYPE {
        FORWARD_LOG_1,
        FORWARD_LOG_2,FORWARD_LOG_2_1,FORWARD_LOG_2_2,
        FORWARD_LOG_3,
        FORWARD_LOG_4,FORWARD_LOG_4_1,
        FORWARD_LOG_5,
        FORWARD_LOG_6,
        FORWARD_LOG_7,
        FORWARD_LOG_8,
        FORWARD_LOG_9,
        FORWARD_LOG_10,
        FORWARD_LOG_11,
        FORWARD_LOG_12,
        FORWARD_LOG_13,
        FORWARD_LOG_14,
        FORWARD_LOG_15,
        FORWARD_LOG_16,
        FORWARD_LOG_17,
        FORWARD_LOG_18,
        FORWARD_LOG_19,
        FORWARD_LOG_20,
        FORWARD_LOG_21,
        FORWARD_LOG_22,
        FORWARD_LOG_23,
        FORWARD_LOG_24
    }
}
