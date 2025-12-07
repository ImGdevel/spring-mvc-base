package com.spring.mvc.base.application.member.validator;

import com.spring.mvc.base.application.member.dto.request.SignupRequest;
import com.spring.mvc.base.common.exception.CustomException;
import com.spring.mvc.base.common.exception.code.MemberErrorCode;
import com.spring.mvc.base.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthValidator {

    private final MemberRepository memberRepository;

    /**
     * 회원가입 요청 검증
     */
    public void validateSignup(SignupRequest request) {
        validateEmailNotDuplicated(request.email());
        validateNicknameNotDuplicated(request.nickname());
    }

    private void validateEmailNotDuplicated(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new CustomException(MemberErrorCode.DUPLICATE_EMAIL);
        }
    }

    private void validateNicknameNotDuplicated(String nickname) {
        if (memberRepository.existsByNickname(nickname)) {
            throw new CustomException(MemberErrorCode.DUPLICATE_NICKNAME);
        }
    }
}
