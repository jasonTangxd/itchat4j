package com.xiaoxiaomo.wechat.demo;

import com.xiaoxiaomo.wechat.WeChat;

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
		WeChat wechat = new WeChat("D://itchat4j/login");
		wechat.start();
	}

}
