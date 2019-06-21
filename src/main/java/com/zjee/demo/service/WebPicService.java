package com.zjee.demo.service;

import com.zjee.demo.constant.Constant;
import com.zjee.demo.constant.ResponseStatus;
import com.zjee.demo.service.util.CommonUtil;
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

import java.io.BufferedReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Date: 2019-06-21 13:37
 * Author: zhongjie03
 * E-mail: zhongjie03@meituan.com
 * Description:
 */

@Component
public class WebPicService {

    @Cacheable(cacheNames = "imgUrls",  key = "#key", unless = "#result == null")
    public List<String> batchGetPhotoUrl(String key) throws Exception {
        HttpClient client = HttpClients.createDefault();
        URIBuilder uriBuilder = new URIBuilder(Constant.IMG_URL)
                .addParameter("key", Constant.PIX_KEY)
                .addParameter("image_type", "photo")
                .addParameter("orientation", "horizontal")
                .addParameter("min_width", "1920")
                .addParameter("min_height", "1080")
                .addParameter("safesearch", "true")
                .addParameter("per_page", "200");
        HttpGet get = new HttpGet(uriBuilder.build());
        HttpResponse response = client.execute(get);
        if(response == null || response.getStatusLine().getStatusCode() != ResponseStatus.SUCCESS_CODE){
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
