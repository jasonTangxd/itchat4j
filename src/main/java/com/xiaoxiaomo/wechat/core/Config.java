package com.xiaoxiaomo.wechat.core;

import com.xiaoxiaomo.wechat.utils.enums.OsNameEnum;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * 配置信息
 */
public class Config {
    public static final String picDir = "D://itchat4j";
    public static final String VERSION = "1.2.18";
    public static final String OS = "";
    public static final String DIR = "";
    public static final String DEFAULT_QR = "QR.jpg";

    public static final ArrayList<String> API_SPECIAL_USER = new ArrayList<String>(Arrays.asList("filehelper", "weibo",
            "qqmail", "fmessage", "tmessage", "qmessage", "qqsync", "floatbottle", "lbsapp", "shakeapp", "medianote",
            "qqfriend", "readerapp", "blogapp", "facebookapp", "masssendapp", "meishiapp", "feedsapp", "voip",
            "blogappweixin", "brandsessionholder", "weixin", "weixinreminder", "officialaccounts", "wxitil",
            "notification_messages", "wxid_novlwrv3lqwv11", "gh_22b87fa7cb3c", "userexperience_alarm", "a5ab9ecd0b648cd02b327a5c3e5fb3787787a1cbf65fa20f6c5e4ee6c5934b3b"));

    /**
     * 获取文件目录
     *
     * @return
     * @author https://github.com/yaphone
     * @date 2017年4月8日 下午10:27:42
     */
    public static String getLocalPath() {
        String localPath = null;
        try {
            localPath = new File("").getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return localPath;
    }

    /**
     * 获取系统平台
     *
     * @author https://github.com/yaphone
     * @date 2017年4月8日 下午10:27:53
     */
    public static OsNameEnum getOsName() {
        String os = System.getProperty("os.name").toUpperCase();
        if (os.indexOf(OsNameEnum.DARWIN.toString()) >= 0) {
            return OsNameEnum.DARWIN;
        } else if (os.indexOf(OsNameEnum.WINDOWS.toString()) >= 0) {
            return OsNameEnum.WINDOWS;
        } else if (os.indexOf(OsNameEnum.LINUX.toString()) >= 0) {
            return OsNameEnum.LINUX;
        } else if (os.indexOf(OsNameEnum.MAC.toString()) >= 0) {
            return OsNameEnum.MAC;
        }
        return OsNameEnum.OTHER;
    }

}
