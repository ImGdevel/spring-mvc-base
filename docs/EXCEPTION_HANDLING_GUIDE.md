# 예외 처리 체크리스트 및 권장 방안

## 1. 예외 분류·매핑 정책

### 체크리스트

- [ ] `IllegalArgumentException` / `Assert` 계열(엔티티 내부에서 발생) 처리 방식을 정의했는가?
- [ ] JPA/DB 예외(`DataIntegrityViolationException`, `ConstraintViolationException`, `OptimisticLockException` 등)를 어떤 HTTP 코드로 매핑할지 정했는가?
- [ ] 외부 시스템/인프라 예외(예: Redis, Cloudinary, 외부 API 호출 실패)를 5xx 계열 중 어떤 코드로 내려줄지 정했는가?

### 권장 방안

- **엔티티 내부의 Assert/IllegalArgumentException**
  - 도메인 불변식 위반 = 개발자/비즈니스 로직 버그로 간주한다.
  - 별도 핸들러 없이 `@ExceptionHandler(Exception.class)`에 흡수되어 **500**을 반환하도록 유지한다.
  - 필요하다면 `ExceptionLoggingAspect`에서 스택트레이스 + 알림(webhook 등)으로 빠르게 인지한다.
- **컨트롤러/서비스 레벨의 IllegalArgumentException**
  - 클라이언트 입력 오류 성격이라면, `@ExceptionHandler(IllegalArgumentException.class)`를 추가하여 **400 Bad Request**로 매핑하는 것을 고려한다.
- **JPA/DB 예외**
  - 유니크 제약조건 위반 등 비즈니스적으로 예상 가능한 무결성 오류는 가능하면 `BusinessException` + `ErrorCode`로 감싸서 도메인 예외로 승격한다.
  - 그렇지 않은 로우 레벨 DB 예외는 기본적으로 500으로 두되, 필요 시:
    - `DataIntegrityViolationException` → 409(Conflict)
    - `OptimisticLockException` → 409(Conflict)
    로 매핑하는 전용 핸들러를 추가할 수 있다.
- **외부 시스템 예외**
  - 외부 API/인프라 장애는 클라이언트 입장에서 “일시적인 서비스 장애”에 가까우므로 503(Service Unavailable) 매핑을 검토한다.
  - 재시도 가능 여부, fallback 정책 등과 함께 정의하면 좋다.

---

## 2. Web 계층(Spring MVC / Security) 예외

### 체크리스트

- [ ] 스프링 기본 예외(`HttpMessageNotReadableException`, `MissingServletRequestParameterException`, `MethodArgumentTypeMismatchException` 등)를 명시적으로 처리하고 있는가?
- [ ] Security 핸들러(`CustomAuthenticationEntryPoint`, `CustomAccessDeniedHandler`, 로그인/로그아웃 핸들러)의 응답 구조가 `ErrorResponse` / `ApiResponse`와 완전히 일관적인가?
- [ ] JWT 예외(`ExpiredJwtException`, `JwtException`) 응답과 Security 예외 응답이 UX 관점에서 혼동되지 않는가?

### 권장 방안

- **스프링 기본 예외 처리**
  - 다음 예외들에 대해 400으로 매핑하는 핸들러를 추가하는 것을 권장:
    - `HttpMessageNotReadableException` (JSON 파싱 실패)
    - `MissingServletRequestParameterException`
    - `MethodArgumentTypeMismatchException`
  - 공통 메시지 예: `"invalid_request"` 또는 `"invalid_request_format"` 등.
- **Security 응답 정합성**
  - 현재 사용 중인 `SecurityResponseSender`를 통해 Security 관련 JSON 응답을 모두 일관된 형태로 유지한다.
  - 가능하면 `ErrorResponse` / `ApiResponse` 구조와 필드(`success`, `message`, `code`, `errors`)를 맞추고, 에러 코드(`code`)도 도입할 수 있으면 도입한다.
- **JWT vs Security 예외 메시지**
  - JWT 핸들러:
    - 만료: `"토큰이 만료되었습니다"` (401)
    - 기타: `"유효하지 않은 토큰입니다"` (401)
  - Security 핸들러:
    - 비인증: `"인증이 필요합니다"` (401)
    - 인가 실패: `"접근 권한이 없습니다"` (403)
  - 이처럼 “상황별로 한 번에 이해되는 한글 메시지”를 유지하되, 필요하다면 클라이언트 분기용 `code`도 추가(예: `token_expired`, `token_invalid`, `auth_required`, `access_denied`).

---

