package com.xiaoxiaomo.wechat.utils.enums.parameter;

/**
 * 发送消息
 * <p>
 * Created by xiaoxiaomo on 2017/5/7.
 */
public enum MsgSendParaEnum {

    MSG("Msg", ""),
    SCENE("Scene", "0");

    private String para;
    private String value;

    MsgSendParaEnum(String para, String value) {
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
