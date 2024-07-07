package com.zjee.controller;

import com.zjee.controller.vo.CommonResponse;
import com.zjee.constant.ResponseStatus;
import com.zjee.service.MottoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("api")
public class MottoController {

    @Autowired
    private MottoService mottoService;

    private final Map<String, String> typeMap;

    public MottoController() {
        typeMap = new HashMap<>();
        typeMap.put("a", "Anime - 动画");
        typeMap.put("b", "Comic – 漫画");
        typeMap.put("c", "Game – 游戏");
        typeMap.put("d", "Novel – 小说");
        typeMap.put("e", "Myself – 原创");
        typeMap.put("f", "Internet – 来自网络");
        typeMap.put("g", "Other – 其他");
    }

    @RequestMapping("motto")
    public CommonResponse getMotto(@RequestParam(required = false) String type) {
        CommonResponse response = new CommonResponse();
        if (null == type || typeMap.containsKey(type)) {
            response.setCode(ResponseStatus.SUCCESS_CODE);
            response.setMsg("OK");
            response.setData(mottoService.getRandomMotto(type));
        } else {
            response.setCode(ResponseStatus.ERROR_CODE);
            response.setMsg("invalid type, please reference flow info.");
            response.setData(typeMap);
        }
        return response;
    }
}
