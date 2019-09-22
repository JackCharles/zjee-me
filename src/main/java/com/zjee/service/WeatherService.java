package com.zjee.service;

import com.zjee.constant.ResponseStatus;
import com.zjee.service.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.*;

import static com.zjee.constant.Constant.*;

/**
 * Date: 2019-05-29 10:30
 * Author: zhongjie03
 * E-mail: zhongjie03@meituan.com
 * Description:
 */

@Slf4j
@Service
public class WeatherService {

    @Cacheable(cacheNames = "woeid", key = "#keyWord", unless = "#result == null")
    public Object getWoeidByKeyWord(String keyWord) {
        log.info("getWoeidByKeyWord未命中缓存，keyword = {}", keyWord);
        if (keyWord == null) {
            return null;
        }
        try {
            keyWord = URLEncoder.encode(keyWord, "utf-8");
        } catch (UnsupportedEncodingException e) {
            log.error("UnsupportedEncodingException", e);
            return null;
        }
        try {
            HttpClient httpClient = HttpClients.createDefault();
            URI uri = new URIBuilder(String.format(WOEID_URL, keyWord))
                    .addParameter("lang", "zh-CN")
                    .addParameter("tz", "Asia/Shanghai")
                    .addParameter("ver", "0.0.7518")
                    .build();
            HttpResponse res = httpClient.execute(new HttpGet(uri));
            if (res.getStatusLine().getStatusCode() == ResponseStatus.SUCCESS_CODE) {
                InputStream content = res.getEntity().getContent();
                String jsonStr = new BufferedReader(new InputStreamReader(content)).readLine();
                List<Object> locations = new JSONArray(jsonStr).toList();
                if (locations.size() >= 1) {
                    return locations.get(0);
                }
            }
        } catch (Exception e) {
            log.error("ClientProtocolException", e);
        }
        return null;
    }


    public Map<String, ?> getForecastBywoeId(int woeid) throws Exception {
        long timestamp = System.currentTimeMillis() / 1000;
        byte[] nonce = new byte[32];
        Random rand = new Random();
        rand.nextBytes(nonce);
        String oauthNonce = new String(nonce).replaceAll("\\W", "");

        List<String> parameters = new ArrayList<>();
        parameters.add("oauth_consumer_key=" + CONSUMER_KEY);
        parameters.add("oauth_nonce=" + oauthNonce);
        parameters.add("oauth_signature_method=HMAC-SHA1");
        parameters.add("oauth_timestamp=" + timestamp);
        parameters.add("oauth_version=1.0");
        parameters.add("woeid=" + woeid);
        parameters.add("u=c");
        Collections.sort(parameters);

        StringBuilder parametersList = new StringBuilder();
        for (int i = 0; i < parameters.size(); i++) {
            parametersList.append(((i > 0) ? "&" : "") + parameters.get(i));
        }

        String signatureString = "GET&" +
                URLEncoder.encode(WEATHER_URL, "UTF-8") + "&" +
                URLEncoder.encode(parametersList.toString(), "UTF-8");

        String signature = null;
        try {
            SecretKeySpec signingKey = new SecretKeySpec((CONSUMER_SECRET + "&").getBytes(), "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
            byte[] rawHMAC = mac.doFinal(signatureString.getBytes());
            Base64.Encoder encoder = Base64.getEncoder();
            signature = encoder.encodeToString(rawHMAC);
        } catch (Exception e) {
            log.error("Unable to append signature", e);
            return null;
        }

        String authorizationLine = new StringBuilder("OAuth oauth_consumer_key=\"")
                .append(CONSUMER_KEY)
                .append("\",oauth_nonce=\"")
                .append(oauthNonce)
                .append("\", oauth_timestamp=\"")
                .append(timestamp)
                .append("\", oauth_signature_method=\"HMAC-SHA1\", oauth_signature=\"")
                .append(signature)
                .append("\", oauth_version=\"1.0\"")
                .toString();

        HttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(WEATHER_URL + "?woeid=" + woeid + "&u=c");
        httpGet.addHeader("Authorization", authorizationLine);
        httpGet.addHeader("X-Yahoo-App-Id", APP_ID);
        httpGet.addHeader("Content-Type", "application/xml");

        HttpResponse response = httpClient.execute(httpGet);
        if (response.getStatusLine().getStatusCode() == ResponseStatus.SUCCESS_CODE) {
            JSONObject jsonObject = XML.toJSONObject(CommonUtil.readStreamToString(response.getEntity().getContent()));
            return jsonObject.toMap();
        }
        return null;
    }
}
