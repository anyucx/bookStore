package com.bookstore.common;

/** 统一返回结构。 */
public class ApiResponse<T> {
    public boolean success;
    public int code;
    public String message;
    public T data;
    public long timestamp = System.currentTimeMillis();

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> r = new ApiResponse<T>();
        r.success = true;
        r.code = 0;
        r.message = "ok";
        r.data = data;
        return r;
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> r = success(data);
        r.message = message;
        return r;
    }

    public static <T> ApiResponse<T> fail(int code, String message) {
        ApiResponse<T> r = new ApiResponse<T>();
        r.success = false;
        r.code = code;
        r.message = message;
        return r;
    }
}
