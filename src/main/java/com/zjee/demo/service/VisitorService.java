package com.zjee.demo.service;

import com.zjee.demo.constant.Constant;
import com.zjee.demo.service.util.IpInfoGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

@Component
public class VisitorService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private IpInfoGenerator ipInfoGenerator;

    public List<String> getSortedDateList() {
        File file = new File(Constant.VISITOR_LOG_PATH);
        List<String> dateList = new ArrayList<>();
        if (file != null && file.isDirectory()) {
            for (File logFile : file.listFiles()) {
                String fileName = logFile.getName();
                if(fileName != null && fileName.matches("^visitor-\\d{4}-\\d{2}-\\d{2}.log$"))
                    dateList.add(logFile.getName().substring(8, 18));
            }
            Collections.sort(dateList);
        }
        return dateList;
    }

    public Map<String, List<String>> getVisitorList(String date) {
        if (date == null)
            return Collections.emptyMap();
        String fileName = String.format("%s/visitor-%s.log", Constant.VISITOR_LOG_PATH, date);
        File file = new File(fileName);
        Map<String, List<String>> ipInfo = new HashMap<>();
        if (file != null && file.isFile()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(fileName));
                String line = null;
                Map<String, Integer> map = new HashMap<>();
                while ((line = reader.readLine()) != null) {
                    String[] data = line.split(" @ ")[1].split(" ");
                    if(map.containsKey(data[0]))
                        map.put(data[0], map.get(data[0])+1);//ip count
                    else
                        map.put(data[0], 1);
                }
                for (String ip : map.keySet()) {
                    ipInfo.put(ip, Arrays.asList(map.get(ip).toString(), ipInfoGenerator.getIpLocationInfo(ip)));
                }
            } catch (Exception e) {
                ipInfo = Collections.emptyMap();
            }
        }
        return ipInfo;
    }

    public void clearIpInfoCache(){
        ipInfoGenerator.clearCache();
    }
}

