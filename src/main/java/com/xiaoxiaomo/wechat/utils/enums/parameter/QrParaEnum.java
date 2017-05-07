package com.xiaoxiaomo.wechat.utils.enums.parameter;

/**
 * 二维码请求参数
 * <p>
 * Created by xiaoxiaomo on 2017/5/7.
 */
public enum QrParaEnum {

    T("t", "webwx"),
    _("_", "时间戳");

    private String para;
    private String value;

    QrParaEnum(String para, String value) {
        this.para = para;
        this.value = value;
    }

    public String para() {
        return para;
    }

    public String value() {
        return value;
    }
}
