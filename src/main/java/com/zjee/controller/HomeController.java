package com.zjee.controller;

import com.alibaba.fastjson.JSON;
import com.zjee.constant.Constant;
import com.zjee.constant.ResponseStatus;
import com.zjee.controller.vo.CommonResponse;
import com.zjee.service.DownloadService;
import com.zjee.service.MottoService;
import com.zjee.service.SystemInfoService;
import com.zjee.service.VisitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@Slf4j
public class HomeController {

    private static Map<String, ClassLoader> classLoaderMap = new HashMap<>();

    private int visitCount = 0;
    @Autowired
    private SystemInfoService systemInfoService;
    @Autowired
    private DownloadService downloadService;
    @Autowired
    private VisitorService visitorService;
    @Autowired
    private MottoService mottoService;

    @GetMapping("/")
    public String home(Model model) {
        CommonResponse response = new CommonResponse(ResponseStatus.SUCCESS_CODE, ResponseStatus.SUCCESS_MSG);
        Map<String, Object> data = new HashMap<>();
        data.put("visitCount", ++visitCount);
        data.put("motto", mottoService.getRandomMotto());
        response.setData(data);
        model.addAttribute(Constant.RESPONSE_KEY, response);
        return "index";
    }

    @GetMapping(value = "/api/sysInfo")
    @ResponseBody
    public CommonResponse serverInfo() {
        CommonResponse response = new CommonResponse(ResponseStatus.SUCCESS_CODE, ResponseStatus.SUCCESS_MSG);
        response.setData(systemInfoService.getSystemInfo());
        return response;
    }

    @GetMapping("/api/download/**")
    @ResponseBody
    public Object download(HttpServletRequest httpRequest) {
        CommonResponse response = new CommonResponse(ResponseStatus.SUCCESS_CODE, ResponseStatus.SUCCESS_MSG);
        File file = downloadService.getFileFromUri(httpRequest.getRequestURI(), "/api/");
        if (file == null || !file.exists()) {
            response.setCode(ResponseStatus.ERROR_CODE);
            response.setMsg("file does not exist.");
            return response;
        } else if (file.isDirectory()) { //目录
            response.setData(downloadService.getFileList(file));
            return response;
        } else { //下载文件
            HttpHeaders headers = new HttpHeaders();
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Content-Disposition", "attachment; filename=" + file.getName());
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");
            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentLength(file.length())
                    .contentType(MediaType.parseMediaType("application/octet-stream"))
                    .body(new FileSystemResource(file));
        }

    }

    @GetMapping("/api/visitLog/{operation}")
    @ResponseBody
    public CommonResponse showVisitor(@PathVariable String operation) {
        CommonResponse response = new CommonResponse(ResponseStatus.SUCCESS_CODE, ResponseStatus.SUCCESS_MSG);
        if (StringUtils.isEmpty(operation)) {
            response.setCode(ResponseStatus.ERROR_CODE);
            response.setMsg("path variable can not be empty.");
        }
        if ("clearCache".equals(operation)) {
            visitorService.clearIpInfoCache();
        } else if (operation.matches("\\d{4}-\\d{2}-\\d{2}")) {//日期
            Map<String, Object> data = new HashMap<>();
            data.put("visitorList", visitorService.getVisitorList(operation));
            data.put("date", operation);
            data.put("latestPv", visitorService.getPvList());
            response.setData(data);
        } else {
            response.setCode(ResponseStatus.ERROR_CODE);
            response.setMsg("Illegal operation.");
        }
        return response;
    }

    @RequestMapping("/dynamic")
    public String dynamicIn() {
        return "dynimicInvoke";
    }

    @RequestMapping("/dynamicInvoke")
    @ResponseBody
    public Object dynamicInvoke(String groupId, String artifactId, String version,
                                String clazzName, String method, String paramTypes,
                                String args, boolean staticCall) {
        Class<?> myClazz = null;
        try {
            myClazz = Class.forName(clazzName, true, classLoaderMap.get(clazzName));
        } catch (ClassNotFoundException e) {
            log.warn("need load class {}", clazzName);
        }

        if (myClazz == null) {
            String path = dynamicAddJar(groupId, artifactId, version);
            if (path == null) {
                return "can not found jar lib, may be download filed, please try it again later!";
            }
            try {
                URLClassLoader myClassLoader = URLClassLoader.newInstance(new URL[]{Paths.get(path).toUri().toURL()},
                        Thread.currentThread().getContextClassLoader());
                myClazz = myClassLoader.loadClass(clazzName);
                classLoaderMap.put(clazzName, myClazz.getClassLoader());
            } catch (Exception e) {
                return e.toString();
            }
        }
        Object ret;
        Method mth;
        List<String> types = paramTypes == null ? Collections.EMPTY_LIST :
                Arrays.stream(paramTypes.split(",")).map(String::trim).collect(Collectors.toList());

        List<String> params = args == null ? Collections.EMPTY_LIST :
                Arrays.stream(args.split("&")).map(String::trim).collect(Collectors.toList());

        try {
            if (types.size() > 0) {
                mth = myClazz.getMethod(method, resolveClass(types));
                mth.setAccessible(true);
                ret = mth.invoke(staticCall ? myClazz : myClazz.newInstance(), resolveParms(types, params));
            } else {
                mth = myClazz.getMethod(method);
                mth.setAccessible(true);
                ret = mth.invoke(staticCall ? myClazz : myClazz.newInstance());
            }
        } catch (Exception e) {
            log.error("invoke error: ", e);
            return e.toString();
        }
        return ret;
    }