## 3. 도메인/비즈니스 예외 설계

### 체크리스트

- [ ] 실제 도메인 시나리오에 대해 `ErrorCode`가 충분히 정의되어 있는가? (누락된 케이스는 없는가?)
- [ ] `ErrorCode`에 설정된 `HttpStatus` 값이 일관적인가? (권한 계열은 항상 403, 리소스 없음은 항상 404 등)
- [ ] 도메인 정책에서 “예외를 던질 것인지, 실패 응답을 리턴할 것인지”에 대한 기준이 있는가?

### 권장 방안

- **ErrorCode 커버리지**
  - 도메인 별로 최소한 다음 카테고리의 코드들을 갖추는 것을 권장:
    - Validation 계열 (이미 `validation_failed`로 처리 중)
    - Not found 계열 (예: `POST_NOT_FOUND`, `COMMENT_NOT_FOUND`)
    - Permission/Ownership 계열 (`NO_PERMISSION`, `ACCESS_DENIED`)
    - Conflict/상태 불일치 (`ALREADY_LIKED`, `LIKE_NOT_FOUND` 등)
  - 새로운 비즈니스 요구가 나올 때마다 “우선 ErrorCode부터 추가”하는 습관을 들이면 정합성이 유지된다.
- **HttpStatus 일관성**
  - ErrorCode 정의 시 상태코드를 명시하고, 동일 유형 에러는 항상 같은 상태코드로 내려가도록 한다.
  - 예: 소유권/권한 문제는 무조건 403, 존재하지 않는 리소스는 무조건 404.
- **예외 vs 실패 응답 기준**
  - 예외:
    - “정상 흐름에서 예외적인 상황” (ex. 없는 리소스, 권한 없음, 도메인 규칙 위반) → `BusinessException`.
  - 실패 응답(예외 아님):
    - 단순 조회 API에서 “결과 없음”을 정상 사양으로 삼고 싶은 경우 (예: 빈 리스트 반환) → 예외 대신 빈 값 반환.
  - 이 기준을 문서로 남겨두면 팀 내에서 일관되게 사용할 수 있다.

---

## 4. 로깅 / 모니터링 / 알림

### 체크리스트

- [ ] 예외 로그에 Correlation ID / Request ID가 포함되어 있는가?
- [ ] 예외/로그에 민감 정보(PII, 토큰, 비밀번호)가 포함되지 않도록 주의하고 있는가?
- [ ] 어떤 예외는 알림(슬랙/이메일/webhook)을 보내고, 어떤 예외는 로그만 남길지 기준이 정의되어 있는가?
- [ ] dev/local vs prod에서 스택트레이스 출력 정책이 분리되어 있는가?

### 권장 방안

- **Request ID / Correlation ID**
  - 현재 `ApiLoggingAspect`에서 ID를 생성하여 로그에 남기고 있으므로,
  - 필요하다면 에러 응답 바디에도 `requestId` 필드를 추가해 “이 ID로 문의하세요” 패턴을 사용할 수 있게 한다.
- **민감 정보 보호**
  - 예외 메시지/스택트레이스에 JWT, 비밀번호, 토큰, 개인 정보가 포함되지 않도록 주의한다.
  - 토큰 값 전체가 아닌 prefix만 로깅하는 등의 전략을 사용할 수 있다.
- **알림 기준**
  - `ExceptionLoggingAspect`에서:
    - `BusinessException` → WARN 로그만 (알림 X)
    - 그 외 `Exception`(사실상 500) → ERROR 로그 + 알림 후보
  - 알림 실제 구현 시:
    - 동일한 예외가 대량 발생하는 경우 rate limiting(샘플링) 정책을 함께 고려한다.
- **환경별 스택트레이스 정책**
  - dev/local: `includeStackTrace = true` → 로그/알림에 스택트레이스 포함.
  - prod: `includeStackTrace = false` → 로그에는 최소화된 스택트레이스 또는 요약, 클라이언트 응답에는 절대 노출 X.
  - 현재 `ExceptionLoggingAspect`의 `Environment`/프로퍼티 기반 플래그를 통해 이 정책을 반영하고, `application-prod.yml`에서 관련 설정을 명시적으로 관리하는 것을 권장한다.

---

## 5. API 계약 / Swagger 문서 정합성

### 체크리스트

- [ ] 모든 에러 응답이 `ErrorResponse` 또는 `ApiResponse` 같은 통일된 구조를 사용하고 있는가?
- [ ] Swagger에서 정의한 에러 응답 스펙(`@CustomExceptionDescription`, `SwaggerResponseDescription`)과 실제 응답이 일치하는가?
- [ ] 클라이언트(프론트엔드)가 분기할 때 사용할 에러 코드(`code`)가 충분히 구분 가능하고 안정적인가?

