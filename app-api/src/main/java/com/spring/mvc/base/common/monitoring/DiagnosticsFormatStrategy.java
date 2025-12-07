package com.spring.mvc.base.common.monitoring;

public interface DiagnosticsFormatStrategy {

    /**
     * 포맷 타입 식별자 (예: "text", "html").
     */
    String getType();

    /**
     * 주어진 진단 컨텍스트를 원하는 포맷의 문자열로 변환한다.
     */
    String format(DiagnosticsContext context);
}

