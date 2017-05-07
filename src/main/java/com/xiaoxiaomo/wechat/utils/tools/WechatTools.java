package com.xiaoxiaomo.wechat.utils.tools;

import com.alibaba.fastjson.JSONObject;
import com.xiaoxiaomo.wechat.core.Storage;

import java.util.ArrayList;
import java.util.List;

/**
 * 微信小工具，如获好友列表等
 */
public class WeChatTools {

    private static Storage core = Storage.getInstance();

    /**
     * 获取好友列表，JSONObject格式
     *
     * @return
     * @author https://github.com/yaphone
     * @date 2017年5月4日 下午10:55:18
     */
    private static List<JSONObject> getJsonContactList() {
        return core.getContactList();
    }

    /**
     * <p>
     * 通过RealName获取本次UserName
     * </p>
     * <p>
     * 如NickName为"yaphone"，则获取UserName=
     * "@1212d3356aea8285e5bbe7b91229936bc183780a8ffa469f2d638bf0d2e4fc63"，
     * 可通过UserName发送消息
     * </p>
     *
     * @param nickName
     * @return
     * @author https://github.com/yaphone
     * @date 2017年5月4日 下午10:56:31
     */
    public static String getUserNameByNickName(String nickName) {
        for (JSONObject o : core.getContactList()) {
            if (o.getString("NickName").equals(nickName)) {
                return o.getString("UserName");
            }
        }
        return null;
    }

    /**
     * 返回好友昵称列表
     *
     * @return
     * @author https://github.com/yaphone
     * @date 2017年5月4日 下午11:37:20
     */
    public static List<String> getContactList() {
        List<String> contactList = new ArrayList<String>();
        for (JSONObject o : core.getContactList()) {
            contactList.add(o.getString("NickName"));
        }
        return contactList;
    }

    /**
     * 返回群列表
     *
     * @return
     * @author https://github.com/yaphone
     * @date 2017年5月5日 下午9:55:21
     */
    public static List<String> getGroupList() {
        List<String> groupList = new ArrayList<String>();
        for (JSONObject o : core.getGroupList()) {
            groupList.add(o.getString("Name"));
        }
        return groupList;
    }

    public static List<String> getGroupIdList() {
        return core.getGroupIdList();
    }

}