### 권장 방안

- **공통 에러 응답 스펙**
  - 에러 응답은 원칙적으로 `ErrorResponse` 한 가지 형태로 고정하는 것을 권장:
    - `success: false`
    - `code: string (optional)`
    - `message: string`
    - `errors: List<FieldError> (optional)`
  - Security 쪽도 가능하다면 같은 구조를 따르게 조정한다.
- **Swagger 정합성**
  - `SwaggerResponseDescription` / `CustomExceptionDescription`에서 상태코드/응답 구조를 정의할 때,
    - 실제 `GlobalExceptionHandler` / Security 핸들러가 내려주는 값과 맞추어 작성한다.
  - 자주 쓰이는 공통 에러(401, 403, 404, 500 등)에 대해 “공용 응답 스펙”을 정의해 재사용한다.
- **클라이언트용 에러 코드**
  - 프론트가 분기해야 하는 에러(예: 토큰 만료, 권한 없음, 특정 도메인 에러 등)에 대해서는 `code`를 반드시 채운다.
  - `code`는 사람 친화적인 메시지(`message`)와 분리해서, 영어 대문자/스네이크케이스 등 일관된 네이밍 규칙을 유지한다.
  - 서버 내부에서 ErrorCode enum 이름과 동일하게 가져가면 관리가 편해진다.

---

## 6. 테스트 / 운영 관점

### 체크리스트

- [ ] `GlobalExceptionHandler`에 대해 대표 케이스를 검증하는 테스트가 있는가? (BusinessException, validation, JWT, 기타 Exception)
- [ ] 서버 에러 발생 시에도 CORS, JSON 포맷 등이 깨지지 않고 그대로 유지되는가?
- [ ] 비동기 처리(@Async)나 스케줄러에서 발생하는 예외는 어떻게 처리/로깅되는지 정의되어 있는가?

### 권장 방안

- **핵심 예외 흐름 테스트**
  - 다음 케이스에 대해 컨트롤러/통합 테스트 또는 핸들러 단위 테스트를 유지한다:
    - 비즈니스 예외 → ErrorCode 기반 4xx 응답
    - DTO 검증 실패 → `"validation_failed"` + 필드 오류 리스트
    - JWT 만료/잘못된 토큰 → 401 + 적절한 메시지
    - 예상치 못한 예외 → 500 + `"서버 내부 오류가 발생했습니다"`
- **에러 응답에서도 공통 인프라 보장**
  - 예외 발생 시에도 CORS 헤더가 적용되는지, JSON 응답으로 내려가는지 한번 점검한다.
- **비동기/스케줄러 예외**
  - @Async, 스케줄러(@Scheduled)에서 발생한 예외는 `GlobalExceptionHandler`로 들어오지 않는다.
  - 필요한 경우:
    - 전역 `AsyncUncaughtExceptionHandler`
    - 스케줄러용 예외 처리 로직
    를 별도로 정의해 로깅/알림을 붙이는 것을 권장한다.

---

## 7. 환경 설정 누락/옵션 설정에 대한 정책

### 체크리스트

- [ ] prod 환경에서 반드시 존재해야 하는 설정(DB, Redis, Security 등)에 대해 “부팅 실패” 정책이 적용되어 있는가?
- [ ] dev/local 환경에서 편의를 위한 기본값과, prod에서 강제되는 필수값이 명확히 분리되어 있는가?
- [ ] 구조화된 설정(`@ConfigurationProperties`) 중 “필수 설정”과 “선택 설정”의 구분 및 검증 정책이 정의되어 있는가?
- [ ] 중요도가 낮은 모듈(Webhook, 알림 등)에 대해, 설정 누락 시의 동작(비활성화/기본값 사용/경고 로그)이 문서화되어 있는가?

### 권장 방안

#### 7.1 프로필별(application.yml vs application-prod.yml) 설정 강제

