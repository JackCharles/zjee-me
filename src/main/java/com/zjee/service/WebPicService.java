package com.zjee.service;

import com.zjee.common.util.CommonUtil;
import com.zjee.common.util.HttpUtil;
import com.zjee.common.util.JsonUtil;
import com.zjee.constant.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Date: 2019-06-21 13:37
 * Author: zhongjie
 * Description: web图片获取接口
 */

@Component
@Slf4j
public class WebPicService {


    public String getRandomPicUrl(String keyWord, LocalDate date) {
        List<String> urlList = getWebPicture(keyWord, date);
        if(CollectionUtils.isEmpty(urlList)) {
            return null;
        }
        Random random = new Random(System.currentTimeMillis());
        int randomIndex = random.nextInt(urlList.size());
        return urlList.get(randomIndex);
    }

    @Cacheable(cacheNames = "imgUrls",  key = "#keyWord+#date", unless = "#result.isEmpty()")
    @SuppressWarnings({"rawtypes", "unchecked"})
    public List<String> getWebPicture(String keyWord, LocalDate date) {
        log.info("batchGetPhotoUrl未命中缓存：keyword = {}, date = {}", keyWord, date);
        Map<String, String> param = new HashMap<>();
        param.put("key", Constant.PIX_KEY);
        param.put("q", keyWord);
        param.put("lang", CommonUtil.getLanguage(keyWord));
        param.put("image_type", "photo");
        param.put("orientation", "horizontal");
        param.put("min_width", "3839");
        param.put("min_height", "2159");
        param.put("per_page", "200");

        String resJson = HttpUtil.get(Constant.IMG_URL, param, null);
        Map map = JsonUtil.fromJson(resJson, Map.class);
        if(null == map || !map.containsKey("hits")) {
            return Collections.emptyList();
        }
        List<Map> hits = (List<Map>) map.get("hits");
        return hits.stream().map(h -> (String)h.get("largeImageURL")).collect(Collectors.toList());
    }
}
