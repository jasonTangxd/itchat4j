package com.xiaoxiaomo.wechat.service.imp;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xiaoxiaomo.wechat.core.Config;
import com.xiaoxiaomo.wechat.core.Storage;
import com.xiaoxiaomo.wechat.service.LoginService;
import com.xiaoxiaomo.wechat.utils.commmon.SleepUtils;
import com.xiaoxiaomo.wechat.utils.commmon.http.HttpClient;
import com.xiaoxiaomo.wechat.utils.enums.ResultEnum;
import com.xiaoxiaomo.wechat.utils.enums.URLEnum;
import com.xiaoxiaomo.wechat.utils.tools.CommonTool;
import com.xiaoxiaomo.wechat.utils.tools.MessageTools;
import org.apache.http.HttpEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;

/**
 * 登陆
 */
public class LoginServiceImp implements LoginService {

    private static Logger LOG = LoggerFactory.getLogger(LoginServiceImp.class);

    private static Storage storage = Storage.getInstance();

    public LoginServiceImp() {
    }

    /**
     * 生成UUID
     *
     * @return
     */
    @Override
    public String getUuid() {

        List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair("appid", "wx782c26e4c19acffb"));
        params.add(new BasicNameValuePair("fun", "new"));
        HttpEntity entity = HttpClient.doGet(URLEnum.UUID_URL.getUrl(), params, true, null);

