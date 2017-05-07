package com.xiaoxiaomo.wechat.demo;

import com.xiaoxiaomo.wechat.WeChat;
import com.xiaoxiaomo.wechat.service.MsgHandleService;
import com.xiaoxiaomo.wechat.service.imp.MsgHandleServiceImp;

/**
 * 简单示例程序，收到文本信息自动回复原信息，收到图片、语音、小视频后根据路径自动保存
 * 
 * @author https://github.com/yaphone
 * @date 创建时间：2017年4月25日 上午12:18:09
 * @version 1.0
 *
 */
public class SimpleDemo  {

	public static void main(String[] args) {
		MsgHandleService msgHandler = new MsgHandleServiceImp();
		WeChat wechat = new WeChat(msgHandler, "D://itchat4j/login");
		wechat.start();
	}

}
