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
    FRIEND_SURE_MSG(37,"好友确认消息"),
    POSSIBLE_FRIEND_MSG(40,"POSSIBLEFRIEND_MSG"),
    SHARE_CARD(42,"共享名片"),
    VIDEO(43,"视频消息"),
    EMOTICON(47,"动画表情"),
    LOCATION(48,"位置消息"),
    APP(49,"分享链接"),
    VOIP_MSG(50,"VOIPMSG"),
    STATUS_NOTIFY(51,"微信初始化消息"),
    VOIP_NOTIFY(52,"VOIPNOTIFY"),
    VOIP_INVITE(53,"VOIPINVITE"),
    MICRO_VIDEO(62,"小视频"),
    SYS_NOTICE(9999,"系统提示"),
    SYS_MSG(10000,"系统消息"),
    RECALLED(10002,"撤回消息");

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
