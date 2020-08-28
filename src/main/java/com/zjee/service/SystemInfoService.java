package com.zjee.service;

import cn.leancloud.AVObject;
import cn.leancloud.AVQuery;
import cn.leancloud.core.AVOSCloud;
import com.zjee.constant.Constant;
import com.zjee.constant.ResponseStatus;
import com.zjee.service.util.CommonUtil;
import com.zjee.service.util.SystemInfoTracker;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SystemInfoService {

    @Autowired
    private SystemInfoTracker systemInfoTracker;

    //hostdare鉴权cookie
    private static String authCookie;

    public Map<String, Map> getSystemInfo() {
        Map<String, Map> map = new HashMap<>();
        map.put("cpu", systemInfoTracker.getCpuInfo());
        map.put("mem", systemInfoTracker.getPhysicalMemoryInfo());
        map.put("swap", systemInfoTracker.getSwapInfo());
        map.put("disk", systemInfoTracker.getFileSystemInfo());
        map.put("bandwidth", getBandwidthInfo());
        return map;
    }


    //带宽使用情况
    private Map<String, Object> getBandwidthInfo() {
        Map<String, Object> bandInfo = new HashMap<>();
        String vpsInfo = getVpsInfo();
        //cookie失效，返回了登录页
        if (!vpsInfo.startsWith("{")) {
            log.warn("cookie is invalid, try login");
            login();
            vpsInfo = getVpsInfo();
        }

        JSONObject jsonObject = new JSONObject(vpsInfo);
        JSONObject bandwidth = jsonObject.getJSONObject("info").getJSONObject("bandwidth");
        //GB
        bandInfo.put("totalBand", bandwidth.optDouble("limit_gb", 0.0d));
        bandInfo.put("currUsage", bandwidth.optDouble("used_gb", 0.0d));
        bandInfo.put("usedPercent", CommonUtil.round(bandwidth.optDouble("percent", 0.0d), 2));

        //MB
        Map<String, Object> inBound = bandwidth.getJSONObject("in").toMap();
        Map<String, Object> outBound = bandwidth.getJSONObject("out").toMap();

        List<String> dateList = new ArrayList<>(inBound.keySet());
        dateList.sort(String.CASE_INSENSITIVE_ORDER);
        List<String> incoming = new ArrayList<>(32);
        List<String> outgoing = new ArrayList<>(32);
        for (String date : dateList) {
            incoming.add(String.format("%.2f", CommonUtil.toDouble(inBound.get(date))));
            outgoing.add(String.format("%.2f", CommonUtil.toDouble(outBound.get(date))));
        }

        bandInfo.put("date", dateList);
        bandInfo.put("incoming", incoming);
        bandInfo.put("outgoing", outgoing);
        return bandInfo;
    }

    private String getVpsInfo() {
        HttpClient httpClient = HttpClients.createDefault();
        try {
            URIBuilder builder = new URIBuilder(Constant.VPS_INFO_URL);
            HttpGet httpGet = new HttpGet(builder.build());
            //登录
            if (StringUtils.isEmpty(authCookie)) {
                login();
            }

            httpGet.addHeader("cookie", authCookie);
            HttpResponse response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == ResponseStatus.SUCCESS_CODE) {
                return CommonUtil.readStreamToString(response.getEntity().getContent());
            }
        } catch (Exception e) {
            log.error("failed to get vps info: ", e);
        }
        return "";
    }

    //登录
    private void login() {
        AVObject account = getLoginAccount();
        if (account == null) {
            return;
        }

        HttpClient httpClient = HttpClients.createDefault();
        HttpPost post = new HttpPost(Constant.LOGIN_URL);

        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("username", account.getString("email")));
        nvps.add(new BasicNameValuePair("password", account.getString("password")));
        post.setEntity(new UrlEncodedFormEntity(nvps, StandardCharsets.UTF_8));

        try {
            HttpResponse response = httpClient.execute(post);
            if (response.getStatusLine().getStatusCode() != ResponseStatus.ERROR_CODE) {
                Header[] headers = response.getHeaders("set-cookie");
                for (Header header : headers) {
                    for (HeaderElement element : header.getElements()) {
                        if (Constant.COOKIE_NAME.equals(element.getName())) {
                            authCookie = element.getName() + "=" + element.getValue();
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.error("login failed: ", e);
        }
    }

    private AVObject getLoginAccount() {
        AVQuery<AVObject> query = new AVQuery<>(Constant.HOSTDARE_INFO_CLASS);
        AVObject user = query.getFirst();
        if (user == null) {
            log.error("get hostdare account info failed");
            return null;
        }
        return user;
    }

    public static void main(String[] args) {
        AVOSCloud.initialize(Constant.LEAN_CLOUD_APP_ID, Constant.LEAN_CLOUD_APP_KEY);
        System.out.println(new SystemInfoService().getBandwidthInfo());
    }
}
