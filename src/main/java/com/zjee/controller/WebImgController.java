package com.zjee.controller;

import com.zjee.constant.ResponseStatus;
import com.zjee.controller.vo.CommonResponse;
import com.zjee.service.WebPicService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

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

        try {
            response.setCode(ResponseStatus.SUCCESS_CODE);
            response.setData(webPicService.getRandomPicUrl(keyWord, LocalDate.now()));
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
            response.setCode(ResponseStatus.ERROR_CODE);
            response.setMsg(e.getLocalizedMessage());
        }
        return response;
    }
}
