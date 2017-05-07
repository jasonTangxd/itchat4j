package com.xiaoxiaomo.wechat.service;

import com.xiaoxiaomo.wechat.utils.enums.MsgTypeEnum;

/**
 * 发送消息服务
 * <p>
 * Created by xiaoxiaomo on 2017/5/7.
 */
public interface MsgSendService {
    void send(String msg, String toUserName, String mediaId);

    void sendMsg(String text, String toUserName);

    void sendMsgById(String text, String id);

    boolean sendMsgByNickName(String text, String nickName);

    boolean sendRawMsg(MsgTypeEnum msgType, String content, String toUserName);
}
