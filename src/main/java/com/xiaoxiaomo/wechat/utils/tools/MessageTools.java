package com.xiaoxiaomo.wechat.utils.tools;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;

import com.xiaoxiaomo.wechat.utils.enums.MsgInfoEnum;
import com.xiaoxiaomo.wechat.utils.enums.MsgTypeEnum;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.xiaoxiaomo.wechat.core.Storage;
import com.xiaoxiaomo.wechat.utils.commmon.http.HttpClient;

/**
 * 消息处理类
 * 
 * @author https://github.com/yaphone
 * @date 创建时间：2017年4月23日 下午2:30:37
 * @version 1.0
 *
 */
public class MessageTools {
	private static Logger logger = Logger.getLogger("Message");
	private static Storage core = Storage.getInstance();

	/**
	 * 接收消息，放入队列
	 * 
	 * @author https://github.com/yaphone
	 * @date 2017年4月23日 下午2:30:48
	 * @param msgList
	 * @return
	 */
	public static JSONArray produceMsg(JSONArray msgList) {
		JSONArray result = new JSONArray();
		for (int i = 0; i < msgList.size(); i++) {
			JSONObject msg = new JSONObject();
			JSONObject m = msgList.getJSONObject(i);
			if (m.getString("FromUserName").contains("@@") || m.getString("ToUserName").contains("@@")) { // 群聊消息
				// produceGroupChat(core, m);
				// m.remove("Content");
				if (m.getString("FromUserName").contains("@@")
						&& !core.getGroupIdList().contains(m.getString("FromUserName"))) {
					core.getGroupIdList().add((m.getString("FromUserName")));
				} else if (m.getString("ToUserName").contains("@@")
						&& !core.getGroupIdList().contains(m.getString("ToUserName"))) {
					core.getGroupIdList().add((m.getString("ToUserName")));
				}
				// 群消息与普通消息不同的是在其消息体（Content）中会包含发送者id及":<br/>"消息，这里需要处理一下，去掉多余信息，只保留消息内容
				if (m.getString("Content").contains("<br/>")) {
					String content = m.getString("Content").substring(m.getString("Content").indexOf("<br/>") + 5);
					m.put("Content", content);
				}
			} else {
				CommonTool.msgFormatter(m, "Content");
			}
			if (m.getInteger("MsgTypeEnum") == MsgTypeEnum.TEXT.getCode()) { // words 文本消息
				if (m.getString("Url").length() != 0) {
					String regEx = "(.+?\\(.+?\\))";
					Matcher matcher = CommonTool.getMatcher(regEx, m.getString("Content"));
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
			} else if (m.getInteger("MsgTypeEnum") == MsgTypeEnum.IMAGE.getCode()
					|| m.getInteger("MsgTypeEnum") == MsgTypeEnum.EMOTICON.getCode()) { // 图片消息
				m.put("Type", MsgInfoEnum.PIC.getCode());
			} else if (m.getInteger("MsgTypeEnum") == MsgTypeEnum.VOICE.getCode()) { // 语音消息
				m.put("Type", MsgInfoEnum.VOICE.getCode());
			} else if (m.getInteger("MsgTypeEnum") == 37) {// friends 好友确认消息

			} else if (m.getInteger("MsgTypeEnum") == 42) { // 共享名片
				m.put("Type", MsgInfoEnum.NAME_CARD.getCode());

			} else if (m.getInteger("MsgTypeEnum") == MsgTypeEnum.VIDEO.getCode()
					|| m.getInteger("MsgTypeEnum") == MsgTypeEnum.MICRO_VIDEO.getCode()) {// viedo
				m.put("Type", MsgTypeEnum.VIDEO.getCode());
			} else if (m.getInteger("MsgTypeEnum") == 49) { // sharing 分享链接

			} else if (m.getInteger("MsgTypeEnum") == 51) {// phone init 微信初始化消息

			} else if (m.getInteger("MsgTypeEnum") == 10000) {// 系统消息

			} else if (m.getInteger("MsgTypeEnum") == 10002) { // 撤回消息

			} else {
				logger.info("Useless msg");
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
		logger.info(String.format("Request to send a text message to %s: %s", toUserName, text));
		sendRawMsg(1, text, toUserName);
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
			String toUserName = WechatTools.getUserNameByNickName(nickName);
			sendRawMsg(1, text, toUserName);
			return true;
		}
		return false;

	}

	/**
	 * 消息发送
	 * 
	 * @author https://github.com/yaphone
	 * @date 2017年4月23日 下午2:32:02
	 * @param msgType
	 * @param content
	 * @param toUserName
	 */
	public static void sendRawMsg(int msgType, String content, String toUserName) {
		String url = String.format("%s/webwxsendmsg", core.getLoginInfo().get("url"));

		Map<String, Object> paramMap = new HashMap<String, Object>();
		@SuppressWarnings("unchecked")
		Map<String, Map<String, String>> baseRequestMap = (Map<String, Map<String, String>>) core.getLoginInfo()
				.get("baseRequest");
		paramMap.put("BaseRequest", baseRequestMap.get("BaseRequest"));
		Map<String, Object> msgMap = new HashMap<String, Object>();
		msgMap.put("Type", msgType);
		msgMap.put("Content", content);
		msgMap.put("FromUserName", core.getUserName());
		msgMap.put("ToUserName", toUserName == null ? core.getUserName() : toUserName);
		msgMap.put("LocalID", new Date().getTime() * 10);
		msgMap.put("ClientMsgId", new Date().getTime() * 10);
		paramMap.put("Msg", msgMap);
		paramMap.put("Scene", 0);
		try {
			String paramStr = JSON.toJSONString(paramMap);
			HttpEntity entity = HttpClient.doPost(url, paramStr);
			EntityUtils.toString(entity, "UTF-8");
		} catch (Exception e) {
			logger.info(e.getMessage());
		}

	}

}
