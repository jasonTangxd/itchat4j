package com.xiaoxiaomo.wechat.service;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by xiaoxiaomo on 2017/5/7.
 */
public interface LoginService {
    String getUuid();

    boolean getQR(String qrPath);

    boolean login();

    JSONObject webInit();

    void showMobileLogin();

    void startReceiving();

    boolean webWxGetContact();
}
