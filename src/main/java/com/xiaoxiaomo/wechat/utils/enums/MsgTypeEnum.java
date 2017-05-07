package com.xiaoxiaomo.wechat.utils.enums;

/**
 * 消息类型
 * <p>
 * Created by xiaoxiaomo on 2017/5/6.
 */
public enum MsgTypeEnum {

    TEXT(1,"文本消息类型"),
    IMAGE(3,"图片消息"),
    VOICE(34,"语音消息"),
    POSSIBLE_FRIEND_MSG(37,""),
    SHARE_CARD(42,""),
    VIDEO(43,"小视频消息"),
    EMOTICON(47,""),
    LOCATION(48,""),
    APP(49,""),
    VOIP_MSG(50,""),
    STATUS_NOTIFY(51,""),
    VOIP_NOTIFY(52,""),
    VOIP_INVITE(53,""),
    MICRO_VIDEO(62,""),
    SYS_NOTICE(9999,""),
    SYS(10000,""),
    RECALLED(10002,"");

    private int code;
    private String msg;

    MsgTypeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }
}
