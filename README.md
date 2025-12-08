# Spring MVC Base – Business API 템플릿

이 프로젝트는 **Spring MVC 기반 비즈니스 API 서버 템플릿**입니다.  
새 서비스를 만들 때 바로 포크/템플릿으로 사용하여, 인증·예외 처리·문서화·모니터링 등 공통 기능 위에 비즈니스 코드만 올리도록 설계되어 있습니다.

## 기술 스택

- Java 21, Gradle
- Spring Boot (Web, Validation, Data JPA, Security, OAuth2 Client)
- MySQL + HikariCP
- Redis (Lettuce)
- Springdoc OpenAPI (Swagger UI)
- Querydsl

## 주요 기능 요약

- **인증/인가 & 보안**
  - 폼 로그인, JWT 기반 인증, OAuth2 로그인(Google 등)
  - 커스텀 Security 필터 체인, AccessDenied/AuthenticationEntryPoint 구현
  - refresh token + blacklist(Redis) 기반 로그아웃/차단 처리
- **게시글/댓글/회원 베이스 도메인**
  - Post / Comment / Member 기본 CRUD 및 좋아요, 조회수 정책
- **파일 업로드**
  - 파일 메타데이터 저장 및 Cloudinary 기반 이미지 업로드(옵션)
- **캐시 & Redis 활용**
  - ViewCountPolicy(조회수 중복 방지, Redis) 등 정책 예시
  - 별도 문서로 Caffeine/Spring Cache 연동 가이드 제공
- **예외 처리 & 응답 규격**
  - 글로벌 예외 핸들러 + 공통 ErrorResponse / ApiResponse 구조
  - ErrorCode 기반 도메인 예외 설계
- **AOP 기반 공통 관심사**
  - API 로그, 예외 로깅, 서비스 성능 측정, 트랜잭션 쿼리 로그
- **운영 관점 진단/모니터링**
  - 애플리케이션 시작 시 JVM/스레드/GC/DB/Redis 상태 출력
  - `/api/v1/diagnostics` JSON/HTML 진단 API

## 패키지 구조 개요

- `app-api/src/main/java/com/spring/mvc/base`
  - `application`  
    - `post`, `comment`, `member`, `file` 등 애플리케이션 서비스/컨트롤러
    - `security` : AuthController, 필터, handler, JWT 유틸, OAuth2 서비스
    - `common` : 공통 DTO/상수/컨트롤러
  - `domain`  
    - 엔티티, 리포지토리, 도메인 정책(예: OwnershipPolicy, ViewCountPolicy)
  - `infra`  
    - `redis` : RedisConfig, RedisService/Impl  
    - `image` : Cloudinary 연동, ImageStorageService/NoOp 구현
  - `common`  
    - `exception` : BusinessException, ErrorCode, 도메인별 ErrorCode Enum, GlobalExceptionHandler  
    - `aop` : ApiLoggingAspect, ExceptionLoggingAspect, ServicePerformanceAspect, TransactionalQueryLoggingAspect  
    - `swagger` : SwaggerConfig, *ApiDocs 인터페이스 규약  
    - `monitoring` : StartupDiagnosticsLogger, DiagnosticsCollector/Formatter

## 예외 처리 & 응답 규격

- `BusinessException` + `ErrorCode` 기반 도메인 예외
- `GlobalExceptionHandler`에서:
  - BusinessException → ErrorCode의 HttpStatus/메시지로 매핑
  - DTO 검증 실패(MethodArgumentNotValidException, BindException) → `VALIDATION_FAILED` + 필드 오류 리스트
  - JWT 만료/서명 오류 → 각각 TOKEN_EXPIRED / TOKEN_INVALID
  - 처리되지 않은 예외 → 공통 500 응답(`"서버 내부 오류가 발생했습니다"`)
- 모든 API 응답은 `ApiResponse` / `ErrorResponse` 형태로 통일

자세한 가이드:
- 예외 처리: `docs/EXCEPTION_HANDLING_GUIDE.md`

## 보안 / 인증 구조

- Spring Security + JWT + OAuth2 클라이언트
  - `application/security/config` : SecurityConfig, PasswordEncoderConfig, CorsConfig 등
  - `application/security/filter` : 로그인/로그아웃 필터, JWT 필터
  - `application/security/handler` : 인증 실패/성공, 인가 실패 handler
  - `application/security/service` : SignupService, LoginService, OAuthLoginService, TokenRefreshService, TokenBlacklistService
