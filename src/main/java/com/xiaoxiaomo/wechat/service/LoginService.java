package com.xiaoxiaomo.wechat.service;

import com.alibaba.fastjson.JSONObject;

import java.util.Map;

/**
 *
 * 登陆处理接口
 *
 * Created by xiaoxiaomo on 2017/5/7.
 */
public interface LoginService {

    /** 获取UUID */
    String getUuid();

    /** 获取二维码 */
    boolean getQR(String qrPath);

    /** 登陆 */
    boolean login();

    /** web初始化 */
    JSONObject webWxInit();

    /** 微信状态通知 */
    void wxStatusNotify();

    /** 接收消息 */
    void startReceiving();

    /** 获取联系人信息 */
    boolean webWxGetContact();

}
