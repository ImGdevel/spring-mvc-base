package com.spring.mvc.base.application.security.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.spring.mvc.base.config.RedisMockConfig;
import com.spring.mvc.base.domain.member.entity.oauth.OAuthMember;
import com.spring.mvc.base.domain.member.repository.MemberRepository;
import com.spring.mvc.base.domain.member.repository.OAuthMemberRepository;
import com.spring.mvc.base.fake.FakeOAuth2Provider;
import com.spring.mvc.base.common.utils.SerializationUtil;
import com.spring.mvc.base.application.security.constants.CookieConstants;
import jakarta.servlet.http.Cookie;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(RedisMockConfig.class)
@DisplayName("OAuth2 로그인 통합 테스트")
class OAuthLoginIntegrationTest {

    private static final FakeOAuth2Provider fakeGoogle = FakeOAuth2Provider.google();

    @DynamicPropertySource
    static void registerOauthProvider(DynamicPropertyRegistry registry) {
        fakeGoogle.start();
        fakeGoogle.register(registry);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OAuthMemberRepository oAuthMemberRepository;

    @Autowired
    private MemberRepository memberRepository;

    @AfterEach
    void cleanup() {
        oAuthMemberRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @AfterAll
    static void shutdownFakeProvider() throws IOException {
        fakeGoogle.close();
    }

    @Test
    @DisplayName("OAuth2 구글 로그인 흐름: 사용자·OAuth 연동 정보 생성 및 리프레시 쿠키 발급")
    void oauth2Login_successful() throws Exception {
        MvcResult authorizationResult = mockMvc.perform(MockMvcRequestBuilders.get("/oauth2/authorization/google"))
                .andReturn();

        String authorizationLocation = authorizationResult.getResponse().getHeader(HttpHeaders.LOCATION);
        Cookie[] authCookies = Optional.ofNullable(authorizationResult.getResponse().getCookies()).orElse(new Cookie[0]);
        String storedState = Arrays.stream(authCookies)
                .filter(cookie -> CookieConstants.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME.equals(cookie.getName()))
                .findFirst()
                .map(cookie -> SerializationUtil.deserialize(cookie.getValue(), OAuth2AuthorizationRequest.class).getState())
                .orElseThrow(() -> new IllegalStateException("OAuth2 상태 쿠키를 찾을 수 없습니다."));

        assertThat(authorizationLocation).isNotBlank();

        OkHttpClient client = new OkHttpClient.Builder()
                .followRedirects(false)
                .build();

        Request providerRequest = new Request.Builder()
                .url(authorizationLocation)
                .get()
                .build();

        try (Response providerResponse = client.newCall(providerRequest).execute()) {
            assertThat(providerResponse.code()).isEqualTo(302);

            String callbackLocation = providerResponse.header(HttpHeaders.LOCATION);
            UriComponents callbackUri = UriComponentsBuilder.fromUriString(callbackLocation).build(true);
            String code = callbackUri.getQueryParams().getFirst("code");

            assertThat(code).isNotBlank();

            MvcResult callbackResult = mockMvc.perform(MockMvcRequestBuilders.get("/login/oauth2/code/google")
                            .param("code", code)
                            .param("state", storedState)
                            .cookie(authCookies))
                    .andReturn();

            assertThat(callbackResult.getResponse().getStatus()).isEqualTo(302);
            assertThat(callbackResult.getResponse().getHeader(HttpHeaders.LOCATION))
                    .isEqualTo("http://localhost:3000/oauth/callback");
            assertThat(callbackResult.getResponse().getHeaders(HttpHeaders.SET_COOKIE))
                    .anyMatch(header -> header.contains("refreshToken="));
        }

        assertThat(memberRepository.count()).isOne();

        Optional<OAuthMember> oauthMember = oAuthMemberRepository.findByProviderAndProviderId("google", "fake-google-sub");
        assertThat(oauthMember).isPresent();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(oauthMember.get().getProvider()).isEqualTo("google");
            softly.assertThat(oauthMember.get().getProviderId()).isEqualTo("fake-google-sub");
            softly.assertThat(oauthMember.get().getMember().getEmail()).isEqualTo("google-user@example.com");
        });
    }
}
