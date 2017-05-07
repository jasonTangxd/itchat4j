package com.xiaoxiaomo.wechat.utils.commmon.download;

import com.alibaba.fastjson.JSONObject;
import com.xiaoxiaomo.wechat.core.Storage;
import com.xiaoxiaomo.wechat.utils.commmon.http.HttpClient;
import com.xiaoxiaomo.wechat.utils.enums.MsgInfoEnum;
import com.xiaoxiaomo.wechat.utils.enums.MsgTypeEnum;
import org.apache.http.HttpEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 下载处理类
 *
 * @author https://github.com/yaphone
 * @version 1.0
 * @date 创建时间：2017年4月21日 下午11:18:46
 */
public class DownloadTools {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(DownloadTools.class);
    private static Storage core = Storage.getInstance();

    /**
     * 处理下载任务
     *
     * @param msg
     * @param type
     * @param path
     * @return
     */
    public static Object getDownloadFn(JSONObject msg, String type, String path) {
        Map<String, String> headerMap = new HashMap<String, String>();
        List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
        String url = "";
        if (type.equals(MsgInfoEnum.PIC.getCode())) {
            url = String.format("%s/webwxgetmsgimg", (String) core.getLoginInfo().get("url"));
        } else if (type.equals(MsgInfoEnum.VOICE.getCode())) {
            url = String.format("%s/webwxgetvoice", (String) core.getLoginInfo().get("url"));
        } else if (type.equals(MsgInfoEnum.VIDEO.getCode())) {
            headerMap.put("Range", "bytes=0-");
            url = String.format("%s/webwxgetvideo", (String) core.getLoginInfo().get("url"));
        }
        params.add(new BasicNameValuePair("msgid", msg.getString("NewMsgId")));
        params.add(new BasicNameValuePair("skey", (String) core.getLoginInfo().get("skey")));
        HttpEntity entity = HttpClient.doGet(url, params, true, headerMap);
        try {
            OutputStream out = new FileOutputStream(path);
            byte[] bytes = EntityUtils.toByteArray(entity);
            out.write(bytes);
            out.flush();
            out.close();
            // Tools.printQr(path);

        } catch (Exception e) {
            logger.info(e.getMessage());
            return false;
        }
        return null;
    }

    ;

}
