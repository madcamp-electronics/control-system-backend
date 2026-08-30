# Smart Drain Backend

한이음 드림업 프로젝트  
**실시간 수위 측정 및 이물질 감지 기반 스마트 빗물받이 관제 시스템** 백엔드입니다.

## 1. 프로젝트 목표

- 빗물받이 위치/상태/임계치 기준값 관리
- IoT 센서 원시 데이터 수집/저장
- 백엔드 위험도 분석(risk) 기반 알림(alert) 생성
- 자율 출동형 경보 접수/완료 흐름 지원
- 대시보드 통합 조회 API 제공

초음파 센서가 전송하는 값은 센서에서 수면까지의 거리(cm)입니다. 기존 MQTT
필드명 `trashLevel`과 DB 컬럼 `trash_level`은 장치 및 스키마 호환을 위해 유지하며,
저장값과 조회 API의 `waterLevel`은 `빗물받이 전체 높이 - 감지 거리`로 계산합니다.

## 2. 핵심 도메인 흐름

1. 센서는 위험도를 판단하지 않고 원시 측정값만 전송
2. 백엔드는 `SensorReading` 저장
3. `risk` 도메인이 `Drain` 임계치와 센서값을 비교해 위험도 판단
4. 위험 시 `alert` 도메인에서 알림 생성
5. 작업자는 `ACTIVE` 경보를 접수하여 `PROCESSING` 상태로 전환
6. 정비 전/후 사진 URL은 `alerts`에 직접 저장
7. 정비 완료 시 경보는 `RESOLVED`, 빗물받이 상태는 `NORMAL`로 복구

## 3. 기술 스택

- Java 17
- Spring Boot 3.5.x
- Gradle 8.14.x
- Spring Data JPA / Hibernate
- PostgreSQL
- Spring Security (구조만 준비, 인증 로직은 TODO)

## 환경변수와 프론트엔드 연결

애플리케이션은 프로젝트 루트의 `.env`를 선택적으로 읽습니다. 프론트엔드 개발
주소가 기본값과 다르면 다음 값을 추가하세요.

```env
CORS_ALLOWED_ORIGINS=http://localhost:3000
```

센서 조회 API:

- `GET /api/v1/sensors/latest`: 시설별 최신 센서 측정값
- `GET /api/v1/sensors/drains/{drainId}/latest`: 한 시설의 최신 측정값
- `GET /api/v1/sensors/drains/{drainId}/history`: 기간별 계산 수위 이력

## 4. 패키지 구조

루트 패키지: `com.hanium.smart_drain`

```text
com.hanium.smart_drain
 ├─ global
 │  ├─ config
 │  ├─ exception
 │  ├─ response
 │  └─ security
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
 ├─ risk
 │  ├─ service
 │  ├─ policy
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
