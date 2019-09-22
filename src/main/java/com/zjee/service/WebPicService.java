package com.zjee.service;

import com.zjee.constant.Constant;
import com.zjee.constant.ResponseStatus;
import com.zjee.service.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Date: 2019-06-21 13:37
 * Author: zhongjie03
 * E-mail: zhongjie03@meituan.com
 * Description:
 */

@Component
@Slf4j
public class WebPicService {

    @Cacheable(cacheNames = "imgUrls",  key = "#keyWord+#date", unless = "#result == null")
    public List<String> batchGetPhotoUrl(String keyWord, String date) throws Exception {
        log.info("batchGetPhotoUrl未命中缓存：keyword = {}, date = {}", keyWord, date);
        HttpClient client = HttpClients.createDefault();
        URIBuilder uriBuilder = new URIBuilder(Constant.IMG_URL)
                .addParameter("key", Constant.PIX_KEY)
                .addParameter("q", keyWord)
                .addParameter("lang", CommonUtil.getLanguage(keyWord))
                .addParameter("image_type", "photo")
                .addParameter("orientation", "horizontal")
                .addParameter("min_width", "3839")
                .addParameter("min_height", "2159")
                .addParameter("per_page", "200");
        HttpGet get = new HttpGet(uriBuilder.build());
        HttpResponse response = client.execute(get);
        if(response == null || response.getStatusLine().getStatusCode() != ResponseStatus.SUCCESS_CODE){
            log.error("pixabay API请求失败, url: {}", uriBuilder.build().toString());
            throw new Exception("pixabay API请求失败");
        }
        String res = CommonUtil.readStreamToString(response.getEntity().getContent());
        List<String> urls = new ArrayList<>(200);
        if(!StringUtils.isEmpty(res)){
            JSONObject jsonObject = new JSONObject(res);
            JSONArray jsonArray = jsonObject.getJSONArray("hits");
            for(int i=0; i< jsonArray.length(); ++i){
                urls.add(jsonArray.optJSONObject(i).optString("largeImageURL", "error"));
            }
        }
        return urls;
    }
}
