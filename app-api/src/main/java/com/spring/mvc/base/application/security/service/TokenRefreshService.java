package com.spring.mvc.base.application.security.service;

import com.spring.mvc.base.application.security.util.JwtTokenProvider;
import com.spring.mvc.base.common.exception.CustomException;
import com.spring.mvc.base.common.exception.code.AuthErrorCode;
import com.spring.mvc.base.common.exception.code.MemberErrorCode;
import com.spring.mvc.base.domain.member.entity.Member;
import com.spring.mvc.base.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TokenRefreshService {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final TokenBlacklistService tokenBlacklistService;

    public String refreshAccessToken(String refreshToken) {
        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new CustomException(AuthErrorCode.REFRESH_TOKEN_INVALID);
        }

        if (jwtTokenProvider.isTokenExpired(refreshToken)) {
            throw new CustomException(AuthErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        if (tokenBlacklistService.isBlacklisted(refreshToken)) {
            throw new CustomException(AuthErrorCode.REFRESH_TOKEN_INVALID);
        }

        Long memberId = jwtTokenProvider.getUidFromToken(refreshToken);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.USER_NOT_FOUND));

        if (!member.isActive()) {
            throw new CustomException(MemberErrorCode.MEMBER_INACTIVE);
        }

        return jwtTokenProvider.generateAccessToken(memberId, member.getRole().name());
    }
}
