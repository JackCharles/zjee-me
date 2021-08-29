package com.zjee.service.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.VirtualMemory;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

import java.util.*;

@Slf4j
@Component
public class SystemInfoTracker {

    private static HardwareAbstractionLayer hardware;

    private static OperatingSystem os;

    static {
        SystemInfo systemInfo = new SystemInfo();
        hardware = systemInfo.getHardware();
        os = systemInfo.getOperatingSystem();
    }

    /**
     * Mem info
     * @return
     */
    public Map<String, Object> getPhysicalMemoryInfo() {
        Map<String, Object> memInfo = new HashMap<>();
        try {
            GlobalMemory mem = hardware.getMemory();
            long totalMem = mem.getTotal();
            long availableMem = mem.getAvailable();
            long usedMem =totalMem - availableMem;
            memInfo.put("subject", "RAM");
            memInfo.put("total", CommonUtil.formatByteUnit(totalMem));
            memInfo.put("used", CommonUtil.formatByteUnit(usedMem));
            memInfo.put("usedPercent", CommonUtil.doubleToPercent(usedMem * 1.0 / totalMem));
            memInfo.put("usedPercentD", CommonUtil.round(usedMem * 100.0 / totalMem, 2));
            memInfo.put("free", CommonUtil.formatByteUnit(availableMem));
            memInfo.put("freePercent", CommonUtil.doubleToPercent(availableMem * 1.0 / totalMem));
        } catch (Throwable e) {
            log.error("ERROR:", e);
            memInfo = Collections.emptyMap();
        }
        return memInfo;
    }

    /**
     * Swap Info
     * @return
     */
    public Map<String, String> getSwapInfo() {
        Map<String, String> swapInfo = new HashMap<>();
        try {
            VirtualMemory vMem = hardware.getMemory().getVirtualMemory();
            long swapTotal = vMem.getSwapTotal();
            long swapUsed = vMem.getSwapUsed();
            long availableSwap = swapTotal - swapUsed;
            swapInfo.put("subject", "SWAP");
            swapInfo.put("total", CommonUtil.formatByteUnit(swapTotal));
            swapInfo.put("used", CommonUtil.formatByteUnit(swapUsed));
            swapInfo.put("usedPercent", CommonUtil.doubleToPercent(swapUsed * 1.0 / swapTotal));
            swapInfo.put("free", CommonUtil.formatByteUnit(availableSwap));
            swapInfo.put("freePercent", CommonUtil.doubleToPercent(availableSwap * 1.0 /swapTotal));
        } catch (Throwable e) {
            log.error("ERROR: ", e);
            swapInfo = Collections.emptyMap();
        }
        return swapInfo;
    }

    /**
     * Cpu Info
     * @return
     */
    public Map<String, Object> getCpuInfo() {
        Map<String, Object> cpuInfo = new HashMap<>();
        try {
            CentralProcessor processor = hardware.getProcessor();
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
        } catch (Throwable e) {
            log.error("ERROR: ", e);
            cpuInfo = Collections.emptyMap();
        }
        return cpuInfo;
    }

    private Map<String, Object> cpuLoadTicksToMap(long[] firstTicks, long[] secondTicks) {
        Map<String, Object> map = new HashMap<>();
        long total = Arrays.stream(secondTicks).sum() - Arrays.stream(firstTicks).sum();
        for (CentralProcessor.TickType tickType : CentralProcessor.TickType.values()) {
            map.put(tickType.name(), CommonUtil.doubleToPercent(
                    (secondTicks[tickType.getIndex()] - firstTicks[tickType.getIndex()]) * 1.0 / total));
        }
        return map;
    }

    /**
     * get disk info
     * @return
     */
    public Map<String, Object> getFileSystemInfo() {
        Map<String, Object> fsMap = new HashMap<>();
        try {
            FileSystem fileSystem = os.getFileSystem();
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
                map.put("usedPercent", CommonUtil.doubleToPercent(usedSpace* 1.0 / totalSpace));
                map.put("free", CommonUtil.formatByteUnit(freeSpace));
                map.put("available", CommonUtil.formatByteUnit(fs.getUsableSpace()));
                fsUsage.add(map);
                if ("/".equals(fs.getName())) {
                    fsMap.put("usage", CommonUtil.round(usedSpace * 100.0 / totalSpace, 2));
                }
            }
            fsMap.put("usage", CommonUtil.round((fsTotalSpace - fsFreeSpace) * 100.0 / fsTotalSpace, 2));
            fsMap.put("detail", fsUsage);
        } catch (Exception e) {
            fsMap = Collections.emptyMap();
        }
        return fsMap;
    }
}
