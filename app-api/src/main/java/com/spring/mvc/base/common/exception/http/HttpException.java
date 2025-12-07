package com.spring.mvc.base.common.exception.http;

import com.spring.mvc.base.common.exception.CustomException;
import com.spring.mvc.base.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * HTTP 상태 코드를 명시적으로 표현하는 CustomException 확장 베이스 클래스.
 * 각 하위 예외는 고정된 HttpStatus를 반환한다.
 */
public abstract class HttpException extends CustomException {

    protected HttpException(ErrorCode errorCode) {
        super(errorCode);
    }

    protected HttpException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

}
