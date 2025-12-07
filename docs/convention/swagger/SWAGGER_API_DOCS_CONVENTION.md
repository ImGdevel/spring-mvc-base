# SWAGGER API DOCS CONVENTION

## 1. 목적
- 컨트롤러 클래스에서 Swagger 관련 어노테이션을 제거하고, 별도의 `*ApiDocs` 인터페이스로 문서 스펙(계약)을 관리한다.
- 문서와 실제 구현이 항상 일치하도록, 인터페이스 시그니처 = 컨트롤러 메서드 시그니처를 유지한다.

## 2. 파일 구조 / 네이밍 컨벤션
- 위치: `.../{domain}/controller/docs`
  - 규칙: `{domain}`은 소문자 패키지명
- Docs 인터페이스 파일명 컨벤션: `{ControllerName}ApiDocs.java`
- 실제 컨트롤러는 `implements {ControllerName}ApiDocs`로 인터페이스를 구현한다.

## 3. 인터페이스 기본 구조
- 인터페이스는 순수한 Swagger 문서/계약 전용이며, Spring MVC 어노테이션은 사용하지 않는다.
  - 파일 경로 패턴: `.../{domain}/controller/docs/{ControllerName}ApiDocs.java`

## 4. 클래스 레벨 어노테이션 컨벤션
- `@Tag`는 반드시 `*ApiDocs` 인터페이스에 선언한다.
  - `name`: 도메인 명 (첫 글자 대문자, 단수형)  
  - `description`: “~ 관련 API” 형태의 한글 설명  
- 컨트롤러 클래스에는 Swagger 관련 클래스 레벨 어노테이션을 두지 않는다.

## 5. 메서드 시그니처 / 어노테이션 컨벤션

### 5.1 공통 규칙
- `*ApiDocs` 인터페이스와 실제 컨트롤러 메서드의 **이름과 파라미터 순서/타입은 반드시 동일**해야 한다.
- 인터페이스에는 **Spring MVC 어노테이션을 사용하지 않는다.**
  - 사용 금지: `@GetMapping`, `@PostMapping`, `@RequestMapping`, `@PathVariable`, `@RequestBody`, `@RequestParam`, `@CurrentUser` 등
  - 해당 어노테이션은 모두 컨트롤러 구현 클래스에만 작성한다.

### 5.2 `@Operation`
- 각 엔드포인트 메서드에 `@Operation`을 붙인다.
- 필수 속성:
  - `summary`: 한 줄짜리 요약 (명령형, 간단히)
  - `description`: 조금 더 풀어 쓴 설명 (1~2문장)

### 5.3 `@CustomExceptionDescription`
- 예외 응답 스펙은 `SwaggerResponseDescription` Enum을 통해 관리한다.
- 각 메서드에 해당하는 Enum 값을 지정한다.
- 위치: `@Operation` 바로 아래에 붙인다.

### 5.4 `@Parameter`
- 경로/쿼리 파라미터 등 Swagger에 노출해야 하는 값에 대해서만 인터페이스에 `@Parameter`를 사용한다.
- 규칙:
  - path/쿼리/도메인 상 의미 있는 값만 문서화한다.
  - `@RequestBody` DTO는 필드 레벨 Validation / Schema로 충분하므로, 기본적으로 `@Parameter`를 사용하지 않는다.

## 6. 컨트롤러 구현 클래스 컨벤션
- 컨트롤러는 `*ApiDocs`를 구현한다.
- Swagger 관련 어노테이션은 전부 제거하고, HTTP 매핑/바인딩 관련 어노테이션만 유지한다.
- `*ApiDocs`를 구현하는 메서드에 대해서는 `@Override` 애너테이션을 사용하지 않는다. (Docs 인터페이스는 문서/계약 용도로만 사용하며, 구현 세부와의 강한 결합을 피한다.)

## 7. SwaggerConfig 연동 규칙
- `SwaggerConfig`에서 `OperationCustomizer`가 다음 순서로 `@CustomExceptionDescription`을 찾도록 구성한다.
  1. 구현 컨트롤러 메서드
  2. 구현 컨트롤러가 구현한 인터페이스의 동일 시그니처 메서드 (`*ApiDocs`)
- 따라서, 예외 응답 스펙은 `*ApiDocs` 인터페이스에만 정의해도 Swagger 문서에 반영된다.
