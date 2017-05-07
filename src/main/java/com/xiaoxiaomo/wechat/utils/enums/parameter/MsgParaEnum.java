package com.xiaoxiaomo.wechat.utils.enums.parameter;

/**
 * 消息
 * <p>
 * Created by xiaoxiaomo on 2017/5/7.
 */
public enum MsgParaEnum {

    TYPE("Type", ""),
    CONTENT("Content", ""),
    FROM_USERNAME("FromUserName", ""),
    TO_USERNAME("ToUserName", ""),
    LOCAL_ID("LocalID", ""),//与clientMsgId相同
    CLIENT_MSG_ID("ClientMsgId", "");//时间戳左移4位随后补上4位随机数

    private String para;
    private String value;

    MsgParaEnum(String para, String value) {
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
