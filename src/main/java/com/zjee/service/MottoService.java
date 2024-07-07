package com.zjee.service;

import com.zjee.common.model.Motto;
import com.zjee.common.util.HttpUtil;
import com.zjee.common.util.JsonUtil;
import com.zjee.constant.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@SuppressWarnings("rawtypes")
public class MottoService {

    public Motto getRandomMotto(String type) {
        Map<String, String> params = new HashMap<>();
        if (type == null || type.length() == 0) {
            params.put("c", type);
        }
        String resJson = HttpUtil.get(Constant.HITOKOTO_URL, params, null);
        Map map = JsonUtil.fromJson(resJson, Map.class);
        if (null == map) {
            return null;
        }
        Motto motto = new Motto();
        motto.setId((Integer) map.get("id"));
        motto.setHitokoto((String) map.get("hitokoto"));
        motto.setType((String) map.get("type"));
        motto.setFrom((String) map.get("from"));
        motto.setCreator((String) map.get("creator"));
        return motto;
    }

    public Motto getRandomMotto() {
        return getRandomMotto(null);
    }
}
