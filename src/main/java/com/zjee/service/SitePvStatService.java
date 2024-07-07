package com.zjee.service;

import com.zjee.common.model.GeoIpModel;
import com.zjee.common.model.PvStatModel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class SitePvStatService {
    @Value("${nginx_log_path}")
    private String nginxLogPath;

    @SneakyThrows
    public PvStatModel getPvList(LocalDate endDate) {
        LocalDate startDate = endDate.minusDays(30L);

        PvStatModel model = PvStatModel.builder().dateList(new ArrayList<>()).pvList(new ArrayList<>()).build();
        while (!startDate.isAfter(endDate)) {
            String dt = startDate.format(DateTimeFormatter.ISO_DATE);
            model.getDateList().add(dt);

            Path path = Paths.get(nginxLogPath, String.format("ip_geo.log_%s", dt));
            long count = 0L;
            if (path.toFile().exists()) {
                count = Files.lines(path).count();
            }
            model.getPvList().add(count);
            startDate = startDate.plusDays(1L);
        }
        return model;
    }

    @SneakyThrows
    public List<GeoIpModel> getVisitorList(LocalDate date) {
        if (date == null) {
            return Collections.emptyList();
        }
        String fileName = String.format("ip_geo.log_%s", date.format(DateTimeFormatter.ISO_DATE));
        File file = Paths.get(nginxLogPath, fileName).toFile();
        if (!file.exists()) {
            log.warn("Log file does not exist: {}", file.getAbsolutePath());
            return Collections.emptyList();
        }

        Map<String, GeoIpModel> counter = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] data = line.split("###");
            String ip = data[0];
            String location = data[1];
            GeoIpModel ipModel = counter.getOrDefault(ip,
                    GeoIpModel.builder().ip(ip).location(location).visitCount(0L).build());
            ipModel.setVisitCount(ipModel.getVisitCount() + 1);
            counter.put(ip, ipModel);
        }
        return new ArrayList<>(counter.values());
    }
}

