# Caffeine Cache 가이드

## 1. Caffeine이란 무엇인가

- Java 진영에서 널리 사용되는 고성능 로컬(in-memory) 캐시 라이브러리이다.
- Guava Cache의 후속 격으로, 단순 LRU 대신 Window TinyLFU 등 최신 알고리즘을 사용해 높은 캐시 히트율을 목표로 한다.
- 동시성에 강하고, GC 영향과 메모리 사용량을 고려한 설계가 되어 있어 멀티스레드 웹 애플리케이션에서 안전하게 사용할 수 있다.
- Spring Cache 추상화(`@Cacheable`, `@CacheEvict`, `@CachePut`)와 자연스럽게 통합되며, `CacheManager`로 간단히 교체·추가할 수 있다.

## 2. 언제 Caffeine을 적용하는가

- **읽기 비율이 높은 조회성 트래픽**  
  - 동일한 데이터가 짧은 시간 동안 여러 번 조회될 때(게시글 상세, 랭킹, 설정 값, 코드 테이블 등)
  - DB/HDD/외부 API 호출 비용이 비싸고, 데이터가 즉시 최신일 필요는 없을 때
- **네트워크 왕복 없이 매우 빠른 응답 속도가 필요한 경우**  
  - 서버 안에서 메모리만 접근하므로 Redis 같은 원격 캐시보다 더 낮은 지연 시간을 기대할 수 있다.
- **단일 애플리케이션 인스턴스 또는 소규모 인스턴스를 운영할 때**  
  - 인스턴스마다 로컬 캐시를 두고, 분산 일관성보다는 단일 인스턴스의 성능 향상이 더 중요한 경우
- **Redis 같은 인프라를 도입하기 전, 간단한 캐시가 필요할 때**  
  - 운영 환경에 별도 인프라를 추가하지 않고도 JVM 내부에서만 캐시를 구성하고 싶을 때
- **Redis 앞단의 로컬 캐시(2단계 캐시)로 사용할 때**  
  - Caffeine(로컬) → Redis(원격) → DB 순으로 접근해, 대부분의 요청을 로컬 캐시에서 처리하고 Redis/DB 부하를 줄이고 싶을 때

## 3. Caffeine의 주요 이점

- **매우 빠른 성능과 높은 히트율**
  - 최신 캐시 알고리즘(Window TinyLFU 등)을 사용해, 제한된 메모리에서 최대한 많은 적중률을 얻도록 설계되어 있다.
  - 멀티코어 환경에 맞춘 락 최소화·경쟁 감소 전략을 사용해 높은 동시성을 지원한다.
- **유연한 만료 정책**
  - `expireAfterWrite`, `expireAfterAccess`, `refreshAfterWrite` 등 다양한 시간 기반 만료 전략을 제공한다.
  - 최대 용량(개수 또는 메모리 추정치) 기반의 `maximumSize`, `maximumWeight` 정책으로 메모리 사용량을 제어할 수 있다.
- **Spring Cache와의 쉬운 통합**
  - `Caffeine.newBuilder()`로 캐시를 구성하고, `CaffeineCacheManager`를 통해 Spring의 `CacheManager`로 등록할 수 있다.
  - 비즈니스 코드에서는 `@Cacheable` / `@CacheEvict` / `@CachePut` 등 Spring Cache 애너테이션만 사용하면 되어, 구현 상세가 캡슐화된다.
- **추가 인프라 없이 사용 가능**
  - JVM 프로세스 안에서만 동작하므로 Redis 클러스터, 네트워크 세팅, 모니터링 등 별도의 인프라 구축 없이 바로 적용할 수 있다.
  - **풍부한 모니터링/튜닝 포인트**
  - hit/miss, load, eviction 등 메트릭을 제공하며, Micrometer 등과 연동하여 모니터링 대시보드에 노출할 수 있다.

## 4. Caffeine을 쓰면 안 되는 경우

