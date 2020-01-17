package com.turbo.weiboh5.utils;

import org.greenrobot.eventbus.EventBus;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

/**
 * 文件名：JWebSocketClient
 * 作者：Turbo
 * 时间：2019-12-20 16:19
 * 蚁穴虽小，溃之千里。
 */
public class JWebSocketClient extends WebSocketClient {

    public JWebSocketClient(URI serverURI) {
        super(serverURI);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        LogUtils.e("turbo", "onOpen: ");
        EventBus.getDefault().post(EnumUtils.EVENT_TYPE.SERVER_OPEN);
    }

    @Override
    public void onMessage(String message) {
        LogUtils.e("turbo", "onMessage: ");
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        LogUtils.e("turbo", "onClose: ");
        EventBus.getDefault().post(EnumUtils.EVENT_TYPE.CONN_ERROR);
        EventBus.getDefault().post(EnumUtils.EVENT_TYPE.SERVER_ERROR);
    }

    @Override
    public void onError(Exception ex) {
        LogUtils.e("turbo", "onError: " + ex.getMessage());
    }
}
