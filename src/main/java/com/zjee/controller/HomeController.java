package com.zjee.controller;

import com.sun.org.apache.xpath.internal.operations.Bool;
import com.zjee.constant.Constant;
import com.zjee.constant.ResponseStatus;
import com.zjee.controller.vo.CommonResponse;
import com.zjee.service.DownloadService;
import com.zjee.service.MottoService;
import com.zjee.service.SystemInfoService;
import com.zjee.service.VisitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@Slf4j
public class HomeController {

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
        }
        else if (file.isDirectory()) { //目录
            response.setData(downloadService.getFileList(file));
            return response;
        }
        else { //下载文件
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
        }
        else if (operation.matches("\\d{4}-\\d{2}-\\d{2}")) {//日期
            Map<String, Object> data = new HashMap<>();
            data.put("visitorList", visitorService.getVisitorList(operation));
            data.put("date", operation);
            data.put("latestPv", visitorService.getPvList());
            response.setData(data);
        }
        else {
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
    public Object dynamicInvoke(String clazzName, String method, String paramTypes, String args) throws Exception {
        Path jarPath = Paths.get("ext-lib/extLib.jar");
        URL url = jarPath.toUri().toURL();
        URLClassLoader myClassLoader = URLClassLoader.newInstance(new URL[]{url}, Thread.currentThread().getContextClassLoader());
        Class<?> myClazz;
        try {
            myClazz = myClassLoader.loadClass(clazzName);
        }catch (Exception e) {
            return e.toString();
        }
        Object ret;
        Method mth;
        List<String> types = paramTypes == null ? Collections.EMPTY_LIST :
                Arrays.stream(paramTypes.split(",")).map(String::trim).collect(Collectors.toList());

        List<String> params = args == null ? Collections.EMPTY_LIST :
                Arrays.stream(args.split(",")).map(String::trim).collect(Collectors.toList());

        if(types.size() > 0) {
            mth = myClazz.getMethod(method, resolveClass(types));
            mth.setAccessible(true);
            ret = mth.invoke(myClazz.newInstance(), resolveParms(types, params));
        }else {
            mth = myClazz.getMethod(method);
            mth.setAccessible(true);
            ret = mth.invoke(myClazz.newInstance());
        }
        return ret;
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
                case "String":
                    classes.add(String.class);
                    break;
                default:
                    try {
                        Class<?> aClass = Class.forName(s);
                        classes.add(aClass);
                    } catch (ClassNotFoundException e) {
                        log.error("class not found: ", e);
                    }
            }
        }
        return classes.toArray(new Class[0]);
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
                    case "String":
                        parms.add(String.valueOf(args.get(i)));
                        break;
                    default:
                }
            }catch (Exception e) {
                log.error("parse value error: ", e);
            }
        }
        return parms.toArray();
    }
}