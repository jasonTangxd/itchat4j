package com.xiaoxiaomo.wechat.demo;

import com.alibaba.fastjson.JSONObject;
import com.xiaoxiaomo.wechat.WeChat;
import com.xiaoxiaomo.wechat.service.MsgService;
import com.xiaoxiaomo.wechat.utils.commmon.download.DownloadTools;
import com.xiaoxiaomo.wechat.utils.enums.MsgInfoEnum;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 简单示例程序，收到文本信息自动回复原信息，收到图片、语音、小视频后根据路径自动保存
 * 
 * @author https://github.com/yaphone
 * @date 创建时间：2017年4月25日 上午12:18:09
 * @version 1.0
 *
 */
public class SimpleDemo implements MsgService {

	@Override
	public String textMsgHandle(JSONObject msg) {
		String text = msg.getString("Text");
		return text;
	}

	@Override
	public String picMsgHandle(JSONObject msg) {
		String fileName = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
		String picPath = "D://itchat4j/pic" + File.separator + fileName + ".jpg";
		DownloadTools.getDownloadFn(msg, MsgInfoEnum.PIC.getCode(), picPath);
		return "图片保存成功";
	}

	@Override
	public String voiceMsgHandle(JSONObject msg) {
		String fileName = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
		String voicePath = "D://itchat4j/voice" + File.separator + fileName + ".mp3";
		DownloadTools.getDownloadFn(msg, MsgInfoEnum.VOICE.getCode(), voicePath);
		return "声音保存成功";
	}

	@Override
	public String videoMsgHandle(JSONObject msg) {
		String fileName = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
		String viedoPath = "D://itchat4j/viedo" + File.separator + fileName + ".mp4";
		DownloadTools.getDownloadFn(msg, MsgInfoEnum.VIDEO.getCode(), viedoPath);
		return "视频保存成功";
	}

	@Override
	public String nameCardMsgHandle(JSONObject msg) {
		return "收到名片消息";
	}

	public static void main(String[] args) {
		MsgService msgHandler = new SimpleDemo();
		WeChat wechat = new WeChat(msgHandler, "D://itchat4j/login");
		wechat.start();
	}

}
