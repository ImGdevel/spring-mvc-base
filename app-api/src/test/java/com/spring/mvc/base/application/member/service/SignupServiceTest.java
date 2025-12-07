package com.spring.mvc.base.application.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.spring.mvc.base.application.auth.SignupRequestFixture;
import com.spring.mvc.base.application.member.dto.request.SignupRequest;
import com.spring.mvc.base.application.member.validator.AuthValidator;
import com.spring.mvc.base.application.security.dto.response.LoginResponse;
import com.spring.mvc.base.application.security.service.SignupService;
import com.spring.mvc.base.application.security.util.JwtTokenProvider;
import com.spring.mvc.base.config.annotation.UnitTest;
import com.spring.mvc.base.domain.member.MemberFixture;
import com.spring.mvc.base.domain.member.entity.Member;
import com.spring.mvc.base.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;

@UnitTest
class SignupServiceTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private AuthValidator authValidator;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private SignupService signupService;



    @Test
    @DisplayName("회원가입 시 요청을 검증하고 패스워드를 인코딩한 뒤 저장한다")
    void signup_createsMember() {

        //given
        SignupRequest request = SignupRequestFixture.createRequest();

        given(passwordEncoder.encode(SignupRequestFixture.DEFAULT_PASSWORD)).willReturn("encodedPassword");
        given(memberRepository.save(any(Member.class))).willReturn(MemberFixture.createWithId(1L));
        given(jwtTokenProvider.generateAccessToken(any(), any())).willReturn("fake-token");

        LoginResponse response = signupService.signup(request);

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.accessToken()).isEqualTo("fake-token");
    }
}
