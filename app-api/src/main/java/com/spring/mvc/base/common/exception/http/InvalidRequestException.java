package com.spring.mvc.base.common.exception.http;

import com.spring.mvc.base.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class InvalidRequestException extends HttpException {

    public InvalidRequestException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidRequestException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
