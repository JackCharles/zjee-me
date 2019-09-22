package com.zjee.service;

import com.zjee.constant.Constant;
import com.zjee.constant.ResponseStatus;
import com.zjee.pojo.Motto;
import com.zjee.service.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Slf4j
public class MottoService {

    public Motto getRandomMotto(String type) {
        HttpClient client = HttpClients.createDefault();
        Motto motto = new Motto();
        try {
            URIBuilder uriBuilder = new URIBuilder(Constant.HITOKOTO_URL);
            if (!StringUtils.isEmpty(type)) {
                uriBuilder.addParameter("c", type);
            }
            HttpResponse response = client.execute(new HttpGet(uriBuilder.build()));
            if (response == null || response.getStatusLine().getStatusCode() != ResponseStatus.SUCCESS_CODE) {
                log.error("hitokoto API请求失败, url: {}, 原因：{}", Constant.HITOKOTO_URL, response.getStatusLine().getReasonPhrase());
                return motto;
            }
            String jsonStr = CommonUtil.readStreamToString(response.getEntity().getContent());
            if (!StringUtils.isEmpty(jsonStr)) {
                JSONObject jsonObject = new JSONObject(jsonStr);
                motto.setId(jsonObject.optInt("id", -1));
                motto.setHitokoto(jsonObject.optString("hitokoto", "unknown"));
                motto.setType(jsonObject.optString("type", "unknown"));
                motto.setFrom(jsonObject.optString("from", "unknown"));
                motto.setCreator(jsonObject.optString("creator", "unknown"));
                return motto;
            }
        } catch (Exception e) {
            log.error("an error accrued: ", e);
        }
        return motto;
    }

    public Motto getRandomMotto() {
        return getRandomMotto(null);
    }
}
