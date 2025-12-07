package com.spring.mvc.base.application.security.handler;


import com.spring.mvc.base.application.security.service.TokenBlacklistService;
import com.spring.mvc.base.application.security.util.CookieProvider;
import com.spring.mvc.base.common.dto.api.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LogoutHandler {

    private final ObjectMapper objectMapper;
    private final CookieProvider cookieProvider;
    private final TokenBlacklistService tokenBlacklistService;

    public void onLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {

        SecurityContextHolder.clearContext();

        cookieProvider.getRefreshTokenFromCookie(request)
                .ifPresent(tokenBlacklistService::addToBlacklist);

        cookieProvider.deleteRefreshTokenCookie(response);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ApiResponse<Object> result = ApiResponse.success("로그아웃되었습니다.");
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }


}
