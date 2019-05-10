package com.zjee.demo.service.util;

import org.hyperic.sigar.*;
import org.springframework.stereotype.Component;

import java.lang.management.*;
import java.util.*;

@Component
public class SystemInfoTracker {

    private Sigar sigar = new Sigar();

    public Map<String, Object> getOperationSystemInfo(){
        return OperatingSystem.getInstance().toMap();
    }

    public Map<String, Object> getMachineInfo() {
        Properties prop = System.getProperties();
        Map<String, String> evn = System.getenv();
        String userName = evn.get("USERNAME");
        String computerName = evn.get("COMPUTERNAME");
        String userDomain = evn.get("USERDOMAIN");

        Map<String, Object> machInfo = new HashMap<>();
        machInfo.put("computer name", computerName);
        machInfo.put("user domain", userDomain);
        machInfo.put("user name", prop.getProperty("user.name"));
        machInfo.put("user home", prop.getProperty("user.home"));
        machInfo.put("working dir", prop.getProperty("user.dir"));

        List<Map<String, Object>> userList = new ArrayList<>();
        try {
            Who[] who = sigar.getWhoList();
            who = (who == null ? new Who[]{} : who);
            for (int i = 0; i < who.length && who != null; ++i) {
                userList.add(who[i].toMap());
            }
        } catch (SigarException e) {
            userList = Collections.emptyList();
        }
        machInfo.put("users", userList);
        return machInfo;
    }

    public Map<String, Object> getPhysicalMemoryInfo(){
        Map<String, Object> memInfo;
        try{
            memInfo = sigar.getMem().toMap();
        } catch (SigarException e) {
            memInfo = Collections.emptyMap();
        }
        return memInfo;
    }

    public Map<String, Object> getCpuInfo(){
        Map<String, Object> cpuInfo = new HashMap<>();
        try{
            CpuInfo[] cpuInfos = sigar.getCpuInfoList();
            CpuPerc[] cpuList = sigar.getCpuPercList();
            cpuInfos = (cpuInfos == null ? new CpuInfo[]{} : cpuInfos);
            cpuList = (cpuList == null ? new CpuPerc[]{} : cpuList);
            for(int i=0; i<cpuInfos.length; ++i){
                Map<String, Object> map = new HashMap<>();
                map.put("info", cpuInfos[i].toMap());
                map.put("usage", cpuPercToMap(cpuList[i]));
                cpuInfo.put("CPU"+i, map);
            }
        } catch (SigarException e) {
            cpuInfo = Collections.emptyMap();
        }
        return cpuInfo;
    }

    private Map<String, Object> cpuPercToMap(CpuPerc cpu) {
        Map<String, Object> map = new HashMap<>();
        map.put("user", CpuPerc.format(cpu.getUser()));
        map.put("system", CpuPerc.format(cpu.getSys()));
        map.put("wait", CpuPerc.format(cpu.getWait()));
        map.put("error", CpuPerc.format(cpu.getNice()));
        map.put("idle", CpuPerc.format(cpu.getIdle()));
        return map;
    }

    public Map<String, Object> getFileSystemInfo(){
        Map<String, Object> fsMap = new HashMap<>();
        try {
            FileSystem[] fsList = sigar.getFileSystemList();
            fsList = (fsList == null ? new FileSystem[]{} : fsList);
            for(FileSystem fs : fsList){
                Map<String, Object> map = new HashMap<>();
                map.put("info", fs.toMap());
                map.put("usage", sigar.getFileSystemUsage(fs.getDirName()).toMap());
                fsMap.put(fs.getDirName(), map);
            }
        }catch (Exception e){
            fsMap = Collections.emptyMap();
        }
        return fsMap;
    }

    public Map<String, Object> getNetworkInfo(){
        Map<String, Object> netInfo = new HashMap<>();
        try{
           String[] ifList =  sigar.getNetInterfaceList();
           ifList = (ifList == null ? new String[]{} : ifList);
           for(String ifName : ifList){
               Map<String, Object> map = new HashMap<>();
               NetInterfaceConfig ifConfig = sigar.getNetInterfaceConfig(ifName);
               map.put("config", ifConfig.toMap());
               if ((ifConfig.getFlags() & 1L) <= 0L)  //this interface is down
                   continue;
               map.put("stat", sigar.getNetInterfaceStat(ifName).toMap());
               netInfo.put(ifName, map);
           }
        }catch (Exception e){
            netInfo = Collections.emptyMap();
        }
        return netInfo;
    }

