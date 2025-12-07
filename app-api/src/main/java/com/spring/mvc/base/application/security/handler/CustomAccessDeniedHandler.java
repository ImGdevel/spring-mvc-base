package com.spring.mvc.base.application.security.handler;

import com.spring.mvc.base.application.security.util.SecurityResponseSender;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final SecurityResponseSender securityResponseSender;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.error("[AccessDeniedHandler] {}", accessDeniedException.getMessage(), accessDeniedException);

        securityResponseSender.sendError(response, HttpServletResponse.SC_FORBIDDEN, "접근 권한이 없습니다");
    }
}