    private String dynamicAddJar(String groupId, String artifactId, String version) {
        if (StringUtils.isEmpty(groupId) || StringUtils.isEmpty(artifactId) || StringUtils.isEmpty(version)) {
            return null;
        }

        String fileName = artifactId + "-" + version + ".jar";
        String downloadUrl = "https://repo1.maven.org/maven2/" +
                groupId.replace('.', '/') + "/" +
                artifactId + "/" + version + "/" + fileName;
        String basePath = "ext-lib/" + groupId + "/" + artifactId + "/";

        if (new File(basePath + fileName).exists()) {
            return basePath + fileName;
        }

        // 下载网络文件
        int bytesum = 0;
        int byteread = 0;
        try {
            URL url = new URL(downloadUrl);
            URLConnection conn = url.openConnection();
            InputStream inStream = conn.getInputStream();
            if (new File(basePath).mkdirs()) {
                FileOutputStream fs = new FileOutputStream(basePath + fileName);

                byte[] buffer = new byte[1204];
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread;
                    log.info("{} download: {}", fileName, bytesum);
                    fs.write(buffer, 0, byteread);
                }
            }
        } catch (Exception e) {
            log.error("download jar failed: ", e);
            return null;
        }
        return basePath + fileName;
    }

    private Class<?>[] resolveClass(List<String> clazz) {
        if (clazz == null || clazz.size() == 0) {
            return null;
        }
        List<Class<?>> classes = new ArrayList<>(clazz.size());
        for (String s : clazz) {
            switch (s) {
                case "int":
                    classes.add(int.class);
                    break;
                case "long":
                    classes.add(long.class);
                    break;
                case "double":
                    classes.add(double.class);
                    break;
                case "float":
                    classes.add(float.class);
                    break;
                case "boolean":
                    classes.add(boolean.class);
                    break;
                case "short":
                    classes.add(short.class);
                    break;
                case "byte":
                    classes.add(byte.class);
                    break;
                case "char":
                    classes.add(char.class);
                    break;
                default:
                    classes.add(getClassFromName(s));
            }
        }
        return classes.toArray(new Class[0]);
    }

    private Class getClassFromName(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            log.error("class not found: ", e);
        }
        return Object.class;
    }

    private Object[] resolveParms(List<String> clazz, List<String> args) {
        if (args == null || args.size() == 0) {
            return null;
        }
        List parms = new ArrayList<>(args.size());
        for (int i = 0; i < clazz.size(); i++) {
            try {
                switch (clazz.get(i)) {
                    case "int":
                        parms.add(Integer.parseInt(args.get(i)));
                        break;
                    case "long":
                        parms.add(Long.parseLong(args.get(i)));
                        break;
                    case "double":
                        parms.add(Double.parseDouble(args.get(i)));
                        break;
                    case "float":
                        parms.add(Float.parseFloat(args.get(i)));
                        break;
                    case "boolean":
                        parms.add(Boolean.parseBoolean(args.get(i)));
                        break;
                    case "short":
                        parms.add(Short.parseShort(args.get(i)));
                        break;
                    case "char":
                        parms.add(args.get(i).charAt(0));
                        break;
                    case "byte":
                        parms.add(Byte.parseByte(args.get(i)));
                        break;
                    default:
                        parms.add(JSON.parseObject(args.get(i), getClassFromName(clazz.get(i))));
                }
            } catch (Exception e) {
                log.error("parse value error: ", e);
            }
        }
        return parms.toArray();
    }

    @RequestMapping("/api/bandwidth/cut")
    @ResponseBody
    public String bandwidthDailyCut() {
        systemInfoService.statisticBandwidthUsage();
        return "OK";
    }
}