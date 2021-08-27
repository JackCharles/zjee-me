package com.zjee.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zjee.constant.Constant;
import com.zjee.service.util.CommonUtil;
import com.zjee.service.util.SystemInfoTracker;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
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
        Map<String, Long> bandwidthData = getBandwidthData();
        if(bandwidthData == null) {
            return bandInfo;
        }
        //GB
        Long totalBand = bandwidthData.get("totalBand");
        Long currUsage = bandwidthData.get("currUsage");
        bandInfo.put("totalBand", CommonUtil.formatByteUnit(totalBand));
        bandInfo.put("currUsage", CommonUtil.formatByteUnit(currUsage));
        bandInfo.put("usedPercent", currUsage * 1.0d / totalBand);
        return bandInfo;
    }

    private Map<String, Long> getBandwidthData(){
        HttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet(Constant.BWG_API_URL);
        try {
            HttpResponse response = client.execute(get);
            int statusCode = response.getStatusLine().getStatusCode();
            if(statusCode != HttpStatus.SC_OK) {
                throw new RuntimeException("http request error: " + statusCode);
            }
            String json = EntityUtils.toString(response.getEntity());
            JSONObject jsonObject = JSON.parseObject(json);
            Map<String, Long> bandWidthUsage = new HashMap<>();
            bandWidthUsage.put("totalBand", jsonObject.getLong("plan_monthly_data"));
            bandWidthUsage.put("currUsage", jsonObject.getLong("data_counter"));
            return bandWidthUsage;
        }catch (Exception e) {
            log.error("get bandwidth usage error: ", e);
        }
        return null;
    }
}
