package com.zjee.demo;

import com.zjee.demo.service.WeatherService;
import org.junit.Test;

/**
 * Date: 2019-05-29 13:25
 * Author: zhongjie03
 * E-mail: zhongjie03@meituan.com
 * Description:
 */

public class NoSpringTest {

    private WeatherService weatherService = new WeatherService();

    @Test
    public void apiTest() throws Exception{
        System.out.println(weatherService.getForecastBywoeId(2151330));
    }
}
