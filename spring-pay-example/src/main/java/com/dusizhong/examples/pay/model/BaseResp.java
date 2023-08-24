package com.dusizhong.examples.pay.model;

import lombok.Data;

@Data
public class BaseResp {

    private Integer code;
    private String msg;
    private Object data;

    public BaseResp(Integer code, String message) {
        this.code = code;
        this.msg = message;
    }

    public BaseResp(Integer code, String message, Object data) {
        this.code = code;
        this.msg = message;
        this.data = data;
    }

    public static BaseResp success() {
        return new BaseResp(200, "成功");
    }

    public static BaseResp success(Object data) {
        return new BaseResp(200, "成功", data);
    }

    public static BaseResp error(String message) {
        return new BaseResp(400, message);
    }
}