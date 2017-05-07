package com.xiaoxiaomo.wechat.core;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xiaoxiaomo.wechat.service.MsgHandleService;
import com.xiaoxiaomo.wechat.service.MsgSendService;
import com.xiaoxiaomo.wechat.service.imp.MsgHandleServiceImp;
import com.xiaoxiaomo.wechat.service.imp.MsgSendServiceImp;
import com.xiaoxiaomo.wechat.service.imp.TuLingRobotImp;
import com.xiaoxiaomo.wechat.utils.commmon.SleepUtils;
import com.xiaoxiaomo.wechat.utils.enums.MsgInfoEnum;
import com.xiaoxiaomo.wechat.utils.enums.MsgTypeEnum;
import com.xiaoxiaomo.wechat.utils.tools.CommonTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.regex.Matcher;

/**
 * 消息中心
 */
public class MsgCenter {

    private static Logger LOG = LoggerFactory.getLogger(MsgCenter.class);

    private static Storage storage = Storage.getInstance();
    private MsgHandleService msgHandle;
    private MsgSendService msgSend;

    public MsgCenter() {
        this.msgHandle = new MsgHandleServiceImp();
//        this.msgHandle = new TuLingRobotImp();
        this.msgSend = new MsgSendServiceImp();
    }

    /**
     * 接收消息，放入队列
     *
     * @return
     */
    public static JSONArray produceMsg(JSONArray produceMsg) {

        JSONArray result = new JSONArray();

        for (Iterator iterator = produceMsg.iterator(); iterator.hasNext(); ) {

            JSONObject msg = new JSONObject();
            JSONObject m = (JSONObject) iterator.next();
            if (m.getString("FromUserName").contains("@@") || m.getString("ToUserName").contains("@@")) { // 群聊消息
                // produceGroupChat(storage, m);
                // m.remove("Content");

                if (m.getString("FromUserName").contains("@@")
                        && !storage.getGroupIdList().contains(m.getString("FromUserName"))) {
                    storage.getGroupIdList().add((m.getString("FromUserName")));
                } else if (m.getString("ToUserName").contains("@@")
                        && !storage.getGroupIdList().contains(m.getString("ToUserName"))) {
                    storage.getGroupIdList().add((m.getString("ToUserName")));
                }
                // 群消息与普通消息不同的是在其消息体（Content）中会包含发送者id及":<br/>"消息，这里需要处理一下，去掉多余信息，只保留消息内容
                if (m.getString("Content").contains("<br/>")) {
                    String content = m.getString("Content").substring(m.getString("Content").indexOf("<br/>") + 5);
                    m.put("Content", content);
                }
            } else {
                CommonTools.msgFormatter(m, "Content");
            }


            if (m.getInteger("MsgType") == MsgTypeEnum.TEXT.getCode()) { // words 文本消息
                if (m.getString("Url").length() != 0) {
                    String regEx = "(.+?\\(.+?\\))";
                    Matcher matcher = CommonTools.getMatcher(regEx, m.getString("Content"));
                    String data = "Map";
                    if (matcher.find()) {
                        data = matcher.group(1);
                    }
                    msg.put("Type", "Map");
                    msg.put("Text", data);
                } else {
                    msg.put("Type", MsgInfoEnum.TEXT.getCode());
                    msg.put("Text", m.getString("Content"));
                }
                m.put("Type", msg.getString("Type"));
                m.put("Text", msg.getString("Text"));
            } else if (m.getInteger("MsgType") == MsgTypeEnum.IMAGE.getCode()
                    || m.getInteger("MsgType") == MsgTypeEnum.EMOTICON.getCode()) { // 图片消息
                m.put("Type", MsgInfoEnum.PIC.getCode());
            } else if (m.getInteger("MsgType") == MsgTypeEnum.VOICE.getCode()) { // 语音消息
                m.put("Type", MsgInfoEnum.VOICE.getCode());
            } else if (m.getInteger("MsgType") == 37) {// friends 好友确认消息

            } else if (m.getInteger("MsgType") == 42) { // 共享名片
                m.put("Type", MsgInfoEnum.NAME_CARD.getCode());

            } else if (m.getInteger("MsgType") == MsgTypeEnum.VIDEO.getCode()
                    || m.getInteger("MsgType") == MsgTypeEnum.MICRO_VIDEO.getCode()) {// viedo
                m.put("Type", MsgTypeEnum.VIDEO.getCode());
            } else if (m.getInteger("MsgType") == 49) { // sharing 分享链接

            } else if (m.getInteger("MsgType") == 51) {// phone init 微信初始化消息

            } else if (m.getInteger("MsgType") == 10000) {// 系统消息

            } else if (m.getInteger("MsgType") == 10002) { // 撤回消息

            } else {
                LOG.info("Useless msg");
            }
            result.add(m);
        }
        return result;
    }


    /**
     * 发送消息
     */
    public void sendMsg() {
        while (true) {
            if (storage.getMsgList().size() <= 0 || storage.getMsgList().get(0).containsKey("Content")) {
                SleepUtils.sleep(1000);
            }

            JSONObject msg = storage.getMsgList().get(0);

            if (MsgInfoEnum.TEXT.getCode().equals(msg.getString("Type"))) {
                String result = msgHandle.textMsgHandle(msg);
                msgSend.send(result, msg.getString("FromUserName"), "");
            }

            if (MsgInfoEnum.PIC.getCode().equals(msg.getString("Type"))) {
                String result = msgHandle.picMsgHandle(msg);
                msgSend.send(result, msg.getString("FromUserName"), "");
            }

            if (MsgInfoEnum.VOICE.getCode().equals(msg.getString("Type"))) {
                String result = msgHandle.voiceMsgHandle(msg);
                msgSend.send(result, msg.getString("FromUserName"), "");
            }

            if (MsgInfoEnum.VIDEO.getCode().equals(msg.getString("Type"))) {
                String result = msgHandle.videoMsgHandle(msg);
                msgSend.send(result, msg.getString("FromUserName"), "");
            }

            if (MsgInfoEnum.NAME_CARD.getCode().equals(msg.getString("Type"))) {
                String result = msgHandle.nameCardMsgHandle(msg);
                msgSend.send(result, msg.getString("FromUserName"), "");
            }
            storage.getMsgList().remove(0);

        }

    }

}