| 상황 | 이유 |
| --- | --- |
| 여러 서버가 동일한 캐시 데이터를 공유해야 할 때 | Caffeine은 로컬 캐시이므로 분산 환경 일관성 유지 불가 |
| 실시간으로 변경되는 데이터가 정확히 반영되어야 할 때 | stale 데이터 위험 |
| 캐시 용량이 매우 클 때 (수백 MB~GB) | 로컬 메모리 사용 증가로 GC 영향 |
| 캐시 만료를 이벤트 기반으로 통제해야 할 때 | Redis pub/sub 또는 DB trigger 필요 |

## 5. 이 프로젝트에서 Caffeine 적용을 고려해볼 수 있는 곳

이 프로젝트에서는 이미 Redis를 기반으로 한 인프라 캐시(토큰 블랙리스트, 조회수 중복 방지 등)를 사용하고 있다. Caffeine은 **“서버 로컬에서 읽기 비율이 높은 조회성 데이터”**에 집중해서 적용하는 것이 좋다.

### 5.1 Caffeine 적용을 고려할 수 있는 후보

- `PostService.getPostDetails(Long postId, Long memberId)`
  - 게시글 상세 조회는 읽기 비율이 높고, 약간의 지연된 일관성은 크게 문제 되지 않는 도메인이다.
  - 패턴 예시:
    - `@Cacheable(cacheNames = "postDetails", key = "#postId + ':' + #memberId")`
    - `updatePost`, `deletePost` 메서드에서 동일 키에 대해 `@CacheEvict` 적용.

- `MemberService.getMemberProfile(Long id)`
  - 회원 프로필 조회 역시 읽기 비율이 높고, 업데이트 빈도는 상대적으로 낮다.
  - 패턴 예시:
    - `@Cacheable(cacheNames = "memberProfile", key = "#id")`
    - `updateMember`, `deleteMember`에서 같은 키로 `@CacheEvict`.

- 코드/설정 테이블 및 공통 조회 데이터 (향후 추가 예정인 기능 포함)
  - 태그 목록, 카테고리, 코드 테이블, 공통 설정 값 등 자주 바뀌지 않는 조회성 데이터는 Caffeine으로 짧은 TTL+최대 사이즈를 줘서 캐시하기 좋다.
  - 이 영역은 Redis까지 사용하지 않고, Caffeine 단일 계층 캐시만으로도 충분한 경우가 많다.

### 5.2 Redis를 유지하거나 Caffeine을 신중히 써야 하는 부분

- `TokenBlacklistService` (리프레시 토큰 블랙리스트)
  - 여러 서버가 동시에 토큰 상태를 공유해야 하고, 블랙리스트에 등록된 즉시 전 서버에서 차단되어야 한다.
  - 여기서 Caffeine만 사용하면 인스턴스별로 상태가 달라지는 문제가 생기므로, 현재처럼 Redis 중심 구현을 유지하는 것이 바람직하다.

- `ViewCountPolicy` (조회수 중복 방지)
  - 여러 서버가 떠 있는 환경에서는 로컬 캐시만 사용하면 인스턴스마다 한 번씩 더 카운트되는 문제가 생길 수 있다.
  - 조회수가 토큰만큼 민감하진 않더라도, “여러 인스턴스 간 일관된 기준으로 중복을 막는 것”이 중요하다면 Redis 기반 구현을 유지하는 편이 더 안전하다.
  - 다만, 부하 완화를 위해 **아주 짧은 TTL의 Caffeine 로컬 캐시를 L1 캐시처럼 추가**하는 방안은 별도로 검토할 수 있다.

정리하면, 이 프로젝트에서는 Caffeine을 **게시글/회원 등 조회성 서비스 계층에 로컬 캐시로 적용**하고, Redis는 **토큰, 조회수 중복 방지 등 분산 일관성이 중요한 인프라 성격의 데이터**에 계속 사용하는 방향이 적절하다.