- **prod에서는 설정이 없으면 애플리케이션이 부팅 실패해야 한다.**
  - 예: `application-prod.yml`에서 placeholder 강제 사용
  - 예시:
    ```yaml
    spring:
      datasource:
        url: ${DB_URL:?DB_URL must be set}
        username: ${DB_USERNAME:?DB_USERNAME must be set}
        password: ${DB_PASSWORD:?DB_PASSWORD must be set}

      data:
        redis:
          host: ${REDIS_HOST:?REDIS_HOST must be set}
          port: ${REDIS_PORT:?REDIS_PORT must be set}
    ```
  - `${ENV:?message}` 문법을 사용하면 해당 값이 없을 때 부팅 시점에 즉시 예외가 발생한다.
  - 이때 `message` 부분을 최대한 친절하게 작성하면, 로그만 보고도 원인을 바로 파악할 수 있다.
    - 예: `${DB_URL:?[prod] DB_URL 환경 변수가 설정되지 않아 서버를 시작할 수 없습니다.}`
- **dev/local에서는 개발 편의를 위해 기본값 허용**
  - 예: `application.yml`에서 기본값 정의
    ```yaml
    spring:
      data:
        redis:
          host: ${REDIS_HOST:localhost}
          port: ${REDIS_PORT:6379}
    ```
  - 개발/로컬 환경에서는 값이 없어도 동작하지만, prod에서는 반드시 채워 넣어야 한다.

#### 7.2 구조화된(중요도 높은) 설정에 대한 검증

- DB, Redis, Security(JWT, CORS 등)처럼 **없으면 애플리케이션이 제대로 동작할 수 없는 설정**은 `@ConfigurationProperties + @Validated`로 관리한다.
- 예시 (JWT 설정):
  ```java
  @Getter
  @Setter
  @Component
  @Validated
  @ConfigurationProperties(prefix = "spring.security.jwt")
  public class JwtProperties {

      @NotBlank
      private String secret;

      @NotNull @Positive
      private Long accessTokenExpiration;

      @NotNull @Positive
      private Long refreshTokenExpiration;
  }
  ```
- 값이 비어 있거나 범위가 잘못된 경우, 컨텍스트 초기화 단계에서 바인딩 예외가 발생하고 서버가 부팅되지 않는다.
- 필요하다면 Bean Validation 애너테이션에 `message`를 지정하거나, 메시지 프로퍼티를 통해 보다 이해하기 쉬운 한글 에러 메시지를 노출할 수 있다.
- 같은 패턴으로 DB/Redis 설정도 `DataSourceProperties`, `RedisProperties` 등으로 묶어 필수값을 검증할 수 있다.

#### 7.3 중요도 낮은 설정(옵션 모듈)에 대한 처리

- Webhook, 알림, 부가 모듈 등 **“있으면 좋은” 수준의 기능**은 다음 원칙을 따른다.
  - 애플리케이션 부팅은 허용하되, 설정 누락 시:
    - 명확한 경고 로그 출력
    - 모듈 비활성화 또는 기본값 사용
- 예: Webhook 모듈
  ```java
  @ConfigurationProperties(prefix = "notify.webhook")
  public class WebhookProperties {
      private boolean enabled = false;
      private String url; // 없어도 됨
  }
  ```

  ```java
  @Configuration
  public class WebhookConfig {

      private static final Logger log = LoggerFactory.getLogger(WebhookConfig.class);

      @Bean
      @ConditionalOnProperty(prefix = "notify.webhook", name = "enabled", havingValue = "true")
      public WebhookClient webhookClient(WebhookProperties props) {
          if (props.getUrl() == null || props.getUrl().isBlank()) {
              log.warn("Webhook 설정이 존재하지 않습니다. Webhook 모듈이 비활성화됩니다.");
              return new NoOpWebhookClient();
          }
          return new RealWebhookClient(props.getUrl());
      }
  }
  ```
- 비동기/배치 모듈의 경우:
  - 설정이 없을 때에는 `log.warn("XX 설정이 없습니다. 기본값 OO를 사용합니다.")` 형태로 경고를 남기고 기본값으로 동작하게 한다.
  - 정말로 설정이 필수라면 7.2의 “부팅 실패” 정책을 따르고, 그렇지 않다면 “경고 + 비활성/기본값” 패턴을 선택한다.

#### 7.4 요약 정책

- **prod + 필수 설정(DB, Redis, Security 등)**  
  - 설정이 없으면 부팅 실패 (placeholder 강제, `@ConfigurationProperties + @Validated`).
- **dev/local + 필수 설정**  
  - 개발 편의를 위해 적당한 기본값 허용, 필요 시 prod와 별도로 관리.
- **선택 설정(Webhook, 알림 등 중요도 낮은 모듈)**  
  - 설정 누락 시 애플리케이션은 실행되며:
    - 모듈 비활성화 또는 안전한 기본값으로 동작
    - 경고/정보 로그로 현재 상태를 명확히 알린다.
