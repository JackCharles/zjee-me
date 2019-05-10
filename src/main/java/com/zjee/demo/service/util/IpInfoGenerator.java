package com.zjee.demo.service.util;

import com.alibaba.fastjson.JSON;
import com.zjee.demo.constant.Constant;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class IpInfoGenerator {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Cacheable(cacheNames = "ipInfo", key = "#ip", unless = "#result==null")
    public String getIpLocationInfo(String ip) {
        logger.info("IP:{}未命中缓存", ip);
        if (ip == null)
            return null;
        try {
            logger.info("开始调用API获取IP:{}的信息", ip);
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(Constant.IP_LOCATION_API + ip);
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            if(httpResponse.getStatusLine().getStatusCode() == 200) {
                byte[] bytes = new byte[1024];
                InputStream inputStream = httpResponse.getEntity().getContent();
                int len = inputStream.read(bytes);
                Map<String, Object> map = JSON.parseObject(new String(bytes, 0, len, StandardCharsets.UTF_8));
                logger.info("IP:{}的位置信息获取成功", ip);
                return new StringBuilder(map.get("country").toString())
                        .append(" - ")
                        .append(map.get("region"))
                        .append(" - ")
                        .append(map.get("city").toString())
                        .append(" - ")
                        .append(map.get("organization").toString())
                        .toString();
            }else {
                return "unknown";
            }
        } catch (Exception e) {
            logger.error("调用远程API失败，原因：{}",e.getMessage());
            return null;
        }
    }

    @CacheEvict(cacheNames = "ipInfo", allEntries = true)
    public void clearCache(){
        logger.info("清空缓存完成");
    }
}
