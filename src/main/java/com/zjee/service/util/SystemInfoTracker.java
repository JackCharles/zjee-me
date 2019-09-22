package com.zjee.service.util;

import org.hyperic.sigar.*;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SystemInfoTracker {

    private Sigar sigar = new Sigar();

    public Map<String, Object> getPhysicalMemoryInfo() {
        Map<String, Object> memInfo = new HashMap<>();
        try {
            Mem mem = sigar.getMem();
            memInfo.put("subject", "RAM");
            memInfo.put("total", CommonUtil.formatByteUnit(mem.getTotal()));
            memInfo.put("used", CommonUtil.formatByteUnit(mem.getUsed()));
            memInfo.put("usedPercent", CommonUtil.doubleToPercent(mem.getUsedPercent() / 100.0));
            memInfo.put("usedPercentD", CommonUtil.round(mem.getUsedPercent(), 2));
            memInfo.put("free", CommonUtil.formatByteUnit(mem.getFree()));
            memInfo.put("freePercent", CommonUtil.doubleToPercent(mem.getFreePercent() / 100.0));
        } catch (SigarException e) {
            memInfo = Collections.emptyMap();
        }
        return memInfo;
    }

    public Map<String, String> getSwapInfo() {
        Map<String, String> swapInfo = new HashMap<>();
        try {
            Swap swap = sigar.getSwap();
            swapInfo.put("subject", "SWAP");
            swapInfo.put("total", CommonUtil.formatByteUnit(swap.getTotal()));
            swapInfo.put("used", CommonUtil.formatByteUnit(swap.getUsed()));
            swapInfo.put("usedPercent", CommonUtil.doubleToPercent((swap.getUsed() * 1.0) / swap.getTotal()));
            swapInfo.put("free", CommonUtil.formatByteUnit(swap.getFree()));
            swapInfo.put("freePercent", CommonUtil.doubleToPercent((swap.getFree() * 1.0) / swap.getTotal()));
        } catch (SigarException e) {
            swapInfo = Collections.emptyMap();
        }
        return swapInfo;
    }


    public Map<String, Object> getCpuInfo() {
        Map<String, Object> cpuInfo = new HashMap<>();
        try {
            double totalUsage = 0;
            CpuPerc[] cpuList = sigar.getCpuPercList();
            cpuList = (cpuList == null ? new CpuPerc[]{} : cpuList);
            List<Map<String, Object>> cpuUsageList = new ArrayList<>();
            for (int i = 0; i < cpuList.length; ++i) {
                cpuUsageList.add(cpuPercToMap(cpuList[i], i));
                totalUsage += cpuList[i].getCombined();
            }
            Collections.sort(cpuUsageList, Comparator.comparingInt(m -> ((int) m.get("index"))));
            cpuInfo.put("detail", cpuUsageList);
            cpuInfo.put("usage", CommonUtil.round((totalUsage * 100.0) / cpuList.length, 2));
        } catch (SigarException e) {
            cpuInfo = Collections.emptyMap();
        }
        return cpuInfo;
    }

    private Map<String, Object> cpuPercToMap(CpuPerc cpu, int index) {
        Map<String, Object> map = new HashMap<>();
        map.put("index", index);
        map.put("user", CpuPerc.format(cpu.getUser()));
        map.put("system", CpuPerc.format(cpu.getSys()));
        map.put("wait", CpuPerc.format(cpu.getWait()));
        map.put("error", CpuPerc.format(cpu.getNice()));
        map.put("idle", CpuPerc.format(cpu.getIdle()));
        map.put("total", CpuPerc.format(cpu.getCombined()));
        return map;
    }

    public Map<String, Object> getFileSystemInfo() {
        Map<String, Object> fsMap = new HashMap<>();
        try {
            List<Map<String, Object>> fsUsage = new ArrayList<>();
            FileSystem[] fsList = sigar.getFileSystemList();
            fsList = (fsList == null ? new FileSystem[]{} : fsList);
            for (FileSystem fs : fsList) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", fs.getDirName());
                map.put("type", fs.getSysTypeName());
                FileSystemUsage fsu = sigar.getFileSystemUsage(fs.getDirName());
                map.put("total", CommonUtil.formatKBUnit(fsu.getTotal()));
                map.put("used", CommonUtil.formatKBUnit(fsu.getUsed()));
                map.put("usedPercent", CommonUtil.doubleToPercent(fsu.getUsePercent()));
                map.put("free", CommonUtil.formatKBUnit(fsu.getFree()));
                map.put("available", CommonUtil.formatKBUnit(fsu.getAvail()));
                map.put("files", fsu.getFiles());
                fsUsage.add(map);
                if ("/".equals(fs.getDirName())) {
                    fsMap.put("usage", CommonUtil.round((fsu.getTotal() - fsu.getAvail()) * 100.0 / fsu.getTotal(), 2));
                }
            }
            Collections.sort(fsUsage, (f1, f2) -> String.valueOf(f1.get("name")).compareToIgnoreCase(String.valueOf(f2.get("name"))));
            fsMap.put("detail", fsUsage);
        } catch (Exception e) {
            fsMap = Collections.emptyMap();
        }
        return fsMap;
    }
}
