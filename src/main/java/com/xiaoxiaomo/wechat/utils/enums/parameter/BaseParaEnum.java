package com.xiaoxiaomo.wechat.utils.enums.parameter;

/**
 *
 * 基本请求参数
 * 1. webWxInit      初始化
 * 2. wxStatusNotify 微信状态通知
 *
 * <p>
 * Created by xiaoxiaomo on 2017/5/7.
 */
public enum BaseParaEnum {

    Skey("Skey", "skey"),
    Sid("Sid", "wxsid"),
    Uin("Uin", "wxuin"),
    DeviceID("DeviceID", "pass_ticket");

    private String para;
    private String value;

    BaseParaEnum(String para, String value) {
        this.para = para;
        this.value = value;
    }

    public String para() {
        return para;
    }


    public Object value() {
        return value;
    }

}
