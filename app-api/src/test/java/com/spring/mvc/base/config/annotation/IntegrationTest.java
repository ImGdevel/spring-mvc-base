package com.spring.mvc.base.config.annotation;

import com.spring.mvc.base.config.ImageStorageMockConfig;
import com.spring.mvc.base.config.JpaAuditingTestConfig;
import com.spring.mvc.base.config.RedisMockConfig;
import com.spring.mvc.base.config.TestSecurityConfig;
import com.spring.mvc.base.infra.redis.config.RedisConfig;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * 통합 테스트 공통 설정을 모아둔 메타 어노테이션입니다.
 * - @ActiveProfiles(\"test\") : 테스트 프로필 활성화
 * - @SpringBootTest + @AutoConfigureMockMvc : 전체 컨텍스트 + MockMvc 구동
 * - @Import(TestSecurityConfig, JpaAuditingTestConfig, ImageStorageMockConfig, RedisMockConfig) :
 *   보안, JPA Auditing, 이미지 스토리지, 캐시 스토리지에 대한 테스트 전용 설정 주입
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ActiveProfiles("test")
@Import({
        TestSecurityConfig.class,
        JpaAuditingTestConfig.class,
        ImageStorageMockConfig.class,
        RedisMockConfig.class
})
@SpringBootTest
@AutoConfigureMockMvc
@Tag("integration")
public @interface IntegrationTest {
}