    public Map<String, Object> getJvmStaticInfo(){
        Map<String, Object> jvmInfo = new HashMap();
        Properties props = System.getProperties();

        jvmInfo.put("Java的运行环境版本", props.getProperty("java.version"));
        jvmInfo.put("Java的运行环境供应商", props.getProperty("java.vendor"));
        jvmInfo.put("Java供应商的URL", props.getProperty("java.vendor.url"));
        jvmInfo.put("Java的安装路径", props.getProperty("java.home"));
        jvmInfo.put("Java的虚拟机规范版本", props.getProperty("java.vm.specification.version"));
        jvmInfo.put("Java的虚拟机规范供应商", props.getProperty("java.vm.specification.vendor"));
        jvmInfo.put("Java的虚拟机规范名称", props.getProperty("java.vm.specification.name"));
        jvmInfo.put("Java的虚拟机实现版本", props.getProperty("java.vm.version"));
        jvmInfo.put("Java的虚拟机实现供应商", props.getProperty("java.vm.vendor"));
        jvmInfo.put("Java的虚拟机实现名称", props.getProperty("java.vm.name"));
        jvmInfo.put("Java运行时环境规范版本", props.getProperty("java.specification.version"));
        jvmInfo.put("Java运行时环境规范供应商", props.getProperty("java.specification.vendor"));
        jvmInfo.put("Java运行时环境规范名称", props.getProperty("java.specification.name"));
        jvmInfo.put("Java的类格式版本号", props.getProperty("java.class.version"));
        jvmInfo.put("Java的类路径", props.getProperty("java.class.path"));
        jvmInfo.put("加载库时搜索的路径列表", props.getProperty("java.library.path"));
        jvmInfo.put("默认的临时文件路径", props.getProperty("java.io.tmpdir"));
        jvmInfo.put("一个或多个扩展目录的路径", props.getProperty("java.ext.dirs"));

        return jvmInfo;
    }

    public Map<String, Object> getJvmRuntimeInfo(){
        Map<String, Object> jvmInfo = new HashMap();
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> map = new HashMap<>();//temp

        //JVM基本信息
        RuntimeMXBean mxbean = ManagementFactory.getRuntimeMXBean();
        map.put("jvm name", mxbean.getVmName());
        map.put("jvm version", mxbean.getVmVersion());
        map.put("jvm start time", mxbean.getStartTime());
        map.put("jvm total memory", runtime.totalMemory());
        map.put("jvm free memory", runtime.freeMemory());
        map.put("jvm available processors", runtime.availableProcessors());
        jvmInfo.put("jvm", map);

        //JVM线程信息
        map = new HashMap<>();
        ThreadMXBean thread = ManagementFactory.getThreadMXBean();
        map.put("thread count", thread.getThreadCount());
        map.put("daemon thread count", thread.getDaemonThreadCount());
        map.put("peak thread count", thread.getPeakThreadCount());
        map.put("total started thread count", thread.getTotalStartedThreadCount());
        jvmInfo.put("thread", map);

        //GC信息
        List<Map> list = new ArrayList<>();
        List<GarbageCollectorMXBean> gc = ManagementFactory.getGarbageCollectorMXBeans();
        for(GarbageCollectorMXBean gcm : gc){
            map = new HashMap<>();
            map.put("garbage collector name", gcm.getName());
            map.put("garbage collection count", gcm.getCollectionCount());
            map.put("garbage collection time", gcm.getCollectionTime());
            list.add(map);
        }
        jvmInfo.put("gc", list);

        //堆内存信息
        map = new HashMap<>();
        MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = mem.getHeapMemoryUsage();
        map.put("heap committed", heap.getCommitted());
        map.put("heap init", heap.getInit());
        map.put("heap max", heap.getMax());
        map.put("heap used", heap.getUsed());
        jvmInfo.put("heap", map);

        //JVM内存管理器信息
        list = new ArrayList<>();
        List<MemoryManagerMXBean> mm = ManagementFactory.getMemoryManagerMXBeans();
        for(MemoryManagerMXBean eachmm : mm){
            map = new HashMap<>();
            map.put("memory manager name", eachmm.getName());
            map.put("memory manager valid", eachmm.isValid());
            list.add(map);
        }
        jvmInfo.put("memory manager",list);

        //JVM内存池信息
        list = new ArrayList<>();
        List<MemoryPoolMXBean> mps = ManagementFactory.getMemoryPoolMXBeans();
        for(MemoryPoolMXBean mp : mps){
            map = new HashMap<>();
            map.put("memory pool name", mp.getName());
            map.put("memory pool type", mp.getType());
            map.put("collection usage", mp.getCollectionUsage());
            map.put("collection peak usage", mp.getPeakUsage());
            map.put("collection valid", mp.isValid());
            list.add(map);
        }
        jvmInfo.put("memory pool", list);
        return jvmInfo;
    }
}
