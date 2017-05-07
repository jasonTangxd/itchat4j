package com.xiaoxiaomo.wechat.utils.commmon.http;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * HTTP访问类，对Apache HttpClient进行简单封装，适配器模式
 *
 * @author https://github.com/yaphone
 * @version 1.0
 * @date 创建时间：2017年4月9日 下午7:05:04
 */
public class HttpClient {
    private static Logger LOG = LoggerFactory.getLogger(HttpClient.class);

    private static CloseableHttpClient httpClient = HttpClients.createDefault();
    public static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36";

    private HttpClient() {
    }

    /**
     *
     * 处理GET请求
     *
     * @param url
     * @param params
     * @param redirect
     * @param headerMap
     * @return
     */
    public static HttpEntity doGet(
            final String url, final List<BasicNameValuePair> params, final boolean redirect, final Map<String, String> headerMap) {

        HttpGet httpGet;
        try {
            if (params != null) {
                String paramStr = EntityUtils.toString(new UrlEncodedFormEntity(params, Consts.UTF_8));
                httpGet = new HttpGet(url + "?" + paramStr);
            } else {
                httpGet = new HttpGet(url);
            }
            if (!redirect) {
                httpGet.setConfig(RequestConfig.custom().setRedirectsEnabled(false).build()); // 禁止重定向
            }

            httpGet.setHeader("Content-type", "application/json; charset=utf-8");
            httpGet.setHeader("User-Agent", USER_AGENT);
            if (headerMap != null) {
                Set<Entry<String, String>> entries = headerMap.entrySet();
                for (Entry<String, String> entry : entries) {
                    httpGet.setHeader(entry.getKey(), entry.getValue());
                }
            }
            CloseableHttpResponse response = httpClient.execute(httpGet);
            return response.getEntity();
        } catch (Exception e) {
            LOG.error("GET请求失败" , e);
        }

        return null;
    }


    /**
     *
     * 处理POST请求
     *
     * @param url
     * @param paramsStr
     * @return
     */
    public static HttpEntity doPost(String url, String paramsStr) {
        HttpPost httpPost = new HttpPost(url);
        try {
            StringEntity params = new StringEntity(paramsStr, Consts.UTF_8);
            httpPost.setEntity(params);
            httpPost.setHeader("Content-type", "application/json; charset=utf-8");
            httpPost.setHeader("User-Agent", USER_AGENT);
            CloseableHttpResponse response = httpClient.execute(httpPost);
            return response.getEntity();
        } catch (Exception e) {
            LOG.error("POST请求失败" , e);
        }
        return null;
    }
}