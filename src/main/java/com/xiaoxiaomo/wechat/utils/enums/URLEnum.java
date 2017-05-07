package com.xiaoxiaomo.wechat.utils.enums;

/**
 * URL
 * Created by xiaoxiaomo on 2017/5/6.
 */
public enum URLEnum {

    BASE_URL("https://login.weixin.qq.com","基本的URL"),
    UUID_URL(BASE_URL.url+"/jslogin","UUIDL"),
    INIT_URL(BASE_URL.url+"/webwxinit?&r=","初始化"),
    MOBILE_URL(BASE_URL.url+"/webwxstatusnotify?lang=zh_CN&pass_ticket=","MOBILE"),
    LOGIN_URL(BASE_URL.url+"/cgi-bin/mmwebwx-bin/login","登陆"),
    QRCODE_URL(BASE_URL.url+"/qrcode/","初始化");

    private String url;
    private String msg;

    URLEnum(String url, String msg) {
        this.url = url;
        this.msg = msg;
    }


    public String getUrl() {
        return url;
    }
}
