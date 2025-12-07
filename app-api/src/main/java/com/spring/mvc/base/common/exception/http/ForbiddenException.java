package com.spring.mvc.base.common.exception.http;

import com.spring.mvc.base.common.exception.CustomException;
import com.spring.mvc.base.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class ForbiddenException extends CustomException {

    public ForbiddenException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ForbiddenException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.FORBIDDEN;
    }
}
