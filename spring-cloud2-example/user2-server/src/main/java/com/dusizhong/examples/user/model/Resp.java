package com.dusizhong.examples.user.model;

import lombok.Data;

@Data
public class Resp {

    private Integer code;
    private String message;
    private Object data;

    public Resp(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Resp(Integer code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static Resp success() {
        return new Resp(200, "成功");
    }

    public static Resp success(Object data) {
        return new Resp(200, "成功", data);
    }

    public static Resp error(String message) {
        return new Resp(400, message);
    }
}

