package com.spring.mvc.base.common.exception.http;

import com.spring.mvc.base.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class ConflictException extends HttpException {

    public ConflictException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ConflictException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.CONFLICT;
    }
}
