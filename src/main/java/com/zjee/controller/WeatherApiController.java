package com.zjee.controller;

import com.zjee.constant.ResponseStatus;
import com.zjee.controller.vo.CommonResponse;
import com.zjee.service.WeatherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Date: 2019-05-29 09:46
 * Author: zhongjie03
 * E-mail: zhongjie03@meituan.com
 * Description:
 */

@Slf4j
@RestController
@RequestMapping(value = "api")
public class WeatherApiController {

    @Autowired
    WeatherService weatherService;

    @GetMapping("woeid")
    @CrossOrigin
    public CommonResponse getWoeid(String keyWord){
        CommonResponse response = new CommonResponse();
        if(keyWord == null){
            response.setCode(ResponseStatus.ERROR_CODE);
            response.setMsg("keyWord can not be null!");
            return response;
        }
        Object location = weatherService.getWoeidByKeyWord(keyWord);
        if(location != null){
            response.setCode(ResponseStatus.SUCCESS_CODE);
            response.setMsg(ResponseStatus.SUCCESS_MSG);
            response.setData(location);
        }else {
            response.setCode(ResponseStatus.ERROR_CODE);
            response.setMsg(ResponseStatus.DEFAULT_ERROR_MSG);
        }
        return response;
    }


    @GetMapping("weather")
    @CrossOrigin
    public CommonResponse getWeatherForecast(int woeid){
        CommonResponse response = new CommonResponse();
        try {
            Map<String, ?> forecast = weatherService.getForecastBywoeId(woeid);
            if(forecast != null ){
                response.setCode(ResponseStatus.SUCCESS_CODE);
                response.setMsg(ResponseStatus.SUCCESS_MSG);
                response.setData(forecast);
                return response;
            }
        } catch (Exception e) {
            log.error("Exception", e);
        }
        response.setCode(ResponseStatus.ERROR_CODE);
        response.setMsg(ResponseStatus.DEFAULT_ERROR_MSG);
        return response;
    }
}
