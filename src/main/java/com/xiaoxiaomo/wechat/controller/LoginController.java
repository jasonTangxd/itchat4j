package com.xiaoxiaomo.wechat.controller;

import com.alibaba.fastjson.JSONObject;
import com.xiaoxiaomo.wechat.core.Storage;
import com.xiaoxiaomo.wechat.service.LoginService;
import com.xiaoxiaomo.wechat.service.imp.LoginServiceImp;
import com.xiaoxiaomo.wechat.utils.commmon.SleepUtils;
import com.xiaoxiaomo.wechat.utils.enums.ResultEnum;
import com.xiaoxiaomo.wechat.utils.tools.CommonTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 登陆流程控制中心
 * <p>
 * Created by xiaoxiaomo on 2017/5/7.
 */
public class LoginController {
    private static Logger LOG = LoggerFactory.getLogger(LoginController.class);

    private LoginService loginService = new LoginServiceImp();
    private static Storage core = Storage.getInstance();

    public String login(String qrPath) {

        if (core.isAlive()) { // 已登陆
            LOG.warn("WeChat has already logged in.");
            return ResultEnum.SUCCESS.getCode();
        }

        while (true) {
            for (int count = 0; count < 10; count++) {
                LOG.info("1. 获取微信UUID");
                while (loginService.getUuid() == null) {
                    LOG.warn("1.1. 获取微信UUID失败，两秒后重新获取");
                    SleepUtils.sleep(2000);
                }

                LOG.info("2. 获取登陆二维码图片");
                if (loginService.getQR(qrPath)) {
                    break;
                } else if (count == 10) {
                    LOG.error("2.2. 获取登陆二维码图片失败，系统退出");
                    System.exit(0);
                }
            }

            LOG.info("3. 请扫描二维码图片，并在手机上确认");
            if (loginService.login()){
                core.setAlive(true);
                break;
            }
            LOG.info("4. 登陆超时，请重新扫描二维码图片");
        }


        LOG.info("5. 登陆成功，微信初始化");
        if( !loginService.webWxInit() ){
            LOG.info("6. 微信初始化异常");
            System.exit(0);
        }


        LOG.info("6. 开启微信状态通知");
        loginService.wxStatusNotify();


        LOG.info("6. 清除。。。。");
        CommonTools.clearScreen();
        LOG.info(String.format("LoginServiceImp successfully as %s", core.getNickName()));


        LOG.info("7. 接收消息");
        loginService.startReceiving();

        LOG.info("8. 获取联系人信息");
        loginService.webWxGetContact();
        return ResultEnum.SUCCESS.getCode();
    }
}