- JWT/쿠키/리다이렉트 등은 별도 유틸 클래스로 분리
- JWT/보안 설정 값은 `JwtProperties`, `CorsProperties`로 구조화 + Bean Validation 통해 필수값 검증

## Redis & 캐시

- Redis 설정: `infra/redis/config/RedisConfig`, `RedisProperties`
- 간단한 Key-Value 어댑터: `RedisService`, `RedisServiceImpl`
- 사용 예:
  - `TokenBlacklistService` : refresh token 블랙리스트 저장
  - `ViewCountPolicy` : 게시글 조회수 중복 방지 (IP/멤버/UA 기반 키)
- 캐시 학습용 문서:
  - `docs/CAFFEINE_CACHE_GUIDE.md`
  - `docs/SPRING_CACHE_AOP_GUIDE.md`

## 파일 업로드 & 이미지 스토리지

- `application/file`  
  - `FileController` : presign, 업로드 완료, 파일 조회/삭제 API  
  - `FileService` : 파일 엔티티 생성/조회/삭제 및 Cloudinary 업로드 시그니처 발급
- `infra/image`  
  - `ImageStorageService` : 추상화 인터페이스  
  - `CloudinaryImageStorageService` : Cloudinary 구현 (`storage.cloudinary.enabled=true`일 때 활성)  
  - `NoOpImageStorageService` : 설정이 없을 때 업로드 비활성화 + 명확한 에러 응답

## AOP 기반 공통 기능

- `ApiLoggingAspect` : API 요청/응답 로깅
- `ExceptionLoggingAspect` : 예외 로깅 + (향후) 외부 알림 연동 훅
- `ServicePerformanceAspect` : 서비스 메서드 실행 시간 측정
- `TransactionalQueryLoggingAspect` : 트랜잭션 내 쿼리 로깅

모든 AOP는 `aop.*.enabled` 설정으로 On/Off 가능하도록 설계되어 있습니다.

## 진단 / 모니터링

- `StartupDiagnosticsLogger`  
  - 애플리케이션 시작 시:
    - 활성 프로필
    - JVM/힙 메모리, CPU 코어
    - 스레드 수
    - GC 정보
    - HikariCP 풀 설정
    - Redis 설정
  를 콘솔에 콘솔 UI 형태로 출력
- `DiagnosticsCollector` / `DiagnosticsFormatter`  
  - 동일 정보를 런타임에 수집하고 text/html로 포맷팅하는 전략 패턴 구조
- `DiagnosticsController`  
  - `GET /api/v1/diagnostics` : JSON 진단 정보  
  - `GET /api/v1/diagnostics/html` : 브라우저에서 보기 좋은 HTML 대시보드

## Swagger / API 문서화

- Springdoc OpenAPI 기반 Swagger UI 제공
- 컨트롤러와 분리된 `*ApiDocs` 인터페이스로 문서/계약 관리 (`docs/convention/swagger` 참조)
  - Swagger 어노테이션은 `*ApiDocs` 인터페이스에만 위치
  - 실제 컨트롤러는 HTTP 매핑에만 집중

## 설정 전략 (prod / local)

- `application.yml` : local 기본값 (MySQL/Redis/JWT/Tomcat/Hikari)
- `application-dev.yml` : dev용 DB 설정
- `application-prod.yml` : prod용 필수 설정
  - DB/Redis/JWT 등은 `${ENV:?message}`로 **없으면 부팅 실패(fail-fast)**  
  - Hikari/Tomcat 쓰레드는 CPU 기준 권장 비율을 기본값으로 제공
- Cloudinary, Webhook 등 중요도가 낮은 모듈은:
  - 설정 없으면 비활성화 + 경고 로그
  - 애플리케이션 전체는 정상 기동

자세한 설정/예외 정책은 `docs/EXCEPTION_HANDLING_GUIDE.md`의  
“환경 설정 누락/옵션 설정에 대한 정책” 섹션을 참고하면 됩니다.

---

이 템플릿은 **“실무에서 바로 쓸 수 있는 기본기”**에 집중합니다.  
새 프로젝트를 시작할 때, 이 베이스 위에 도메인 모듈만 추가해도  
보안·예외 처리·모니터링·문서화가 어느 정도 갖춰진 상태로 출발할 수 있습니다.

