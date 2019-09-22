package com.zjee.service;

import com.zjee.constant.Constant;
import com.zjee.service.util.IpInfoGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class VisitorService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private IpInfoGenerator ipInfoGenerator;

    /**
     * 获取近31天PV
     *
     * @return
     */
    public Map<String, Object> getPvList() {
        Map<String, Object> resMap = new HashMap<>();
        File file = new File(Constant.VISITOR_LOG_PATH);
        Map<String, Long> pvMap = new HashMap<>();
        if (file != null && file.isDirectory()) {
            String minDate = LocalDate.now().minusDays(31).format(DateTimeFormatter.ISO_DATE);
            File[] fileList = file.listFiles((f, name) -> name.matches("^visitor-\\d{4}-\\d{2}-\\d{2}.log$") &&
                    name.substring(8, 18).compareTo(minDate) >= 0);
            for (File logFile : fileList) {
                String fileName = logFile.getName();
                try {
                    pvMap.put(fileName.substring(8, 18), Files.lines(logFile.toPath()).count());
                } catch (Exception e) {
                    logger.error("get pv error: ", e);
                }
            }
            List<String> dateList = new ArrayList<>(pvMap.keySet());
            dateList.sort(String::compareTo);
            List<Long> pvList = new ArrayList<>(32);
            dateList.forEach(d -> pvList.add(pvMap.get(d)));
            resMap.put("dateList", dateList);
            resMap.put("pvList", pvList);
        }
        return resMap;
    }

    @Cacheable(cacheNames = "visitors-info", key = "#date", unless = "#result == null || #result.empty")
    public List<Map<String, Object>> getVisitorList(String date) {
        if (date == null) {
            return Collections.emptyList();
        }
        String fileName = String.format("%s/visitor-%s.log", Constant.VISITOR_LOG_PATH, date);
        File file = new File(fileName);
        List<Map<String, Object>> ipInfo = new ArrayList<>();
        if (file != null && file.isFile()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(fileName));
                String line = null;
                Map<String, Integer> counter = new HashMap<>();
                while ((line = reader.readLine()) != null) {
                    String[] data = line.split(" @ ")[1].split(" ");
                    if (counter.containsKey(data[0])) {
                        counter.put(data[0], counter.get(data[0]) + 1);//ip count
                    } else {
                        counter.put(data[0], 1);
                    }
                }
                for (String ip : counter.keySet()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("key", ip);
                    map.put("ip", ip);
                    map.put("visitCount", counter.get(ip));
                    String ipLocation = ipInfoGenerator.getIpLocationInfo(ip);
                    map.put("location", StringUtils.isEmpty(ipLocation) ? "unknown" : ipLocation);
                    ipInfo.add(map);
                }
            } catch (Exception e) {
                logger.error("get visit log error: ", e);
            }
        }
        return ipInfo;
    }

    public void clearIpInfoCache() {
        ipInfoGenerator.clearCache();
    }
}

