package com.spring.mvc.base.application.security.service;

import com.spring.mvc.base.application.member.dto.request.SignupRequest;
import com.spring.mvc.base.application.member.validator.AuthValidator;
import com.spring.mvc.base.application.security.dto.response.LoginResponse;
import com.spring.mvc.base.application.security.util.JwtTokenProvider;
import com.spring.mvc.base.domain.member.entity.Member;
import com.spring.mvc.base.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SignupService {
    private final MemberRepository memberRepository;
    private final AuthValidator authValidator;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public LoginResponse signup(SignupRequest request){
        authValidator.validateSignup(request);

        String email = request.email();
        String password = passwordEncoder.encode(request.password());
        String nickname = request.nickname();

        Member member = Member.create(email, password, nickname);
        member.updateProfileImage(request.profileImage());

        Member savedMember = memberRepository.save(member);

        String accessToken = jwtTokenProvider.generateAccessToken(savedMember.getId(), savedMember.getRole().name());
        return new LoginResponse(savedMember.getId(), accessToken);
    }
}
