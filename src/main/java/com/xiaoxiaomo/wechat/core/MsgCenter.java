package com.xiaoxiaomo.wechat.core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xiaoxiaomo.wechat.utils.commmon.http.HttpClient;
import com.xiaoxiaomo.wechat.utils.enums.MsgInfoEnum;
import com.xiaoxiaomo.wechat.utils.enums.MsgTypeEnum;
import com.xiaoxiaomo.wechat.utils.enums.URLEnum;
import com.xiaoxiaomo.wechat.utils.enums.parameter.MsgParaEnum;
import com.xiaoxiaomo.wechat.utils.enums.parameter.MsgSendParaEnum;
import com.xiaoxiaomo.wechat.utils.enums.storage.StorageLoginInfoEnum;
import com.xiaoxiaomo.wechat.utils.tools.CommonTools;
import com.xiaoxiaomo.wechat.utils.tools.WeChatTools;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;

/**
 *
 * 消息中心
 * 
 */
public class MsgCenter {

    private static Logger LOG = LoggerFactory.getLogger(MsgCenter.class);

	private static Storage storage = Storage.getInstance();

	/**
	 * 接收消息，放入队列
	 * 
	 * @return
	 */
	public static JSONArray produceMsg(JSONArray produceMsg) {

        JSONArray result = new JSONArray();

        for (Iterator iterator = produceMsg.iterator(); iterator.hasNext();) {

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

	public static void send(String msg, String toUserName, String mediaId) {
		sendMsg(msg, toUserName);
	}

	/**
	 *
	 * 根据UserName发送文本消息
	 * 
	 * @param text
	 * @param toUserName
	 */
	public static void sendMsg(String text, String toUserName) {
		LOG.info(String.format("Request to send a text message to %s: %s", toUserName, text));
		sendRawMsg(MsgTypeEnum.TEXT, text, toUserName);
	}

	/**
	 * 根据ID发送文本消息
	 * 
	 * @param text
	 * @param id
	 */
	public static void sendMsgById(String text, String id) {
		sendMsg(text, id);
	}

	/**
	 * 根据NickName发送文本消息
	 * 
	 * @author https://github.com/yaphone
	 * @date 2017年5月4日 下午11:17:38
	 * @param text
	 * @param nickName
	 */
	public static boolean sendMsgByNickName(String text, String nickName) {
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
	public static boolean sendRawMsg(MsgTypeEnum msgType, String content, String toUserName) {

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
