package com.zjee.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

/**
 * @author ZhongJie
 * @date 22:08
 * @desc
 */
@Slf4j
public class HttpUtil {
    private static final HttpClient HTTP_CLIENT = HttpClients.createDefault();
    private static final RequestConfig REQUEST_CONFIG;

    static {
        REQUEST_CONFIG = RequestConfig.custom()
                .setConnectionRequestTimeout(5_000)
                .setConnectTimeout(30_000)
                .setContentCompressionEnabled(true)
                .setCircularRedirectsAllowed(false)
                .setMaxRedirects(3)
                .setCookieSpec(CookieSpecs.STANDARD)
                .build();
    }

    public static String get(String url, Map<String, String> param, Map<String, String> header) {
        URI uri = buildUri(url, param);
        HttpGet get = new HttpGet(uri);
        get.setConfig(REQUEST_CONFIG);
        addHeader(get, header);
        return sendRequest(get);
    }

    public static String post(String url, Map<String, String> param, Map<String, String> header, Object entity) {
        URI uri = buildUri(url, param);
        HttpPost post = new HttpPost(uri);
        post.setConfig(REQUEST_CONFIG);
        addHeader(post, header);
        addEntity(post, entity);
        return sendRequest(post);
    }

    private static String sendRequest(HttpUriRequest request) {
        try {
            HttpResponse response = HTTP_CLIENT.execute(request);
            HttpEntity entity = Objects.requireNonNull(response, "http response is null").getEntity();
            return EntityUtils.toString(entity, StandardCharsets.UTF_8);
        } catch (Throwable e) {
            log.error("http request error.", e);
            throw new RuntimeException(e);
        }
    }

    private static URI buildUri(String url, Map<String, String> param) {
        try {
            URIBuilder uriBuilder = new URIBuilder(url);
            if (null != param && !param.isEmpty()) {
                param.forEach(uriBuilder::addParameter);
            }
            return uriBuilder.build();
        } catch (Throwable e) {
            log.error("uri build error.", e);
            throw new RuntimeException(e);
        }
    }

    private static void addHeader(HttpUriRequest request, Map<String, String> header) {
        if (null == header || header.isEmpty()) {
            return;
        }
        header.forEach(request::addHeader);
    }

    private static void addEntity(HttpEntityEnclosingRequest request, Object bodyObj) {
        try {
            if (null != bodyObj) {
                String jsonBody = JsonUtil.toJson(bodyObj);
                StringEntity entity = new StringEntity(jsonBody);
                request.setEntity(entity);
            }
        } catch (Throwable e) {
            log.error("", e);
        }
    }
}
