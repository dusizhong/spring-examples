package com.dusizhong.examples.log.model;

import lombok.Data;

@Data
public class BaseResp {

    private Integer code;
    private String msg;
    private Object data;

    public BaseResp(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public BaseResp(Integer code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static BaseResp success() {
        return new BaseResp(200, "成功");
    }

    public static BaseResp success(Object data) {
        return new BaseResp(200, "成功", data);
    }

    public static BaseResp error(String msg) {
        return new BaseResp(400, msg);
    }
}

