package com.xiaoxiaomo.wechat;

import com.alibaba.fastjson.JSONObject;
import com.xiaoxiaomo.wechat.controller.LoginController;
import com.xiaoxiaomo.wechat.core.Storage;
import com.xiaoxiaomo.wechat.service.MsgService;
import com.xiaoxiaomo.wechat.utils.enums.MsgInfoEnum;
import com.xiaoxiaomo.wechat.utils.tools.MessageTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * 主类，初始化工作
 *
 * @version 2.0
 * @date 创建时间：2017年4月25日 上午12:42:54
 */
public class WeChat {
    private static final Logger LOG = LoggerFactory.getLogger(WeChat.class);
    private static Storage core = Storage.getInstance();

    private MsgService msgService;

    public WeChat(MsgService msgService, String qrPath) {
        System.setProperty("jsse.enableSNIExtension", "false"); // 防止SSL错误
        this.msgService = msgService;

        LOG.debug("生成二维码图片");
        LoginController login = new LoginController();
        login.login(qrPath);

    }

    public void start() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    if (core.getMsgList().size() > 0
                            && core.getMsgList().get(0).containsKey("Content")) {

                        JSONObject msg = core.getMsgList().get(0);

                        if (MsgInfoEnum.TEXT.getCode().equals(msg.getString("Type"))) {
                            String result = msgService.textMsgHandle(msg);
                            MessageTools.send(result, msg.getString("FromUserName"), "");
                        }

                        if (MsgInfoEnum.PIC.getCode().equals(msg.getString("Type"))) {
                            String result = msgService.picMsgHandle(msg);
                            MessageTools.send(result, msg.getString("FromUserName"), "");
                        }

                        if (MsgInfoEnum.VOICE.getCode().equals(msg.getString("Type"))) {
                            String result = msgService.voiceMsgHandle(msg);
                            MessageTools.send(result, msg.getString("FromUserName"), "");
                        }

                        if (MsgInfoEnum.VIDEO.getCode().equals(msg.getString("Type"))) {
                            String result = msgService.videoMsgHandle(msg);
                            MessageTools.send(result, msg.getString("FromUserName"), "");
                        }

                        if (MsgInfoEnum.NAME_CARD.getCode().equals(msg.getString("Type"))) {
                            String result = msgService.nameCardMsgHandle(msg);
                            MessageTools.send(result, msg.getString("FromUserName"), "");
                        }
                        core.getMsgList().remove(0);
                    }
                    try {
                        TimeUnit.MILLISECONDS.sleep(1000);
                    } catch (InterruptedException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            }
        }).start();
    }

}
