package com.zjee;

import com.zjee.controller.WeatherApiController;
import com.zjee.controller.vo.CommonResponse;
import com.zjee.service.util.IpInfoGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class zjeeApplicationTests {

    @Autowired
    private IpInfoGenerator ipInfoGenerator;

    @Autowired
    WeatherApiController weatherApiController;


    @Test
    public void test(){
        System.out.println(ipInfoGenerator.getIpLocationInfo("49.140.86.146"));
    }

    @Test
    public void getWoeidTest(){
        CommonResponse response = weatherApiController.getWoeid("changchun");
        Integer woeid = (Integer) ((Map)response.getData()).get("woeid");
        System.out.println(woeid);
        CommonResponse weatherForecast = weatherApiController.getWeatherForecast(woeid);
        System.out.println(weatherForecast);
    }

}
