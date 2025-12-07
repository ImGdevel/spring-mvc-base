# Spring Cache AOP 가이드 (`@Cacheable`, `@CacheEvict`, `@CachePut`)

## 1. Spring Cache와 AOP 개요

- Spring Cache는 **메서드 호출 결과를 캐시에 저장/조회/삭제**하기 위한 추상화이다.
- `@Cacheable`, `@CacheEvict`, `@CachePut` 애너테이션은 모두 **AOP(관점 지향 프로그래밍)** 기반으로 동작한다.
  - 실제로는 프록시(proxy)가 메서드 호출을 가로채서, 캐시를 먼저 조회하거나, 메서드 실행 후 캐시를 갱신·삭제한다.
  - 비즈니스 로직 코드에서는 “캐시 관련 코드(if/else, Map 조작 등)”를 전혀 작성하지 않고, 애너테이션만으로 캐시를 적용할 수 있다.
- 실제 캐시 저장소는 `CacheManager` 설정에 따라 Caffeine, Redis, SimpleMap 등으로 바뀔 수 있고, 비즈니스 코드는 이를 알 필요가 없다.

## 2. `@Cacheable` – 조회/저장용 캐시 AOP

### 2.1 역할과 목적

- 메서드를 **읽기 전용 조회처럼 사용**할 때, 결과를 캐시에 저장해 두고 다음 호출부터는 캐시에서 바로 꺼내오는 역할을 한다.
- 가장 일반적인 캐싱 패턴으로, DB·외부 API 호출 등 **비용이 큰 연산의 결과를 재사용**하는 데 사용한다.

### 2.2 동작 방식 (AOP 관점)

1. 클라이언트가 `@Cacheable`이 붙은 메서드를 호출한다.
2. Spring AOP 프록시가 호출을 가로챈다.
3. 캐시 이름(`cacheNames`)과 키(`key`)를 기준으로 **캐시를 먼저 조회**한다.
   - 캐시 히트(hit)인 경우: 실제 메서드를 실행하지 않고, 캐시 값을 그대로 반환한다.
   - 캐시 미스(miss)인 경우: 실제 메서드를 실행한 뒤, 반환값을 캐시에 저장하고 클라이언트에 반환한다.

### 2.3 사용 예시

```java
@Service
public class MemberQueryService {

    @Cacheable(cacheNames = "memberById", key = "#memberId")
    public MemberDetailDto getMemberDetail(Long memberId) {
        // 비용이 큰 DB 조회 또는 외부 API 호출
        return findMemberDetailFromDatabase(memberId);
    }
}
```

- 첫 번째 호출: DB 조회 후 결과를 `"memberById"` 캐시에 저장.
- 이후 동일한 `memberId`에 대한 호출: 캐시에서 바로 반환 (DB 미접근).

### 2.4 주요 속성

- `cacheNames` / `value`: 사용할 캐시 이름(또는 이름 목록).
- `key`: 캐시 키 SpEL 표현식 (예: `#id`, `#root.args[0]` 등).
- `condition`: 특정 조건일 때만 캐시 적용 (예: `#id > 0`).
- `unless`: 결과값에 따라 캐시 저장을 건너뛰는 조건 (예: `#result == null`).

## 3. `@CacheEvict` – 캐시 삭제용 AOP

### 3.1 역할과 목적

- 캐시된 데이터가 **더 이상 유효하지 않을 때 삭제**하는 역할을 한다.
- 보통 **데이터 변경(등록/수정/삭제) 시점**에 함께 사용해, 이후 조회가 최신 값을 가져가도록 만든다.

### 3.2 동작 방식 (AOP 관점)

1. 클라이언트가 `@CacheEvict`이 붙은 메서드를 호출한다.
2. Spring AOP 프록시가 호출을 가로채고, 설정에 따라 **메서드 전/후에 캐시를 삭제**한다.
   - `beforeInvocation = false`(기본값): 메서드 실행이 정상 완료된 후에 캐시 삭제.
   - `beforeInvocation = true`: 메서드 실행 전에 캐시 삭제.
3. `allEntries = true`이면 해당 캐시 이름에 속한 **모든 키를 삭제**한다.

### 3.3 사용 예시

```java
@Service
public class MemberCommandService {

    @CacheEvict(cacheNames = "memberById", key = "#memberId")
    public void updateMember(Long memberId, MemberUpdateRequest request) {
        updateMemberInDatabase(memberId, request);
    }
}
```

- 멤버 수정 후 `"memberById"` 캐시에서 해당 `memberId` 키를 제거.
- 이후 조회 시 `@Cacheable` 메서드가 다시 DB를 조회하고, 최신 값을 캐시에 넣는다.

```java
@CacheEvict(cacheNames = "memberById", allEntries = true)
public void bulkUpdateMembers(...) {
    // 대량 업데이트 로직
}
```

