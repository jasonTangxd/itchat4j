package com.xiaoxiaomo.wechat.utils.enums;

/**
 * 消息信息枚举类
 * <p>
 * Created by xiaoxiaomo on 2017/5/6.
 */
public enum MsgInfoEnum {

    TEXT("Text", "文本信息"),
    PIC("Pic", "图片信息"),
    VOICE("Voice", "语音信息"),
    NAME_CARD("NameCard", "明片消息"),
    VIDEO("Video", "小视频消息");

    private String code;
    private String msg;

    MsgInfoEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

//    public static MsgInfoEnum getCode(String code) {
//        switch (code) {
//            case "Text":
//                return MsgInfoEnum.TEXT;
//            default:
//                return MsgInfoEnum.VIDEO;
//        }
//    }

}
