package com.ynu.edu.elm_intermediate.common;

public class AppException extends RuntimeException {

    private final int code;
    private final int httpStatus;

    public AppException(int code, String message) {

        this(code, message, 400);
    }

    public AppException(int code, String message, int httpStatus) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public int getCode() { return code; }
    public int getHttpStatus() { return httpStatus; }
}
