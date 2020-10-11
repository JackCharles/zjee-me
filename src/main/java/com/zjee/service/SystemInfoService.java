package com.zjee.service;

import com.zjee.service.util.SystemInfoTracker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class SystemInfoService {

    @Autowired
    private SystemInfoTracker systemInfoTracker;

    //hostdare鉴权cookie
    private static String authCookie = "WHMCSQnFy6YcDKurd=295b91fc84c4753dc9d1f1dedcace5b1";

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
        //GB
        bandInfo.put("totalBand", 0.0d);
        bandInfo.put("currUsage", 0.0d);
        bandInfo.put("usedPercent", 0.0d);

        bandInfo.put("date", Collections.emptyList());
        bandInfo.put("incoming", Collections.emptyList());
        bandInfo.put("outgoing", Collections.emptyList());
        return bandInfo;
    }
}
