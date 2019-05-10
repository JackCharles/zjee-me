package com.zjee.demo.controller.vo;

import lombok.Data;

import java.util.Map;

@Data
public class CommonResponse {
    private int code;
    private String msg;
    private Object data;

    public CommonResponse(){}

    public CommonResponse(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
