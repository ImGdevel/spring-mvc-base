# CONVENTION DOCS WRITING CONVENTION

## 1. 목적
- 컨벤션 문서 자체에 대한 작성 규칙을 정의한다.
- 컨벤션 문서가 주제별로 구조화되고, 일관된 네이밍을 갖도록 한다.

## 2. 위치 규칙
- 모든 컨벤션 문서는 `docs/convention` 디렉터리 하위에 위치한다.
- 컨벤션 문서는 성격에 따라 전용 하위 폴더에 분리한다.
  - 예시: Swagger 관련 컨벤션 → `docs/convention/swagger`
  - 예시: 보안 관련 컨벤션 → `docs/convention/security`
- 공통/메타 컨벤션(이 문서와 같이 컨벤션 작성 규칙을 정의하는 문서)은 `docs/convention` 바로 아래에 위치한다.

## 3. 파일 네이밍 규칙
- 모든 컨벤션 문서 파일 이름은 **대문자 + 언더스코어**만 사용한다.
  - 사용 가능 문자: `A-Z`, `0-9`, `_`
- 추천 패턴: `{SUBJECT}_{DETAIL}_CONVENTION.md`
  - `{SUBJECT}`: 컨벤션의 큰 범주 (예: `SWAGGER_API`, `SECURITY`, `CONVENTION_DOCS`)
  - `{DETAIL}`: 보다 구체적인 주제 (예: `DOCS`, `ERROR_RESPONSE`, `WRITING`)
  - 마지막 토큰은 항상 `CONVENTION`으로 끝난다.

## 4. 내용 구성 규칙
- 문서의 최상단 제목은 파일명과 동일한 대문자/언더스코어 형태로 작성한다.
- 문서 내용은 규칙 중심으로 작성하고, 특정 도메인/엔티티에 종속된 예시는 필요 최소한으로만 사용하거나 생략한다.
- 규칙에서 플레이스홀더가 필요한 경우, 중괄호 형태로 표현한다.
  - 예: `{ControllerName}`, `{domain}`, `{ResponseDto}`

