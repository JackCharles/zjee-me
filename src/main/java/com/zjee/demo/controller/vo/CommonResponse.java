package com.zjee.demo.controller.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
public class CommonResponse {
    private int code;
    private String msg;
    private Map<String, ?> data;

    public CommonResponse(){}

    public CommonResponse(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
