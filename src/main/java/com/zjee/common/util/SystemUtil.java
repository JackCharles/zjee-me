package com.zjee.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.zjee.common.model.TrafficStatModel;
import lombok.extern.slf4j.Slf4j;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.VirtualMemory;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SystemUtil {
    private static final HardwareAbstractionLayer HARDWARE;
    private static final OperatingSystem OS;

    static {
        SystemInfo systemInfo = new SystemInfo();
        HARDWARE = systemInfo.getHardware();
        OS = systemInfo.getOperatingSystem();
    }

    /**
     * Mem info
     *
     * @return physical memory info
     */
    public static Map<String, Object> getMemoryInfo() {
        Map<String, Object> memInfo = new HashMap<>();
        try {
            GlobalMemory mem = HARDWARE.getMemory();
            long totalMem = mem.getTotal();
            long availableMem = mem.getAvailable();
            long usedMem = totalMem - availableMem;
            memInfo.put("subject", "RAM");
            memInfo.put("total", CommonUtil.formatByteUnit(totalMem));
            memInfo.put("used", CommonUtil.formatByteUnit(usedMem));
            memInfo.put("usedPercent", CommonUtil.doubleToPercent(usedMem * 1.0 / totalMem));
            memInfo.put("usedPercentD", CommonUtil.round(usedMem * 100.0 / totalMem, 2));
            memInfo.put("free", CommonUtil.formatByteUnit(availableMem));
            memInfo.put("freePercent", CommonUtil.doubleToPercent(availableMem * 1.0 / totalMem));
            return memInfo;
        } catch (Throwable e) {
            log.error("get physical memory info error.", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Swap Info
     *
     * @return swap info
     */
    public static Map<String, String> getSwapInfo() {
        Map<String, String> swapInfo = new HashMap<>();
        try {
            VirtualMemory vMem = HARDWARE.getMemory().getVirtualMemory();
            long swapTotal = vMem.getSwapTotal();
            long swapUsed = vMem.getSwapUsed();
            long availableSwap = swapTotal - swapUsed;
            swapInfo.put("subject", "SWAP");
            swapInfo.put("total", CommonUtil.formatByteUnit(swapTotal));
            swapInfo.put("used", CommonUtil.formatByteUnit(swapUsed));
            swapInfo.put("usedPercent", CommonUtil.doubleToPercent(swapUsed * 1.0 / swapTotal));
            swapInfo.put("free", CommonUtil.formatByteUnit(availableSwap));
            swapInfo.put("freePercent", CommonUtil.doubleToPercent(availableSwap * 1.0 / swapTotal));
            return swapInfo;
        } catch (Throwable e) {
            log.error("get system swap info.", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Cpu Info
     *
     * @return cpu info
     */
    public static Map<String, Object> getCpuInfo() {
        Map<String, Object> cpuInfo = new HashMap<>();
        try {
            CentralProcessor processor = HARDWARE.getProcessor();
            long[][] firstTicks = processor.getProcessorCpuLoadTicks();
            long[] firstSysCpuLoad = processor.getSystemCpuLoadTicks();
            Thread.sleep(1000);
            long[][] secondTicks = processor.getProcessorCpuLoadTicks();
            double systemCpuLoad = processor.getSystemCpuLoadBetweenTicks(firstSysCpuLoad);
            int logicProcessorCount = processor.getLogicalProcessorCount();
            List<Map<String, Object>> cpuUsageList = new ArrayList<>(logicProcessorCount);
            List<CentralProcessor.LogicalProcessor> lcpus = processor.getLogicalProcessors();
            for (int i = 0; i < logicProcessorCount; i++) {
                Map<String, Object> detailMap = cpuLoadTicksToMap(firstTicks[i], secondTicks[i]);
                detailMap.put("name", "CPU " + lcpus.get(i).getProcessorNumber());
                cpuUsageList.add(detailMap);
            }
            cpuInfo.put("detail", cpuUsageList);
            cpuInfo.put("usage", CommonUtil.round(systemCpuLoad * 100, 2));
            return cpuInfo;
        } catch (Throwable e) {
            log.error("get system cpu info error.", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * get disk info
     *
     * @return file system info
     */
    public static Map<String, Object> getFileSystemInfo() {
        Map<String, Object> fsMap = new HashMap<>();
        try {
            FileSystem fileSystem = OS.getFileSystem();
            List<OSFileStore> fileStores = fileSystem.getFileStores();
            List<Map<String, Object>> fsUsage = new ArrayList<>();
            long fsTotalSpace = 0;
            long fsFreeSpace = 0;
            for (OSFileStore fs : fileStores) {
                Map<String, Object> map = new HashMap<>();
                long totalSpace = fs.getTotalSpace();
                long freeSpace = fs.getFreeSpace();
                long usedSpace = totalSpace - freeSpace;
                fsTotalSpace += totalSpace;
                fsFreeSpace += freeSpace;

                map.put("name", fs.getName());
                map.put("type", fs.getType());
                map.put("total", CommonUtil.formatByteUnit(totalSpace));
                map.put("used", CommonUtil.formatByteUnit(usedSpace));
                map.put("usedPercent", CommonUtil.doubleToPercent(usedSpace * 1.0 / totalSpace));
                map.put("free", CommonUtil.formatByteUnit(freeSpace));
                map.put("available", CommonUtil.formatByteUnit(fs.getUsableSpace()));
                fsUsage.add(map);
                if ("/".equals(fs.getName())) {
                    fsMap.put("usage", CommonUtil.round(usedSpace * 100.0 / totalSpace, 2));
                }
            }
            fsMap.put("usage", CommonUtil.round((fsTotalSpace - fsFreeSpace) * 100.0 / fsTotalSpace, 2));
            fsMap.put("detail", fsUsage);
            return fsMap;
        } catch (Exception e) {
            log.error("get file system error.", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * get latest 31 days traffic info
     *
     * @return traffic list
     */
    public static List<TrafficStatModel> getLatestTrafficInfo() {
        String[] command = CommonUtil.buildShellCmd("vnstat --json d 31");

        try {
            log.info("Read traffic start: {}", Arrays.toString(command));
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor(5, TimeUnit.SECONDS);
            String json = CommonUtil.readStreamToString(process.getInputStream());
            log.info("Read traffic finished: {}", json);

            JsonNode root = JsonUtil.parseJsonTree(json);
            JsonNode traffic = root.withArrayProperty("interfaces")
                    .get(0).get("traffic");
            JsonNode total = traffic.get("total");
            long totalInBound = total.get("rx").asLong();
            long totalOutBound = total.get("tx").asLong();

            List<TrafficStatModel> res = new ArrayList<>();
            traffic.withArrayProperty("day").forEach(t -> {
                TrafficStatModel model = new TrafficStatModel();
                // Linux timestamp是秒，要*1000转毫秒
                model.setDate(timestampToDateStr(t.get("timestamp").asLong() * 1000L));
                model.setInBound(t.get("rx").asLong());
                model.setOutBound(t.get("tx").asLong());
                model.setTotalInBound(totalInBound);
                model.setTotalOutBound(totalOutBound);
                res.add(model);
            });
            log.info("Parse traffic json success");
            return res;
        } catch (Exception e) {
            log.error("Query traffic info Error: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private static Map<String, Object> cpuLoadTicksToMap(long[] firstTicks, long[] secondTicks) {
        Map<String, Object> map = new HashMap<>();
        long total = Arrays.stream(secondTicks).sum() - Arrays.stream(firstTicks).sum();
        for (CentralProcessor.TickType tickType : CentralProcessor.TickType.values()) {
            map.put(tickType.name(), CommonUtil.doubleToPercent(
                    (secondTicks[tickType.getIndex()] - firstTicks[tickType.getIndex()]) * 1.0 / total));
        }
        return map;
    }

    /**
     * convert timestamp to date string
     * @param timestamp 毫秒
     * @return date string
     */
    private static String timestampToDateStr(long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        return LocalDate.ofInstant(instant, ZoneId.systemDefault()).format(DateTimeFormatter.ISO_DATE);
    }
}
