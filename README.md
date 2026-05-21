# Smart Drain Backend

한이음 드림업 프로젝트  
**실시간 수위 측정 및 이물질 감지 기반 스마트 빗물받이 관제 시스템** 백엔드 저장소입니다.

## 1. 프로젝트 개요

도심 침수 예방을 위해 빗물받이 상태를 실시간 모니터링하고,  
수위/이물질 적체량 기반 위험 알림을 제공하는 관제 백엔드를 구축합니다.

핵심 목표:
- 빗물받이 위치/상태 데이터 관리
- IoT 센서 데이터 수집 및 저장
- 위험 알림 생성/조회/확인 처리
- 대시보드용 요약/지도 데이터 API 제공

## 2. 백엔드 기술 스택

- Language: `Java 17`
- Framework: `Spring Boot 3.5.x`
- Build Tool: `Gradle 8.14.x`
- Database: `PostgreSQL`
- ORM: `Spring Data JPA (Hibernate)`
- Validation: `Spring Validation`
- Security: `Spring Security`
- Realtime (확장 예정): `WebSocket`, `MQTT`

## 3. 패키지 구조

루트 패키지: `com.hanium.smart_drain`

```text
com.hanium.smart_drain
 ├─ global
 │  ├─ config
 │  ├─ exception
 │  └─ response
 │
 ├─ drain
 │  ├─ controller
 │  ├─ service
 │  ├─ repository
 │  ├─ entity
 │  └─ dto
 │
 ├─ sensor
 │  ├─ controller
 │  ├─ service
 │  ├─ repository
 │  ├─ entity
 │  └─ dto
 │
 ├─ alert
 │  ├─ controller
 │  ├─ service
 │  ├─ repository
 │  ├─ entity
 │  └─ dto
 │
 └─ dashboard
    ├─ controller
    ├─ service
    └─ dto
```

## 4. 현재 구현 범위

현재는 **초기 구조(스켈레톤)** 단계입니다.

- 도메인별 Entity / DTO / Repository / Service / Controller 골격 생성
- 공통 응답(`ApiResponse`) 및 전역 예외 처리 기본 구조 생성
- Ping API 엔드포인트 생성
- 실제 비즈니스 로직(위험도 계산, 조회 쿼리, 알림 정책)은 미구현

## 5. 실행 환경 설정

`src/main/resources/application.properties`에서 DB 설정을 사용합니다.

```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/smart_drain}
spring.datasource.username=${DB_USERNAME:smart_drain_user}
spring.datasource.password=${DB_PASSWORD:smart_drain_password}
```

필요 시 환경변수로 덮어쓸 수 있습니다.

```bash
export DB_URL=jdbc:postgresql://localhost:5432/smart_drain
export DB_USERNAME=smart_drain_user
export DB_PASSWORD=smart_drain_password
```

## 6. 로컬 실행

```bash
./gradlew clean build
./gradlew bootRun
```

앱 실행 후 기본 포트: `8080`

## 7. 기본 API (Health/Ping)

- `GET /api/drains/ping` → `drain api ok`
- `GET /api/sensors/ping` → `sensor api ok`
- `GET /api/alerts/ping` → `alert api ok`
- `GET /api/dashboard/ping` → `dashboard api ok`

## 8. 향후 확장 계획

- 유지보수 작업자 배정 워크플로우
- MQTT 기반 센서 실시간 수신
- 기상청/외부 날씨 API 연동
- WebSocket 실시간 알림 푸시
- PostGIS 기반 공간 검색 및 반경 조회

## 9. Git 보안/운영 가이드

- `.env`, 키/인증서 파일은 `.gitignore`로 제외
- 실제 운영 비밀번호/토큰은 절대 저장소에 커밋하지 않음
- 환경변수 또는 별도 시크릿 관리 방식 사용

