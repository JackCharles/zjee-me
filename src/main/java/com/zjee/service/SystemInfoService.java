package com.zjee.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zjee.constant.Constant;
import com.zjee.dal.BandwidthMapper;
import com.zjee.pojo.BwStat;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SystemInfoService {

    @Autowired
    private SystemInfoTracker systemInfoTracker;

    @Autowired
    private BandwidthMapper bandwidthMapper;

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
        LocalDateTime today = LocalDate.now().atStartOfDay();
        List<BwStat> bwStat = bandwidthMapper.getBwStat(today.minusDays(30), today.minusDays(1));
        if (bwStat.isEmpty()) {
            bandInfo.put("totalBand", 0);
            bandInfo.put("currUsage", 0);
            bandInfo.put("usedPercent", 100);
            return bandInfo;
        }

        bwStat.sort(Comparator.comparing(BwStat::getDt, LocalDateTime::compareTo));
        BwStat sample = bwStat.get(0);
        bandInfo.put("totalBand", CommonUtil.formatByteUnit(sample.getCapacity()));
        bandInfo.put("currUsage", CommonUtil.formatByteUnit(sample.getUsageTotal()));
        bandInfo.put("usedPercent", CommonUtil.round(sample.getUsageTotal() * 100.0d / sample.getCapacity(), 2));
        List<String> dts = new ArrayList<>();
        List<Double> usage = new ArrayList<>();
        for (BwStat bw : bwStat) {
            dts.add(bw.getDt().toLocalDate().toString());
            usage.add(CommonUtil.round(bw.getUsageToday() * 1.0 / CommonUtil.MB, 0));
        }
        bandInfo.put("dt", dts);
        bandInfo.put("dataUsage", usage);
        return bandInfo;
    }

    @Scheduled(cron = "0 55 23 * * *")
    public void statisticBandwidthUsage(){
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
            Long totalData = jsonObject.getLong("plan_monthly_data");
            Long dataUsage = jsonObject.getLong("data_counter");
            LocalDateTime today = LocalDate.now().minusDays(1).atStartOfDay();
            LocalDateTime yesterday = today.minusDays(1);
            List<BwStat> bwStat = bandwidthMapper.getBwStat(yesterday, yesterday);
            long lastUsageSum = 0;
            if(!CollectionUtils.isEmpty(bwStat)) {
                lastUsageSum = bwStat.get(0).getUsageTotal();
            }
            BwStat toDayUsage = new BwStat();
            toDayUsage.setDt(today);
            toDayUsage.setCapacity(totalData);
            toDayUsage.setUsageToday(dataUsage - lastUsageSum);
            toDayUsage.setUsageTotal(dataUsage);
            bandwidthMapper.insertOne(toDayUsage);
        }catch (Exception e) {
            log.error("get bandwidth usage error: ", e);
        }
    }
}
