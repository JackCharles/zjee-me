package com.zjee.controller;

import com.zjee.controller.vo.CommonResponse;
import com.zjee.constant.Constant;
import com.zjee.constant.ResponseStatus;
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
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

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
        } else if (file.isDirectory()) { //目录
            response.setData(downloadService.getFileList(file));
            return response;
        }else { //下载文件
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

    @GetMapping( "/api/visitLog/{operation}")
    @ResponseBody
    public CommonResponse showVisitor(@PathVariable String operation) {
        CommonResponse response = new CommonResponse(ResponseStatus.SUCCESS_CODE, ResponseStatus.SUCCESS_MSG);
        if(StringUtils.isEmpty(operation)) {
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
}