package com.xiaoxiaomo.wechat.service;

import com.alibaba.fastjson.JSONObject;

/**
 * 消息处理接口
 *
 */
public interface MsgHandleService {

	/**
	 * 处理文本
	 * @param msg
	 * @return
	 */
	String textMsgHandle(JSONObject msg);

	/**
	 * 处理图片消息
	 * @param msg
	 * @return
	 */
	String picMsgHandle(JSONObject msg);

	/**
	 * 处理声音消息
	 * 
	 * @param msg
	 * @return
	 */
	String voiceMsgHandle(JSONObject msg);

	/**
	 * 处理小视频消息
	 * 
	 * @param msg
	 * @return
	 */
	String videoMsgHandle(JSONObject msg);

	/**
	 * 处理名片消息
	 * 
	 * @param msg
	 * @return
	 */
	String nameCardMsgHandle(JSONObject msg);

}
