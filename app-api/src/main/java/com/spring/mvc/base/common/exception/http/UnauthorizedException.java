package com.spring.mvc.base.common.exception.http;

import com.spring.mvc.base.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class UnauthorizedException extends HttpException {

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
