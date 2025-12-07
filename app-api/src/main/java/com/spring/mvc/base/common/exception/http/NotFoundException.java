package com.spring.mvc.base.common.exception.http;

import com.spring.mvc.base.common.exception.CustomException;
import com.spring.mvc.base.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class NotFoundException extends CustomException {

    public NotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public NotFoundException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
