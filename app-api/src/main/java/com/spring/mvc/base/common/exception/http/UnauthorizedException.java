package com.spring.mvc.base.common.exception.http;

import com.spring.mvc.base.common.exception.CustomException;
import com.spring.mvc.base.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class UnauthorizedException extends CustomException {

    public UnauthorizedException(ErrorCode errorCode) {
        super(errorCode);
    }

    public UnauthorizedException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.UNAUTHORIZED;
    }
}
