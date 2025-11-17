package com.example.week7.common.response;

import lombok.Getter;

@Getter
public class APIResponse<T> {
    private String message;
    private T data;

    public APIResponse(String message, T data) {
        this.message = message;
        this.data = data;
    }

    public static <T> APIResponse<T> success(String message, T data) {
        return new APIResponse<>(message, data);
    }

    public static <T> APIResponse<T> fail(String message) {
        return new APIResponse<>(message, null);
    }
}