        try {
            String result = EntityUtils.toString(entity);
            String regEx = "window.QRLogin.code = (\\d+); window.QRLogin.uuid = \"(\\S+?)\";";
            Matcher matcher = CommonTool.getMatcher(regEx, result);
            if (matcher.find()) {
                if ((ResultEnum.SUCCESS.getCode().equals(matcher.group(1)))) {
                    storage.setUuid(matcher.group(2));
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

        return storage.getUuid();
    }

    /**
     * 获取登陆二维码图片
     *
     * @param qrPath
     * @return
     */
    @Override
    public boolean getQR(String qrPath) {

        if (qrPath == null)
            qrPath = this.getClass().getResource("/img").getPath();
        qrPath += File.separator + "QR.jpg";

        String qrUrl = URLEnum.QRCODE_URL.getUrl() + storage.getUuid();
        HttpEntity entity = HttpClient.doGet(qrUrl, null, true, null);
        try {

            OutputStream out = new FileOutputStream(qrPath);
            byte[] bytes = EntityUtils.toByteArray(entity);
            out.write(bytes);
            out.flush();
            out.close();
            CommonTool.printQr(qrPath); // 打开登陆二维码图片

        } catch (Exception e) {
            LOG.error("获取登陆二维码图片失败", e);
            return false;
        }

        return true;
    }

    /**
     * 登陆
     *
     * @return
     */
    @Override
    public boolean login() {
        Long localTime = new Date().getTime();
        List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair("loginicon", "true"));
        params.add(new BasicNameValuePair("uuid", storage.getUuid()));
        params.add(new BasicNameValuePair("tip", "0"));
        params.add(new BasicNameValuePair("r", String.valueOf(localTime / 1579L)));
        params.add(new BasicNameValuePair("_", String.valueOf(localTime)));
        HttpEntity entity = HttpClient.doGet(URLEnum.LOGIN_URL.getUrl(), params, true, null);
        try {
            String result = EntityUtils.toString(entity);
            String status = checklogin(result);

            if (ResultEnum.SUCCESS.getCode().equals(status)) {
                LOG.info(("登陆成功"));
                processLoginInfo(result);   //处理结果
                return true;
            }

            if (ResultEnum.WAIT_CONFIRM.getCode().equals(status)) {
                LOG.info("请点击微信确认按钮，进行登陆");
                SleepUtils.sleep(10000);
                login();
            }

        } catch (Exception e) {
            LOG.error("微信登陆异常！" , e);
        }
        return false;
    }


    /**
     * 检查登陆状态
     *
     * @param result
     * @return
     */
    public String checklogin(String result) {
        String regEx = "window.code=(\\d+)";
        Matcher matcher = CommonTool.getMatcher(regEx, result);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 处理登陆信息
     * @param loginContent
     */
    public void processLoginInfo(String loginContent) {
        String regEx = "window.redirect_uri=\"(\\S+)\";";
        Matcher matcher = CommonTool.getMatcher(regEx, loginContent);
        if (matcher.find()) {
            String originalUrl = matcher.group(1);
            String url = originalUrl.substring(0, originalUrl.lastIndexOf('/')); // https://wx2.qq.com/cgi-bin/mmwebwx-bin
            storage.getLoginInfo().put("url", url);
            Map<String, List<String>> possibleUrlMap = getPossibleUrlMap();
            Iterator<Entry<String, List<String>>> iterator = possibleUrlMap.entrySet().iterator();
            Map.Entry<String, List<String>> entry;
            String fileUrl;
            String syncUrl;
            while (iterator.hasNext()) {
                entry = iterator.next();
                String indexUrl = entry.getKey();
                fileUrl = "https://" + entry.getValue().get(0) + "/cgi-bin/mmwebwx-bin";
                syncUrl = "https://" + entry.getValue().get(1) + "/cgi-bin/mmwebwx-bin";
                if (storage.getLoginInfo().get("url").toString().contains(indexUrl)) {
                    storage.getLoginInfo().put("fileUrl", fileUrl);
                    storage.getLoginInfo().put("syncUrl", syncUrl);
                    break;
                }
            }
            if (storage.getLoginInfo().get("fileUrl") == null && storage.getLoginInfo().get("syncUrl") == null) {
                storage.getLoginInfo().put("fileUrl", url);
                storage.getLoginInfo().put("syncUrl", url);
            }
            storage.getLoginInfo().put("deviceid", "e" + String.valueOf(new Random().nextLong()).substring(1, 16)); // 生成15位随机数
            storage.getLoginInfo().put("BaseRequest", new ArrayList<String>());
            String text = "";
            // // 禁止重定向

            try {
                HttpEntity entity = HttpClient.doGet(originalUrl, null, false, null);
                text = EntityUtils.toString(entity);
            } catch (Exception e) {
                LOG.info(e.getMessage());
                return;
            }
            Document doc = CommonTool.xmlParser(text);
            Map<String, Map<String, String>> BaseRequest = new HashMap<String, Map<String, String>>();
            Map<String, String> baseRequest = new HashMap<String, String>();
            if (doc != null) {
                storage.getLoginInfo().put("skey",
                        doc.getElementsByTagName("skey").item(0).getFirstChild().getNodeValue());
                baseRequest.put("Skey", (String) storage.getLoginInfo().get("skey"));
                storage.getLoginInfo().put("wxsid",
                        doc.getElementsByTagName("wxsid").item(0).getFirstChild().getNodeValue());
                baseRequest.put("Sid", (String) storage.getLoginInfo().get("wxsid"));
                storage.getLoginInfo().put("wxuin",
                        doc.getElementsByTagName("wxuin").item(0).getFirstChild().getNodeValue());
                baseRequest.put("Uin", (String) storage.getLoginInfo().get("wxuin"));
                storage.getLoginInfo().put("pass_ticket",
                        doc.getElementsByTagName("pass_ticket").item(0).getFirstChild().getNodeValue());
                baseRequest.put("DeviceID", (String) storage.getLoginInfo().get("pass_ticket"));
                BaseRequest.put("BaseRequest", baseRequest);
                storage.getLoginInfo().put("baseRequest", BaseRequest);
            }

        }
    }

    Map<String, List<String>> getPossibleUrlMap() {
        Map<String, List<String>> possibleUrlMap = new HashMap<String, List<String>>();
        possibleUrlMap.put("wx2.qq.com", new ArrayList<String>() {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            {
                add("file.wx2.qq.com");
                add("webpush.wx2.qq.com");
            }
        });
        possibleUrlMap.put("wx8.qq.com", new ArrayList<String>() {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            {
                add("file.wx8.qq.com");
                add("webpush.wx8.qq.com");
            }
        });

        possibleUrlMap.put("web2.wechat.com", new ArrayList<String>() {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            {
                add("file.web2.wechat.com");
                add("webpush.web2.wechat.com");
            }
        });
        possibleUrlMap.put("wechat.com", new ArrayList<String>() {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            {
                add("file.web.wechat.com");
                add("webpush.web.wechat.com");
            }
        });
        return possibleUrlMap;
    }

    @Override
    public JSONObject webInit() {
        JSONObject obj = null;
        String url = URLEnum.INIT_URL.getUrl() + String.valueOf(new Date().getTime());
        @SuppressWarnings("unchecked")
        Map<String, Map<String, String>> paramMap = (Map<String, Map<String, String>>) storage.getLoginInfo()
                .get("baseRequest");
        String paramStr = JSON.toJSONString(paramMap);
        try {
            HttpEntity entity = HttpClient.doPost(url, paramStr);
            String result = EntityUtils.toString(entity, "UTF-8");
            obj = JSON.parseObject(result);
            // TODO utils.emoji_formatter(dic['User'], 'NickName')
            storage.getLoginInfo().put("InviteStartCount", obj.getInteger("InviteStartCount"));
            storage.getLoginInfo().put("User", CommonTool.structFriendInfo(obj.getJSONObject("User"))); // 为userObj添加新字段
            storage.getLoginInfo().put("SyncKey", obj.getJSONObject("SyncKey"));
            JSONArray syncArray = obj.getJSONObject("SyncKey").getJSONArray("List");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < syncArray.size(); i++) {
                sb.append(syncArray.getJSONObject(i).getString("Key") + "_"
                        + syncArray.getJSONObject(i).getString("Val") + "|");
            }
            String synckey = sb.toString();
            storage.getLoginInfo().put("synckey", synckey.substring(0, synckey.length() - 1));// 1_656161336|2_656161626|3_656161313|11_656159955|13_656120033|201_1492273724|1000_1492265953|1001_1492250432|1004_1491805192
            storage.setUserName((obj.getJSONObject("User")).getString("UserName"));
            storage.setNickName((obj.getJSONObject("User")).getString("NickName"));
            storage.getUserSelfList().add(obj.getJSONObject("User"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    @Override
    public void showMobileLogin() {
        String mobileUrl = URLEnum.MOBILE_URL.getUrl() + (String) storage.getLoginInfo().get("pass_ticket");
        Map<String, Object> paramMap = new HashMap<String, Object>();
        @SuppressWarnings("unchecked")
        Map<String, Map<String, String>> baseRequestMap = (Map<String, Map<String, String>>) storage.getLoginInfo()
                .get("baseRequest");
        paramMap.put("BaseRequest", baseRequestMap.get("BaseRequest"));
        paramMap.put("Code", 3);
        paramMap.put("FromUserName", storage.getUserName());
        paramMap.put("ToUserName", storage.getUserName());
        paramMap.put("ClientMsgId", String.valueOf(new Date().getTime()));
        String paramStr = JSON.toJSONString(paramMap);
        try {
            HttpEntity entity = HttpClient.doPost(mobileUrl, paramStr);
            EntityUtils.toString(entity, "UTF-8");
        } catch (Exception e) {

        }
    }

    @Override
    public void startReceiving() {
        storage.setAlive(true);
        new Thread(new Runnable() {
            int retryCount = 0;

            @Override
            public void run() {
                while (storage.isAlive()) {
                    try {
                        String i = syncCheck();
                        if (i == null) {
                            storage.setAlive(false);
                        } else if (i.equals("0")) {
                            continue;
                        } else {
                            JSONArray msgList = new JSONArray();
                            JSONObject msgObj = getMsg();
                            if (msgObj != null) {
                                msgList = msgObj.getJSONArray("AddMsgList");
                                msgList = MessageTools.produceMsg(msgList);
                                for (int j = 0; j < msgList.size(); j++) {
                                    storage.getMsgList().add(msgList.getJSONObject(j));
                                }

                                // TODO chatroomMsg =
                                // update_local_chatrooms(self, chatroomList)
                                // TODO self.msgList.put(chatroomMsg)
                                // TODO update_local_friends(self, otherList)
                            }
                        }
                        retryCount = 0;

                    } catch (Exception e) {
                        LOG.info(e.getMessage());
                        retryCount += 1;
                        if (storage.getReceivingRetryCount() < retryCount) {
                            storage.setAlive(false);
                        } else {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e1) {
                                LOG.info(e.getMessage());
                            }
                        }
                    }

                }
            }
        }).start();
    }

    /**
     * 保活心跳
     *
     * @return
     * @author https://github.com/yaphone
     * @date 2017年4月16日 上午11:11:34
     */
    public String syncCheck() {
        String result = null;
        String syncUrl = (String) storage.getLoginInfo().get("syncUrl");
        // String syncUrl = "https://webpush.wx2.qq.com/cgi-bin/mmwebwx-bin";
        if (syncUrl == null || syncUrl.equals("")) {
            syncUrl = (String) storage.getLoginInfo().get("url");
        }
        String url = String.format("%s/synccheck", syncUrl);
        List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair("r", String.valueOf(new Date().getTime())));
        params.add(new BasicNameValuePair("skey", (String) storage.getLoginInfo().get("skey")));
        params.add(new BasicNameValuePair("sid", (String) storage.getLoginInfo().get("wxsid")));
        params.add(new BasicNameValuePair("uin", (String) storage.getLoginInfo().get("wxuin")));
        params.add(new BasicNameValuePair("deviceid", (String) storage.getLoginInfo().get("deviceid")));
        params.add(new BasicNameValuePair("synckey", (String) storage.getLoginInfo().get("synckey")));
        params.add(new BasicNameValuePair("_", String.valueOf(new Date().getTime())));
        try {
            HttpEntity entity = HttpClient.doGet(url, params, true, null);
            if (entity == null) {
                return "0";
            }
            String text = EntityUtils.toString(entity);
            String regEx = "window.synccheck=\\{retcode:\"(\\d+)\",selector:\"(\\d+)\"\\}";
            Matcher matcher = CommonTool.getMatcher(regEx, text);
            if (!matcher.find() || matcher.group(1).equals("2")) {
                LOG.info(String.format("Unexpected sync check result: %s", text));
            } else {
                result = matcher.group(2);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    JSONObject getMsg() {
        JSONObject result = new JSONObject();
        String url = String.format("%s/webwxsync?sid=%s&skey=%s&pass_ticket=%s", storage.getLoginInfo().get("url"),
                storage.getLoginInfo().get("wxsid"), storage.getLoginInfo().get("skey"),
                storage.getLoginInfo().get("pass_ticket"));
        Map<String, Object> paramMap = new HashMap<String, Object>();
        @SuppressWarnings("unchecked")
        Map<String, Map<String, String>> baseRequestMap = (Map<String, Map<String, String>>) storage.getLoginInfo()
                .get("baseRequest");
        paramMap.put("BaseRequest", baseRequestMap.get("BaseRequest"));
        paramMap.put("SyncKey", storage.getLoginInfo().get("SyncKey"));
        paramMap.put("rr", -new Date().getTime() / 1000);
        String paramStr = JSON.toJSONString(paramMap);
        try {
            HttpEntity entity = HttpClient.doPost(url, paramStr);
            String text = EntityUtils.toString(entity, "UTF-8");
            JSONObject obj = JSON.parseObject(text);
            if (obj.getJSONObject("BaseResponse").getInteger("Ret") != 0) {
                result = null;
            } else {
                result = obj;
                storage.getLoginInfo().put("SyncKey", obj.getJSONObject("SyncCheckKey"));
                JSONArray syncArray = obj.getJSONObject("SyncKey").getJSONArray("List");
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < syncArray.size(); i++) {
                    sb.append(syncArray.getJSONObject(i).getString("Key") + "_"
                            + syncArray.getJSONObject(i).getString("Val") + "|");
                }
                String synckey = sb.toString();
                storage.getLoginInfo().put("synckey", synckey.substring(0, synckey.length() - 1));// 1_656161336|2_656161626|3_656161313|11_656159955|13_656120033|201_1492273724|1000_1492265953|1001_1492250432|1004_1491805192
            }
        } catch (Exception e) {
            LOG.info(e.getMessage());
        }
        return result;

    }

    /**
     * <p>
     * 获取联系人信息，成功返回true，失败返回false
     * </p>
     * <p>
     * get all contacts: people, group, public user, special user
     * </p>
     *
     * @return
     * @author https://github.com/yaphone
     * @date 2017年5月3日 上午12:28:51
     */
    @Override
    public boolean webWxGetContact() {
        String result = "";
        String url = String.format("%s/webwxgetcontact", storage.getLoginInfo().get("url"));
        List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair("pass_ticket", (String) storage.getLoginInfo().get("pass_ticket")));
        params.add(new BasicNameValuePair("skey", (String) storage.getLoginInfo().get("skey")));
        params.add(new BasicNameValuePair("r", String.valueOf(String.valueOf(new Date().getTime()))));
        HttpEntity entity = HttpClient.doGet(url, params, true, null);
        try {
            result = EntityUtils.toString(entity, "UTF-8");
        } catch (Exception e) {
            LOG.info(e.getMessage());
        }
        JSONObject fullFriendsJsonList = JSON.parseObject(result);
        storage.setMemberCount(fullFriendsJsonList.getInteger(("MemberCount")));
        JSONArray memberJsonArray = fullFriendsJsonList.getJSONArray("MemberList");
        for (int i = 0; i < memberJsonArray.size(); i++) {
            storage.getMemberList().add(memberJsonArray.getJSONObject(i));
        }
        for (JSONObject o : storage.getMemberList()) {
            if ((o.getInteger("VerifyFlag") & 8) != 0) { // 公众号/服务号
                storage.getPublicUsersList().add(o);
            } else if (Config.API_SPECIAL_USER.contains(o.getString("UserName"))) { // 特殊账号
                storage.getSpecialUsersList().add(o);
            } else if (o.getString("UserName").indexOf("@@") != -1) { // 群聊
                storage.getGroupList().add(o);
            } else if (o.getString("UserName").equals(storage.getUserSelfList().get(0).getString("UserName"))) { // 自己
                storage.getContactList().remove(o);
            } else { // 普通联系人
                storage.getContactList().add(o);
            }
        }
        return true;
    }

}
