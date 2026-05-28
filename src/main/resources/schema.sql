-- =============================================================
-- 스마트 빗물받이 관제 시스템 (Smart Drain Control System)
-- 최종 PostgreSQL DDL 스크립트
-- =============================================================

-- [참고] 기존 테이블 초기화가 필요할 경우 아래 주석을 해제하고 실행하세요.
-- DROP TABLE IF EXISTS alerts CASCADE;
-- DROP TABLE IF EXISTS sensor_readings CASCADE;
-- DROP TABLE IF EXISTS drains CASCADE;
-- DROP TABLE IF EXISTS users CASCADE;


-- 1. 유저 테이블 (1인 관리자 및 현장 작업자)
CREATE TABLE IF NOT EXISTS users (
    user_id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(30) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    role VARCHAR(20) NOT NULL,
    registered_at TIMESTAMP NOT NULL DEFAULT NOW()
);


-- 2. 빗물받이 마스터 테이블
CREATE TABLE IF NOT EXISTS drains (
    drain_id BIGSERIAL PRIMARY KEY,
    address VARCHAR(255) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    total_depth DOUBLE PRECISION NOT NULL,
    water_level_threshold DOUBLE PRECISION NOT NULL,
    trash_level_threshold DOUBLE PRECISION NOT NULL,
    latest_device_photo_url VARCHAR(512) NULL,
    registered_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 기존 DB에 안전하게 컬럼 반영
ALTER TABLE IF EXISTS drains
ADD COLUMN IF NOT EXISTS latest_device_photo_url VARCHAR(512) NULL;

-- 구버전 스키마 컬럼 정리 (존재할 때만 삭제)
ALTER TABLE IF EXISTS drains DROP COLUMN IF EXISTS name;
ALTER TABLE IF EXISTS drains DROP COLUMN IF EXISTS warning_water_level;
ALTER TABLE IF EXISTS drains DROP COLUMN IF EXISTS danger_water_level;
ALTER TABLE IF EXISTS drains DROP COLUMN IF EXISTS warning_trash_level;
ALTER TABLE IF EXISTS drains DROP COLUMN IF EXISTS danger_trash_level;
ALTER TABLE IF EXISTS drains DROP COLUMN IF EXISTS id;


-- 3. 센서 데이터 수집 로그 테이블 (시계열 데이터)
CREATE TABLE IF NOT EXISTS sensor_readings (
    reading_id BIGSERIAL PRIMARY KEY,
    drain_id BIGINT NOT NULL,
    water_level DOUBLE PRECISION NOT NULL,
    trash_level DOUBLE PRECISION NOT NULL,
    battery_level DOUBLE PRECISION NOT NULL,
    signal_strength INTEGER NOT NULL,
    measured_at TIMESTAMP NOT NULL,
    received_at TIMESTAMP NOT NULL DEFAULT NOW(),
    
    CONSTRAINT fk_sensor_readings_drain FOREIGN KEY (drain_id) 
        REFERENCES drains (drain_id) ON DELETE CASCADE
);


-- 4. 위험 알림 및 정비 이력 통합 테이블 (사진 업로드 포함)
CREATE TABLE IF NOT EXISTS alerts (
    alert_id BIGSERIAL PRIMARY KEY,
    drain_id BIGINT NOT NULL,
    worker_id BIGINT NULL,
    risk_level VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    before_photo_url VARCHAR(512) NULL,
    after_photo_url VARCHAR(512) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    resolved_at TIMESTAMP NULL,
    
    CONSTRAINT fk_alerts_drain FOREIGN KEY (drain_id) 
        REFERENCES drains (drain_id) ON DELETE CASCADE,
    CONSTRAINT fk_alerts_worker FOREIGN KEY (worker_id) 
        REFERENCES users (user_id) ON DELETE SET NULL
);

-- 5. 리프레시 토큰 저장 테이블 (JWT 재발급용)
CREATE TABLE IF NOT EXISTS refresh_tokens (
    refresh_token_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(512) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    issued_at TIMESTAMP NOT NULL DEFAULT NOW(),
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    revoked_at TIMESTAMP NULL,

    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id)
        REFERENCES users (user_id) ON DELETE CASCADE
);


-- =============================================================
-- 성능 최적화를 위한 핵심 인덱스 (Index) 설정
-- =============================================================

-- ① 센서 데이터 시계열 범위 조회를 위한 결합 인덱스 (차트 가속용)
CREATE INDEX IF NOT EXISTS idx_sensor_readings_drain_time 
ON sensor_readings (drain_id, measured_at DESC);

-- ② 관제 대시보드 및 작업자 앱의 미해결 경보 실시간 조회를 위한 부분 인덱스 (네트워크/DB 부하 절약)
CREATE INDEX IF NOT EXISTS idx_alerts_active_status 
ON alerts (status) 
WHERE status IN ('ACTIVE', 'PROCESSING');

-- 사용자별 최신/유효 토큰 조회 가속
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_revoked_expires
ON refresh_tokens (user_id, revoked, expires_at DESC);
