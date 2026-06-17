package com.studyhub.exception;

import com.studyhub.common.ErrorCode;

public class UnauthorizedException extends RuntimeException {

    private final Integer code;

    public UnauthorizedException() {
        super(ErrorCode.UNAUTHORIZED.getMessage());
        this.code = ErrorCode.UNAUTHORIZED.getCode();
    }

    public UnauthorizedException(String message) {
        super(message);
        this.code = ErrorCode.UNAUTHORIZED.getCode();
    }

    public Integer getCode() {
        return code;
    }
}