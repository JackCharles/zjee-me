package com.zjee.service;

import com.zjee.constant.Constant;
import com.zjee.constant.ResponseStatus;
import com.zjee.service.util.CommonUtil;
import com.zjee.service.util.SystemInfoTracker;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SystemInfoService {

    @Autowired
    private SystemInfoTracker systemInfoTracker;

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
        Map<String, Double> currentUsage = getBandCurrentUsage();
        bandInfo.put("currUsage", currentUsage.get("bandwidth_used"));
        bandInfo.put("totalBand", currentUsage.get("bandwidth_total"));
        bandInfo.put("usedPercent", CommonUtil.round((currentUsage.get("bandwidth_used")*100.0) / currentUsage.get("bandwidth_total"), 2));

        HttpClient httpClient = HttpClients.createDefault();
        try {
            URIBuilder builder = new URIBuilder(Constant.BAND_WIDTH);
            builder.addParameter("SUBID", Constant.SUB_ID);
            HttpGet httpGet = new HttpGet(builder.build());
            httpGet.addHeader("API-Key", Constant.VULTR_KEY);
            HttpResponse response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == ResponseStatus.SUCCESS_CODE) {
                String json = CommonUtil.readStreamToString(response.getEntity().getContent());
                JSONObject jsonObject = new JSONObject(json);
                JSONArray incomingBytes = jsonObject.optJSONArray("incoming_bytes");
                JSONArray outgoingBytes = jsonObject.optJSONArray("outgoing_bytes");

                List<String> dateList = new ArrayList<>(32);
                List<String> incoming = new ArrayList<>(32);
                List<String> outgoing = new ArrayList<>(32);
                for (int i = 0; i < incomingBytes.length(); ++i) {
                    JSONArray in = incomingBytes.optJSONArray(i);
                    JSONArray out = outgoingBytes.optJSONArray(i);
                    dateList.add(in.optString(0, ""));
                    incoming.add(CommonUtil.formatByteToMB(Long.parseLong(in.optString(1, "0"))));
                    outgoing.add(CommonUtil.formatByteToMB(Long.parseLong(out.optString(1, "0"))));
                }
                bandInfo.put("date", dateList);
                bandInfo.put("incoming", incoming);
                bandInfo.put("outgoing", outgoing);
            }
        } catch (Exception e) {
            log.error("An error occurred while getting bandwidth detail: {}", e.getMessage(), e);
            return bandInfo;
        }
        return bandInfo;
    }

    //当前带宽使用总量
    private Map<String, Double> getBandCurrentUsage() {
        Map<String, Double> map = new HashMap<>();
        try {
            HttpGet httpGet = new HttpGet(Constant.SERVER_LIST);
            httpGet.addHeader("API-Key", Constant.VULTR_KEY);
            CloseableHttpResponse response = HttpClients.createDefault().execute(httpGet);
            if (response.getStatusLine().getStatusCode() == ResponseStatus.SUCCESS_CODE) {
                String json = CommonUtil.readStreamToString(response.getEntity().getContent());
                JSONObject jsonObject = new JSONObject(json).optJSONObject(Constant.SUB_ID);
                map.put("bandwidth_used", jsonObject.optDouble("current_bandwidth_gb", 0.0));
                map.put("bandwidth_total", Double.valueOf(jsonObject.optString("allowed_bandwidth_gb", "1")));
            }
        } catch (Exception e) {
            log.error("An error occurred while getting server info: {}", e.getMessage(), e);
            return map;
        }
        return map;
    }
}
