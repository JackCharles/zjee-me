package com.zjee.controller;

import com.zjee.constant.ResponseStatus;
import com.zjee.controller.vo.CommonResponse;
import com.zjee.service.WebPicService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

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
    public CommonResponse getWebImgUrl(@RequestParam(required = false, defaultValue = "自然风景") String keyWord,
                                       @RequestParam(required = false, defaultValue = "0") Integer batch) {
        CommonResponse response = new CommonResponse();
        response.setCode(ResponseStatus.ERROR_CODE);
        try {
            List<String> urlList = webPicService.batchGetPhotoUrl(keyWord, LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            if (!CollectionUtils.isEmpty(urlList)){
                response.setCode(ResponseStatus.SUCCESS_CODE);
                response.setMsg(ResponseStatus.SUCCESS_MSG);
                if(batch == 1) {
                    response.setData(urlList);
                }else {
                    Random random = new Random(System.currentTimeMillis());
                    int randomIndex = random.nextInt(urlList.size());
                    response.setData(urlList.get(randomIndex));
                }
            }else {
                response.setMsg(String.format("没有检索到与【%s】相关的内容", keyWord));
            }
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
            response.setMsg(e.getLocalizedMessage());
        }finally {
            return response;
        }
    }
}
