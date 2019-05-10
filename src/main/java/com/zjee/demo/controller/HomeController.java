package com.zjee.demo.controller;

import com.zjee.demo.constant.Constant;
import com.zjee.demo.constant.ResponseStatus;
import com.zjee.demo.controller.vo.CommonResponse;
import com.zjee.demo.service.DownloadService;
import com.zjee.demo.service.SystemInfoService;
import com.zjee.demo.service.VisitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.*;

@Controller
public class HomeController {

    private int visitCount = 0;
    @Autowired
    private SystemInfoService systemInfoService;
    @Autowired
    private DownloadService downloadService;
    @Autowired
    private VisitorService visitorService;

    @GetMapping("/")
    public String home(Model model) {
        CommonResponse response = new CommonResponse(ResponseStatus.SUCCESS_CODE, ResponseStatus.SUCCESS_MSG);
        Map<String, Object> data = new HashMap<>();
        data.put("visitCount", ++visitCount);
        response.setData(data);
        model.addAttribute(Constant.RESPONSE_KEY, response);
        return "home";
    }

    @GetMapping(value = "/server")
    @ResponseBody
    public CommonResponse serverInfo() {
        CommonResponse response = new CommonResponse(ResponseStatus.SUCCESS_CODE, ResponseStatus.SUCCESS_MSG);
        response.setData(systemInfoService.getSystemInfo());
        return response;
    }

    @GetMapping(value = "/jvm")
    @ResponseBody
    public CommonResponse JvmInfo() {
        CommonResponse response = new CommonResponse(ResponseStatus.SUCCESS_CODE, ResponseStatus.SUCCESS_MSG);
        response.setData(systemInfoService.getJvmInfo());
        return response;
    }

    @GetMapping("/download/**")
    public String download(Model model, HttpServletRequest httpRequest) {
        CommonResponse response = new CommonResponse(ResponseStatus.SUCCESS_CODE, ResponseStatus.SUCCESS_MSG);
        Map<String, Object> data = new HashMap<>();
        File file = downloadService.getFileFromUri(httpRequest.getRequestURI());
        if (file == null || !file.exists()) {
            response.setCode(ResponseStatus.ERROR_CODE);
            response.setMsg("file not exists.");
        } else if (file.isFile()) {
            httpRequest.setAttribute("file", file);
            return "forward:/startDownload";
        } else if (file.isDirectory()) {
            data.put("fileList", downloadService.getFileList(file));
        }
        response.setData(data);
        model.addAttribute(Constant.RESPONSE_KEY, response);
        return "download";
    }

    @GetMapping(value = "/startDownload")
    @ResponseBody
    public Object downloadFile(HttpServletRequest httpRequest) {
        File file = (File) httpRequest.getAttribute("file");
        if (file == null) {
            return "Invalid request";
        }
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

    @GetMapping({"/visitor", "/visitor/{operation}"})
    public String showVisitor(@PathVariable(required = false) String operation, Model model) {
        CommonResponse response = new CommonResponse(ResponseStatus.SUCCESS_CODE, ResponseStatus.SUCCESS_MSG);
        Map<String, Object> data = new HashMap<>();
        if (operation == null) {
            data.put("dateList", visitorService.getSortedDateList());
            data.put("isDetail", false);
        } else if (operation.equals("clearCache")) {
            visitorService.clearIpInfoCache();
            return "redirect:/visitor";
        } else if (operation.matches("\\d{4}-\\d{2}-\\d{2}")) {//日期
            data.put("visitorList", visitorService.getVisitorList(operation));
            data.put("isDetail", true);
            data.put("date", operation);
        } else {
            response.setCode(ResponseStatus.ERROR_CODE);
            response.setMsg("Illegal operation.");
        }
        response.setData(data);
        model.addAttribute(Constant.RESPONSE_KEY, response);
        return "visitor";
    }
}