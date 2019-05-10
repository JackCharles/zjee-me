package com.zjee.demo.service;

import com.zjee.demo.service.util.SystemInfoTracker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SystemInfoService {

    @Autowired
    private SystemInfoTracker systemInfoTracker;

    public Map<String, Map> getSystemInfo(){
        Map<String, Map> map = new HashMap<>();
        map.put("machine", systemInfoTracker.getMachineInfo());
        map.put("OS", systemInfoTracker.getOperationSystemInfo());
        map.put("CPU", systemInfoTracker.getCpuInfo());
        map.put("Memory",systemInfoTracker.getPhysicalMemoryInfo());
        map.put("Disk",systemInfoTracker.getFileSystemInfo());
        map.put("Network",systemInfoTracker.getNetworkInfo());
        return map;
    }

    public Map<String, Map> getJvmInfo(){
        Map<String, Map> map = new HashMap<>();
        map.put("JVM Static",systemInfoTracker.getJvmStaticInfo());
        map.put("JVM Runtime",systemInfoTracker.getJvmRuntimeInfo());
        return map;
    }
}