- 멤버를 대량으로 수정하는 경우, 특정 키만 지우기 어렵다면 `allEntries = true`로 전체 캐시를 비울 수 있다.

### 3.4 주요 속성

- `cacheNames` / `value`: 삭제 대상 캐시 이름.
- `key`: 삭제할 캐시 키.
- `allEntries`: `true`이면 해당 캐시 이름의 모든 엔트리를 삭제.
- `beforeInvocation`: 메서드 실행 전에 삭제할지 여부 (예외 발생 시에도 캐시를 지우고 싶다면 `true` 고려).

## 4. `@CachePut` – 항상 실행 + 캐시 갱신용 AOP

### 4.1 역할과 목적

- 메서드를 **항상 실행하면서**, 그 결과를 캐시에 **강제로 반영(갱신)**할 때 사용한다.
- `@Cacheable`과 달리 캐시를 조회해서 건너뛰지 않고, **항상 메서드를 호출**한다.
- 주로 “업데이트 후 최신 값을 캐시에 저장”하는 용도로 사용한다.

### 4.2 동작 방식 (AOP 관점)

1. 클라이언트가 `@CachePut`이 붙은 메서드를 호출한다.
2. Spring AOP 프록시가 호출을 가로채지만, 캐시를 조회하지 않고 **항상 실제 메서드를 실행**한다.
3. 메서드 실행 결과(반환값)를 지정된 캐시에 저장한 뒤, 그 값을 클라이언트에 반환한다.

### 4.3 사용 예시

```java
@Service
public class MemberCommandService {

    @CachePut(cacheNames = "memberById", key = "#result.id")
    public MemberDetailDto updateMemberAndReturn(Long memberId, MemberUpdateRequest request) {
        MemberDetailDto updated = updateMemberInDatabaseAndLoad(memberId, request);
        return updated; // 이 반환값이 캐시에 저장된다.
    }
}
```

- 매번 DB를 통해 최신 정보를 읽어 오되, 그 결과를 `"memberById"` 캐시에 덮어쓴다.
- 이후 조회 메서드에서 `@Cacheable`을 사용하면 갱신된 값을 캐시에서 재사용할 수 있다.

### 4.4 `@Cacheable` vs `@CachePut` 비교

- `@Cacheable`
  - 캐시가 있으면 메서드를 **실행하지 않고** 캐시에서 바로 반환.
  - 읽기 최적화용.
- `@CachePut`
  - 캐시와 상관없이 메서드를 **항상 실행**하고, 결과를 캐시에 저장.
  - 쓰기 후 최신값 강제 반영용.

## 5. 세 애너테이션 조합 패턴

### 5.1 전형적인 CRUD + 캐시 패턴

- 조회(`GET`): `@Cacheable`
- 생성/수정/삭제(`POST`/`PUT`/`DELETE`): `@CacheEvict` 또는 `@CachePut`

예시 흐름:

1. `GET /members/{id}` → `@Cacheable`로 조회, 없으면 DB에서 읽고 캐시에 저장.
2. `PUT /members/{id}` → `@CacheEvict`로 해당 멤버 캐시 삭제 또는 `@CachePut`로 갱신.
3. 이후 `GET /members/{id}` → 최신 값이 캐시 또는 DB에서 다시 채워짐.

### 5.2 조합 시 주의점

- 동일한 캐시 이름과 키를 사용하는 메서드들끼리 **일관된 정책**을 가져야 한다.
  - `@Cacheable(cacheNames = "memberById", key = "#id")`
  - `@CacheEvict(cacheNames = "memberById", key = "#memberId")`
  - `@CachePut(cacheNames = "memberById", key = "#result.id")`
- 트랜잭션 경계와 캐시 AOP 순서도 함께 고려해야 한다.
  - 보통 데이터 변경이 성공적으로 완료된 후 캐시를 지우거나 갱신하도록 설계한다.

## 6. 언제 어떤 애너테이션을 쓸까?

- `@Cacheable`
  - 동일한 입력에 대해 결과가 자주 재사용되고, 값이 자주 바뀌지 않는 조회성 로직.
  - 예: 게시글 상세, 랭킹/통계, 코드 테이블, 설정 값 등.
- `@CacheEvict`
  - 데이터 변경 후 기존 캐시를 무효화하고 싶을 때.
  - 예: 게시글 수정/삭제, 회원 정보 변경, 설정 값 변경.
- `@CachePut`
  - 변경 직후 **최신 상태를 캐시에 강제로 반영**하고 싶을 때.
  - 예: 업데이트 후 최신 DTO를 반환하면서 동시에 캐시도 갱신하는 API.

---

이 문서는 학습 목적의 개요이므로, 실제 프로젝트에서는 Caffeine·Redis 등과 결합한 `CacheManager` 설정, 캐시 키 설계, 만료 전략, 모니터링 정책 등을 함께 설계하는 것이 좋다.

