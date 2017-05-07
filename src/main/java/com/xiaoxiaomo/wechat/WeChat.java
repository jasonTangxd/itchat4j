package com.xiaoxiaomo.wechat;

import com.xiaoxiaomo.wechat.controller.LoginController;
import com.xiaoxiaomo.wechat.core.MsgCenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * 主类，初始化工作
 */
public class WeChat {
    private static final Logger LOG = LoggerFactory.getLogger(WeChat.class);

    public WeChat(String qrPath) {

        System.setProperty("jsse.enableSNIExtension", "false"); // 防止SSL错误

        //登陆
        LoginController login = new LoginController();
        login.login(qrPath);
    }

    public void start() {

        LOG.info("+++++++++++++++++++消息发送服务开始启动+++++++++++++++++++++");
        new Thread(new Runnable() {
            @Override
            public void run() {
                new MsgCenter().sendMsg();
            }
        }).start();
    }

}
