package com.zjee.common.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

/**
 * @author ZhongJie
 * @date 21:33
 * @desc json util
 */
public class JsonUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @SneakyThrows
    public static String toJson(Object o) {
        if (null == o) {
            return null;
        }
        return OBJECT_MAPPER.writeValueAsString(o);
    }

    @SneakyThrows
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (null == json || json.isEmpty()) {
            return null;
        }
        return OBJECT_MAPPER.readValue(json, clazz);
    }

    @SneakyThrows
    public static <T> T fromJson(String json, TypeReference<T> type) {
        if (null == json || json.isEmpty()) {
            return null;
        }
        return OBJECT_MAPPER.readValue(json, type);
    }

    @SneakyThrows
    public static JsonNode parseJsonTree(String json) {
        if (null == json || json.isEmpty()) {
            return null;
        }
        return OBJECT_MAPPER.readTree(json);
    }
}
