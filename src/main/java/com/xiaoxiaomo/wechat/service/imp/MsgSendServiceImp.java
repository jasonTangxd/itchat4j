package com.xiaoxiaomo.wechat.service.imp;

import com.alibaba.fastjson.JSON;
import com.xiaoxiaomo.wechat.core.MsgCenter;
import com.xiaoxiaomo.wechat.core.Storage;
import com.xiaoxiaomo.wechat.service.MsgSendService;
import com.xiaoxiaomo.wechat.utils.commmon.http.HttpClient;
import com.xiaoxiaomo.wechat.utils.enums.MsgTypeEnum;
import com.xiaoxiaomo.wechat.utils.enums.URLEnum;
import com.xiaoxiaomo.wechat.utils.enums.parameter.MsgParaEnum;
import com.xiaoxiaomo.wechat.utils.enums.parameter.MsgSendParaEnum;
import com.xiaoxiaomo.wechat.utils.enums.storage.StorageLoginInfoEnum;
import com.xiaoxiaomo.wechat.utils.tools.WeChatTools;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 消息发送类
 *
 * Created by xiaoxiaomo on 2017/5/7.
 */
public class MsgSendServiceImp implements MsgSendService {

    private static Logger LOG = LoggerFactory.getLogger(MsgSendServiceImp.class);

    private static Storage storage = Storage.getInstance();


    @Override
    public void send(String msg, String toUserName, String mediaId) {
        sendMsg(msg, toUserName);
    }

    /**
     *
     * 根据UserName发送文本消息
     *
     * @param text
     * @param toUserName
     */
    @Override
    public  void sendMsg(String text, String toUserName) {
        LOG.info(String.format("Request to send a text message to %s: %s", toUserName, text));
        sendRawMsg(MsgTypeEnum.TEXT, text, toUserName);
    }

    /**
     * 根据ID发送文本消息
     *
     * @param text
     * @param id
     */
    @Override
    public void sendMsgById(String text, String id) {
        sendMsg(text, id);
    }

    /**
     * 根据NickName发送文本消息
     *
     * @param text
     * @param nickName
     */
    @Override
    public boolean sendMsgByNickName(String text, String nickName) {
        if (nickName != null) {
            String toUserName = WeChatTools.getUserNameByNickName(nickName);
            return sendRawMsg(MsgTypeEnum.TEXT, text, toUserName);
        }
        return false;

    }

    /**
     * 消息发送
     *
     * @param msgType 枚举
     * @param content
     * @param toUserName
     */
    @Override
    public boolean sendRawMsg(MsgTypeEnum msgType, String content, String toUserName) {

        //组装消息参数和URL
        String url = String.format(URLEnum.WEB_WX_SEND_MSG.getUrl(),
                storage.getLoginInfo().get(StorageLoginInfoEnum.url.getKey()));

        Map<String, Object> paramMap =storage.getParamMap() ;
        Map<String, Object> msgMap = new HashMap<String, Object>();
        msgMap.put(MsgParaEnum.TYPE.para(), msgType.getCode());
        msgMap.put(MsgParaEnum.CONTENT.para(), content);
        msgMap.put(MsgParaEnum.FROM_USERNAME.para(), storage.getUserName());
        msgMap.put(MsgParaEnum.TO_USERNAME.para(), toUserName == null ? storage.getUserName() : toUserName);
        msgMap.put(MsgParaEnum.LOCAL_ID.para(), new Date().getTime() * 10);
        msgMap.put(MsgParaEnum.CLIENT_MSG_ID.para(), new Date().getTime() * 10);
        paramMap.put(MsgSendParaEnum.MSG.para(), msgMap);
        paramMap.put(MsgSendParaEnum.SCENE.para(), MsgSendParaEnum.SCENE.value());
        String paramStr = JSON.toJSONString(paramMap);

        try {
            HttpEntity entity = HttpClient.doPost(url, paramStr);
            EntityUtils.toString(entity, "UTF-8");
            return true;
        } catch (Exception e) {
            LOG.error("消息发送" , e);
        }
        return false ;
    }

}
