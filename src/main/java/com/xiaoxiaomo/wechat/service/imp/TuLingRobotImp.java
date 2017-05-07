package com.xiaoxiaomo.wechat.service.imp;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xiaoxiaomo.wechat.service.MsgHandleService;
import com.xiaoxiaomo.wechat.utils.commmon.download.DownloadTools;
import com.xiaoxiaomo.wechat.utils.commmon.http.HttpClient;
import com.xiaoxiaomo.wechat.utils.enums.MsgInfoEnum;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * 图灵机器人
 *
 * Created by xiaoxiaomo on 2017/5/7.
 */
public class TuLingRobotImp implements MsgHandleService {
    String apiKey = "597b34bea4ec4c85a775c469c84b6817";
    Logger logger = Logger.getLogger("TuLingRobotImp");

    @Override
    public String textMsgHandle(JSONObject msg) {
        String result = "";
        String text = msg.getString("Text");
        String url = "http://www.tuling123.com/openapi/api";
        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("key", apiKey);
        paramMap.put("info", text);
        paramMap.put("userid", "123456");
        String paramStr = JSON.toJSONString(paramMap);
        try {
            HttpEntity entity = HttpClient.doPost(url, paramStr);
            result = EntityUtils.toString(entity, "UTF-8");
            JSONObject obj = JSON.parseObject(result);
            if (obj.getString("code").equals("100000")) {
                result = obj.getString("text");
            } else {
                result = "处理有误";
            }
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
        return result;
    }

    @Override
    public String picMsgHandle(JSONObject msg) {
        return "收到图片";
    }

    @Override
    public String voiceMsgHandle(JSONObject msg) {
        String fileName = String.valueOf(new Date().getTime());
        String voicePath = "D://itchat4j/voice" + File.separator + fileName + ".mp3";
        DownloadTools.getDownloadFn(msg, MsgInfoEnum.VOICE.getCode(), voicePath);
        return "收到语音";
    }

    @Override
    public String videoMsgHandle(JSONObject msg) {
        String fileName = String.valueOf(new Date().getTime());
        String viedoPath = "D://itchat4j/viedo" + File.separator + fileName + ".mp4";
        DownloadTools.getDownloadFn(msg, MsgInfoEnum.VIDEO.getCode(), viedoPath);
        return "收到视频";
    }

    @Override
    public String nameCardMsgHandle(JSONObject msg) {
        // TODO Auto-generated method stub
        return null;
    }
}
