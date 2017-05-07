package com.xiaoxiaomo.wechat.service;

/**
 *
 * 登陆处理接口服务
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
    boolean webWxInit();

    /** 微信状态通知 */
    void wxStatusNotify();

    /** 接收消息 */
    void startReceiving();

    /** 获取联系人信息 */
    boolean webWxGetContact();

}
