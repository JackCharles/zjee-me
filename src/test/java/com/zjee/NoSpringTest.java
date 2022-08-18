package com.zjee;

import com.zjee.service.WeatherService;
import com.zjee.service.WebPicService;
import org.junit.Test;

import java.time.LocalDate;

/**
 * Date: 2019-05-29 13:25
 * Author: zhongjie03
 * E-mail: zhongjie03@meituan.com
 * Description:
 */

public class NoSpringTest {

    private WeatherService weatherService = new WeatherService();

    private WebPicService webPicService = new WebPicService();

    @Test
    public void apiTest() throws Exception {
        System.out.println(weatherService.getForecastBywoeId(2151330));
    }

    @Test
    public void imageUrlTest() throws Exception {
        webPicService.batchGetPhotoUrl("内华达", LocalDate.now().toString()).forEach(System.out::println);
    }
}
