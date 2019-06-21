package com.zjee.demo.controller;

import com.zjee.demo.constant.ResponseStatus;
import com.zjee.demo.controller.vo.CommonResponse;
import com.zjee.demo.service.WebPicService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Date: 2019-06-21 14:15
 * Author: zhongjie03
 * E-mail: zhongjie03@meituan.com
 * Description:
 */

@RestController
@RequestMapping("api")
@Slf4j
public class WebImgController {

    @Autowired
    private WebPicService webPicService;

    @RequestMapping("webImage")
    public CommonResponse getWebImgUrl(){
        CommonResponse response = new CommonResponse();
        try {
            List<String> urlList = webPicService.batchGetPhotoUrl(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            if (!CollectionUtils.isEmpty(urlList)){
                response.setCode(ResponseStatus.SUCCESS_CODE);
                response.setMsg(ResponseStatus.SUCCESS_MSG);
                response.setData(urlList);
            }
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
            response.setCode(ResponseStatus.ERROR_CODE);
            response.setMsg(e.getLocalizedMessage());
        }finally {
            return response;
        }
    }
}
