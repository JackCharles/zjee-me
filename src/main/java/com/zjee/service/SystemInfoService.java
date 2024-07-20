package com.zjee.service;

import com.zjee.common.model.TrafficStatModel;
import com.zjee.common.util.CommonUtil;
import com.zjee.common.util.SystemUtil;
import com.zjee.constant.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@SuppressWarnings({"rawtypes"})
public class SystemInfoService {

    public Map<String, Map> getSystemInfo() {
        Map<String, Map> map = new HashMap<>();
        map.put("cpu", SystemUtil.getCpuInfo());
        map.put("mem", SystemUtil.getMemoryInfo());
        map.put("swap", SystemUtil.getSwapInfo());
        map.put("disk", SystemUtil.getFileSystemInfo());
        map.put("bandwidth", getBandwidthInfo());
        return map;
    }

    //带宽使用情况
    private Map<String, Object> getBandwidthInfo() {
        Map<String, Object> bandInfo = new HashMap<>();
        List<TrafficStatModel> trafficList = SystemUtil.getLatestTrafficInfo();
        if (trafficList.isEmpty()) {
            bandInfo.put("totalBand", 0);
            bandInfo.put("currUsage", 0);
            bandInfo.put("usedPercent", 100);
            return bandInfo;
        }

        trafficList.sort(Comparator.comparing(TrafficStatModel::getDate, String::compareTo));
        TrafficStatModel sample = trafficList.getFirst();
        bandInfo.put("totalBand", CommonUtil.formatByteUnit(Constant.TRAFFIC_CAPACITY));
        bandInfo.put("currUsage", CommonUtil.formatByteUnit(sample.getTotalOutBound()));
        bandInfo.put("usedPercent", CommonUtil.round(sample.getTotalOutBound() * 100.0d / Constant.TRAFFIC_CAPACITY, 2));
        List<String> dts = new ArrayList<>();
        List<Double> outBoundList = new ArrayList<>();
        List<Double> inBoundList = new ArrayList<>();
        for (TrafficStatModel t : trafficList) {
            dts.add(t.getDate());
            outBoundList.add(CommonUtil.round(t.getOutBound() * 1.0 / CommonUtil.MB, 0));
            inBoundList.add(CommonUtil.round(t.getInBound() * 1.0 / CommonUtil.MB, 0));
        }
        bandInfo.put("dt", dts);
        bandInfo.put("outBound", outBoundList);
        bandInfo.put("inBound", inBoundList);
        return bandInfo;
    }
}
