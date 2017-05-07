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
import com.xiaoxiaomo.wechat.utils.enums.storage.BaseRequestEnum;
import com.xiaoxiaomo.wechat.utils.enums.storage.StorageLoginInfoEnum;
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
            LOG.error("微信登陆异常！", e);
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
     *
     * @param loginContent
     */
    public void processLoginInfo(String loginContent) {
        String regEx = "window.redirect_uri=\"(\\S+)\";";
        Matcher matcher = CommonTool.getMatcher(regEx, loginContent);
        if (matcher.find()) {
            String originalUrl = matcher.group(1);

            //保存URL
            String url = originalUrl.substring(0, originalUrl.lastIndexOf('/')); // https://wx2.qq.com/cgi-bin/mmwebwx-bin
            storage.getLoginInfo().put(StorageLoginInfoEnum.url.getKey(), url);
            storage.getLoginInfo().put(StorageLoginInfoEnum.fileUrl.getKey(), url);
            storage.getLoginInfo().put(StorageLoginInfoEnum.syncUrl.getKey(), url);

            Map<String, List<String>> possibleUrlMap = getPossibleUrlMap();
            Iterator<Entry<String, List<String>>> iterator = possibleUrlMap.entrySet().iterator();
            Map.Entry<String, List<String>> entry;
            while (iterator.hasNext()) {
                entry = iterator.next();
                if (storage.getLoginInfo().get(StorageLoginInfoEnum.url.getKey()).toString().contains(entry.getKey())) {
                    storage.getLoginInfo().put(StorageLoginInfoEnum.fileUrl.getKey(), "https://" + entry.getValue().get(0) + "/cgi-bin/mmwebwx-bin");
                    storage.getLoginInfo().put(StorageLoginInfoEnum.syncUrl.getKey(), "https://" + entry.getValue().get(1) + "/cgi-bin/mmwebwx-bin");
                    break;
                }
            }

            storage.getLoginInfo().put(StorageLoginInfoEnum.deviceid.getKey(), "e" + String.valueOf(new Random().nextLong()).substring(1, 16));

            // // 禁止重定向
            String text = "";
            try {
                HttpEntity entity = HttpClient.doGet(originalUrl, null, false, null);
                text = EntityUtils.toString(entity);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
                return;
            }
            Document doc = CommonTool.xmlParser(text);
            if (doc != null) {
                //4
                storage.getLoginInfo().put(StorageLoginInfoEnum.skey.getKey(),
                        doc.getElementsByTagName(StorageLoginInfoEnum.skey.getKey()).item(0).getFirstChild().getNodeValue());
                storage.getLoginInfo().put(StorageLoginInfoEnum.wxsid.getKey(),
                        doc.getElementsByTagName(StorageLoginInfoEnum.wxsid.getKey()).item(0).getFirstChild().getNodeValue());
                storage.getLoginInfo().put(StorageLoginInfoEnum.wxuin.getKey(),
                        doc.getElementsByTagName(StorageLoginInfoEnum.wxuin.getKey()).item(0).getFirstChild().getNodeValue());
                storage.getLoginInfo().put(StorageLoginInfoEnum.pass_ticket.getKey(),
                        doc.getElementsByTagName(StorageLoginInfoEnum.pass_ticket.getKey()).item(0).getFirstChild().getNodeValue());
            }

        }
    }

    Map<String, List<String>> getPossibleUrlMap() {
        Map<String, List<String>> possibleUrlMap = new HashMap<String, List<String>>();
        possibleUrlMap.put("wx2.qq.com", new ArrayList<String>() {
            private static final long serialVersionUID = 1L;

            {
                add("file.wx2.qq.com");
                add("webpush.wx2.qq.com");
            }
        });
        possibleUrlMap.put("wx8.qq.com", new ArrayList<String>() {
            private static final long serialVersionUID = 1L;

            {
                add("file.wx8.qq.com");
                add("webpush.wx8.qq.com");
            }
        });

        possibleUrlMap.put("web2.wechat.com", new ArrayList<String>() {
            private static final long serialVersionUID = 1L;

            {
                add("file.web2.wechat.com");
                add("webpush.web2.wechat.com");
            }
        });
        possibleUrlMap.put("wechat.com", new ArrayList<String>() {
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

        //组装请求URL和参数
        String url = URLEnum.INIT_URL.getUrl() + String.valueOf(new Date().getTime());
        Map<String, Map<String, String>> paramMap = new HashMap<String, Map<String, String>>(1) {
            {
                Map<String, String> map = new HashMap<String, String>();
                for (BaseRequestEnum baseRequest : BaseRequestEnum.values()) {
                    map.put(baseRequest.getKey(), storage.getLoginInfo().get(baseRequest.getStorageKey()).toString());
                }
                put("baseRequest", map);
            }
        };

        //请求初始化接口
        HttpEntity entity = HttpClient.doPost(url, JSON.toJSONString(paramMap));
        try {
            String result = EntityUtils.toString(entity, "UTF-8");
            JSONObject obj = JSON.parseObject(result);


            LOG.info(obj.toJSONString());//调试
            JSONObject user = obj.getJSONObject(StorageLoginInfoEnum.User.getKey());
            JSONObject syncKey = obj.getJSONObject(StorageLoginInfoEnum.SyncKey.getKey());

            storage.getLoginInfo().put(StorageLoginInfoEnum.InviteStartCount.getKey(), obj.getInteger(StorageLoginInfoEnum.InviteStartCount.getKey()));
            storage.getLoginInfo().put(StorageLoginInfoEnum.User.getKey(), CommonTool.structFriendInfo(user)); // 为userObj添加新字段//// TODO: 2017/5/7  多余
            storage.getLoginInfo().put(StorageLoginInfoEnum.SyncKey.getKey(), syncKey);

            JSONArray syncArray = syncKey.getJSONArray("List");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < syncArray.size(); i++) {
                sb.append(syncArray.getJSONObject(i).getString("Key") + "_"
                        + syncArray.getJSONObject(i).getString("Val") + "|");
            }
            String synckey = sb.toString();

            storage.getLoginInfo().put(StorageLoginInfoEnum.synckey.getKey(), synckey.substring(0, synckey.length() - 1));// 1_656161336|2_656161626|3_656161313|11_656159955|13_656120033|201_1492273724|1000_1492265953|1001_1492250432|1004_1491805192
            storage.setUserName(user.getString("UserName"));
            storage.setNickName(user.getString("NickName"));
            storage.getUserSelfList().add(obj.getJSONObject("User"));//// TODO: 2017/5/7  多余
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void showMobileLogin() {
        String mobileUrl = URLEnum.MOBILE_URL.getUrl() + storage.getLoginInfo().get(StorageLoginInfoEnum.pass_ticket.getKey());
        Map<String, Object> paramMap = new HashMap<String, Object>();
        Map<String, Map<String, String>> baseRequest = new HashMap<String, Map<String, String>>(1) {
            {
                Map<String, String> map = new HashMap<String, String>();
                for (BaseRequestEnum baseRequest : BaseRequestEnum.values()) {
                    map.put(baseRequest.getKey(), storage.getLoginInfo().get(baseRequest.getStorageKey()).toString());
                }
                put("baseRequest", map);
            }
        };

        paramMap.put("BaseRequest", baseRequest);
        paramMap.put("Code", 3);
        paramMap.put("FromUserName", storage.getUserName());
        paramMap.put("ToUserName", storage.getUserName());
        paramMap.put("ClientMsgId", String.valueOf(new Date().getTime()));
        String paramStr = JSON.toJSONString(paramMap);
        try {
            HttpEntity entity = HttpClient.doPost(mobileUrl, paramStr);
            EntityUtils.toString(entity, "UTF-8");
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
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
                                LOG.error(e.getMessage());
                            }
                        }
                    }

                }
            }
        }).start();
    }


    /**
     * 保活心跳
     * <p>
     * String syncUrl = "https://webpush.wx2.qq.com/cgi-bin/mmwebwx-bin";
     *
     * @return
     * @author https://github.com/yaphone
     * @date 2017年4月16日 上午11:11:34
     */
    public String syncCheck() {

        //组装请求URL和参数
        String url = storage.getLoginInfo().get(StorageLoginInfoEnum.syncUrl.getKey()) + URLEnum.SYNC_CHECK_URL.getUrl();
        List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair("r", String.valueOf(new Date().getTime())));
        params.add(new BasicNameValuePair(StorageLoginInfoEnum.skey.getKey(), (String) storage.getLoginInfo().get(StorageLoginInfoEnum.skey.getKey())));
        params.add(new BasicNameValuePair(BaseRequestEnum.Sid.getKey(), (String) storage.getLoginInfo().get(StorageLoginInfoEnum.wxsid.getKey())));
        params.add(new BasicNameValuePair(BaseRequestEnum.Uin.getKey(), (String) storage.getLoginInfo().get(StorageLoginInfoEnum.wxuin.getKey())));
        params.add(new BasicNameValuePair(StorageLoginInfoEnum.deviceid.getKey(), (String) storage.getLoginInfo().get(StorageLoginInfoEnum.deviceid.getKey())));
        params.add(new BasicNameValuePair(StorageLoginInfoEnum.synckey.getKey(), (String) storage.getLoginInfo().get(StorageLoginInfoEnum.synckey.getKey())));
        params.add(new BasicNameValuePair("_", String.valueOf(new Date().getTime())));

        HttpEntity entity = HttpClient.doGet(url, params, true, null);
        try {
            if (entity == null) {
                return "0";
            }
            String text = EntityUtils.toString(entity);
            String regEx = "window.synccheck=\\{retcode:\"(\\d+)\",selector:\"(\\d+)\"\\}";
            Matcher matcher = CommonTool.getMatcher(regEx, text);
            if (!matcher.find() || matcher.group(1).equals("2")) {
                LOG.info(String.format("Unexpected sync check result: %s", text));
            } else {
                return matcher.group(2);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }


    JSONObject getMsg() {
        JSONObject result = new JSONObject();
        String url = String.format(URLEnum.WEB_WX_SYNC_URL.getUrl(),
                storage.getLoginInfo().get(StorageLoginInfoEnum.url.getKey()),
                storage.getLoginInfo().get(StorageLoginInfoEnum.wxsid.getKey()),
                storage.getLoginInfo().get(StorageLoginInfoEnum.skey.getKey()),
                storage.getLoginInfo().get(StorageLoginInfoEnum.pass_ticket.getKey()));

        Map<String, Object> paramMap = new HashMap<String, Object>();
        Map<String, Map<String, String>> baseRequest = new HashMap<String, Map<String, String>>(1) {
            {
                Map<String, String> map = new HashMap<String, String>();
                for (BaseRequestEnum baseRequest : BaseRequestEnum.values()) {
                    map.put(baseRequest.getKey(), storage.getLoginInfo().get(baseRequest.getStorageKey()).toString());
                }
                put("baseRequest", map);
            }
        };

        paramMap.put("BaseRequest", baseRequest);
        paramMap.put(StorageLoginInfoEnum.synckey.getKey(), storage.getLoginInfo().get(StorageLoginInfoEnum.synckey.getKey()));
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

        String url = String.format(URLEnum.WEB_WX_GET_CONTACT.getUrl(), storage.getLoginInfo().get(StorageLoginInfoEnum.url.getKey()));
        List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair(StorageLoginInfoEnum.pass_ticket.getKey(), (String) storage.getLoginInfo().get(StorageLoginInfoEnum.pass_ticket.getKey())));
        params.add(new BasicNameValuePair(StorageLoginInfoEnum.skey.getKey(), (String) storage.getLoginInfo().get(StorageLoginInfoEnum.skey.getKey())));
        params.add(new BasicNameValuePair("r", String.valueOf(String.valueOf(new Date().getTime()))));
        HttpEntity entity = HttpClient.doGet(url, params, true, null);

        try {
            String result = EntityUtils.toString(entity, "UTF-8");
            JSONObject fullFriendsJsonList = JSON.parseObject(result);

            storage.setMemberCount(fullFriendsJsonList.getInteger(StorageLoginInfoEnum.MemberCount.getKey()));
            JSONArray member = fullFriendsJsonList.getJSONArray(StorageLoginInfoEnum.MemberList.getKey());
            for (Iterator iterator = member.iterator(); iterator.hasNext(); ) {
                JSONObject o = (JSONObject) iterator.next();

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
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return false;
    }

}
