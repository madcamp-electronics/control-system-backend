BEGIN;

SET LOCAL TIME ZONE 'Asia/Seoul';

-- These are simulated drain locations around KAIST Kaimaru (N11), not a
-- survey of real drain assets. The address is also the stable seed key, so the
-- script can be run repeatedly without duplicating drains or readings.
CREATE TEMP TABLE kaimaru_drain_seed (
    mock_key VARCHAR(16) PRIMARY KEY,
    address VARCHAR(255) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_depth DOUBLE PRECISION NOT NULL,
    water_level_threshold DOUBLE PRECISION NOT NULL,
    cover_distance_threshold DOUBLE PRECISION NOT NULL,
    water_multiplier DOUBLE PRECISION NOT NULL,
    standing_water_offset DOUBLE PRECISION NOT NULL,
    cover_distance_baseline DOUBLE PRECISION NOT NULL,
    battery_start DOUBLE PRECISION NOT NULL,
    signal_base INTEGER NOT NULL,
    sensor_phase INTEGER NOT NULL
) ON COMMIT DROP;

INSERT INTO kaimaru_drain_seed VALUES
    (
        'KM-N01',
        '대전광역시 유성구 대학로 291 KAIST N11 카이마루 북서측 진입로 저점',
        36.374060, 127.358930, 'FLOOD_RISK',
        65.0, 45.0, 30.0,
        1.12, 2.0, 48.0, 96.8, -58, 0
    ),
    (
        'KM-N02',
        '대전광역시 유성구 대학로 291 KAIST N11 카이마루 북동측 하역도로',
        36.374050, 127.359520, 'NORMAL',
        62.0, 44.0, 30.0,
        0.94, 1.0, 51.0, 95.9, -62, 1
    ),
    (
        'KM-N03',
        '대전광역시 유성구 대학로 291 KAIST N11-N12 연결 보행로',
        36.373620, 127.358780, 'NORMAL',
        70.0, 50.0, 30.0,
        0.80, 0.5, 53.0, 97.4, -55, 2
    ),
    (
        'KM-N04',
        '대전광역시 유성구 대학로 291 KAIST N11-북측운동장 연결로',
        36.373310, 127.359660, 'NEED_INSPECTION',
        60.0, 43.0, 30.0,
        1.04, 1.2, 46.0, 94.7, -66, 3
    );

-- Add missing drains. Existing seed drains are updated below instead of
-- inserted again.
INSERT INTO drains (
    address,
    latitude,
    longitude,
    status,
    total_depth,
    trash_level_threshold,
    cover_distance_threshold,
    latest_device_photo_url,
    registered_at
)
SELECT
    seed.address,
    seed.latitude,
    seed.longitude,
    seed.status,
    seed.total_depth,
    seed.water_level_threshold,
    seed.cover_distance_threshold,
    NULL,
    TIMESTAMP '2026-08-15 10:00:00'
FROM kaimaru_drain_seed seed
WHERE NOT EXISTS (
    SELECT 1
    FROM drains existing
    WHERE existing.address = seed.address
);

-- Keep the simulated asset metadata deterministic on repeat runs. Any photo
-- uploaded later is intentionally preserved.
UPDATE drains drain
SET
    latitude = seed.latitude,
    longitude = seed.longitude,
    status = seed.status,
    total_depth = seed.total_depth,
    trash_level_threshold = seed.water_level_threshold,
    cover_distance_threshold = seed.cover_distance_threshold
FROM kaimaru_drain_seed seed
WHERE drain.address = seed.address;

-- Daily precipitation supplied by the user:
-- 08/16 10.5 mm, 08/17 3.9 mm, 08/18 0 mm, 08/19 0 mm,
-- 08/20 1.8 mm, 08/21 0.6 mm, 08/22 30.4 mm.
--
-- Each array contains modeled water levels at 00, 03, 06, 09, 12, 15,
-- 18, and 21 KST. The profile assumes daytime showers because only daily
-- precipitation totals were provided.
WITH weather_profile (weather_date, rainfall_mm, base_levels_cm) AS (
    VALUES
        (DATE '2026-08-16', 10.5, ARRAY[9.0, 11.0, 18.0, 25.0, 27.0, 23.0, 18.0, 15.0]::DOUBLE PRECISION[]),
        (DATE '2026-08-17',  3.9, ARRAY[13.0, 12.0, 11.0, 11.0, 13.0, 17.0, 16.0, 14.0]::DOUBLE PRECISION[]),
        (DATE '2026-08-18',  0.0, ARRAY[12.0, 11.0, 10.0,  9.0,  8.0,  7.0,  7.0,  6.0]::DOUBLE PRECISION[]),
        (DATE '2026-08-19',  0.0, ARRAY[ 6.0,  5.5,  5.0,  5.0,  4.5,  4.5,  4.0,  4.0]::DOUBLE PRECISION[]),
        (DATE '2026-08-20',  1.8, ARRAY[ 4.0,  4.0,  5.0,  6.0,  7.0,  9.0,  8.0,  7.0]::DOUBLE PRECISION[]),
        (DATE '2026-08-21',  0.6, ARRAY[ 6.0,  5.5,  5.0,  5.5,  6.0,  7.0,  6.5,  6.0]::DOUBLE PRECISION[]),
        (DATE '2026-08-22', 30.4, ARRAY[ 7.0,  9.0, 18.0, 31.0, 43.0, 49.0, 45.0, 40.0]::DOUBLE PRECISION[])
),
time_slots AS (
    SELECT
        weather.weather_date
            + ((sample.sample_index - 1) * INTERVAL '3 hours') AS measured_at,
        weather.rainfall_mm,
        weather.base_levels_cm[sample.sample_index] AS base_water_level,
        sample.sample_index
    FROM weather_profile weather
    CROSS JOIN generate_series(1, 8) AS sample(sample_index)
),
mock_readings AS (
    SELECT
        drain.drain_id,
        ROUND(
            LEAST(
                drain.total_depth - 1.0,
                GREATEST(
                    0.0,
                    slot.base_water_level * seed.water_multiplier
                        + seed.standing_water_offset
                        + (MOD(slot.sample_index + seed.sensor_phase, 3) - 1) * 0.25
                )
            )::NUMERIC,
            1
        )::DOUBLE PRECISION AS water_level,
        ROUND(
            (
                CASE
                    -- Floating debris partially blocks KM-N04 during the
                    -- heavy rain on 08/22.
                    WHEN seed.mock_key = 'KM-N04'
                        AND slot.measured_at >= TIMESTAMP '2026-08-22 12:00:00'
                    THEN 24.0 + (MOD(slot.sample_index, 3) - 1) * 0.4
                    ELSE seed.cover_distance_baseline
                        + (MOD(slot.sample_index + seed.sensor_phase, 5) - 2) * 0.35
                END
            )::NUMERIC,
            1
        )::DOUBLE PRECISION AS cover_distance,
        ROUND(
            GREATEST(
                80.0,
                seed.battery_start
                    - EXTRACT(EPOCH FROM (
                        slot.measured_at - TIMESTAMP '2026-08-16 00:00:00'
                    )) / 86400.0 * 0.42
            )::NUMERIC,
            1
        )::DOUBLE PRECISION AS battery_level,
        seed.signal_base
            + MOD(slot.sample_index + seed.sensor_phase, 5) - 2 AS signal_strength,
        slot.measured_at,
        slot.measured_at
            + ((8 + seed.sensor_phase) * INTERVAL '1 second') AS received_at
    FROM kaimaru_drain_seed seed
    JOIN LATERAL (
        SELECT existing.drain_id, existing.total_depth
        FROM drains existing
        WHERE existing.address = seed.address
        ORDER BY existing.drain_id
        LIMIT 1
    ) drain ON TRUE
    CROSS JOIN time_slots slot
)
INSERT INTO sensor_readings (
    drain_id,
    trash_level,
    cover_distance,
    battery_level,
    signal_strength,
    measured_at,
    received_at
)
SELECT
    mock.drain_id,
    mock.water_level,
    mock.cover_distance,
    mock.battery_level,
    mock.signal_strength,
    mock.measured_at,
    mock.received_at
FROM mock_readings mock
WHERE NOT EXISTS (
    SELECT 1
    FROM sensor_readings existing
    WHERE existing.drain_id = mock.drain_id
      AND existing.measured_at = mock.measured_at
);

-- Add two active alerts that correspond to the 08/22 peak: one high-water
-- location and one cover-obstruction location. Existing active alerts for the
-- same drain are preserved and not duplicated.
WITH alert_seed (mock_key, risk_level, created_at) AS (
    VALUES
        ('KM-N01', 'FLOOD_RISK',     TIMESTAMP '2026-08-22 15:05:00'),
        ('KM-N04', 'NEED_INSPECTION', TIMESTAMP '2026-08-22 12:05:00')
)
INSERT INTO alerts (
    drain_id,
    worker_id,
    risk_level,
    status,
    before_photo_url,
    after_photo_url,
    created_at,
    updated_at,
    resolved_at
)
SELECT
    drain.drain_id,
    NULL,
    alert_seed.risk_level,
    'ACTIVE',
    NULL,
    NULL,
    alert_seed.created_at,
    alert_seed.created_at,
    NULL
FROM alert_seed
JOIN kaimaru_drain_seed seed ON seed.mock_key = alert_seed.mock_key
JOIN LATERAL (
    SELECT existing.drain_id
    FROM drains existing
    WHERE existing.address = seed.address
    ORDER BY existing.drain_id
    LIMIT 1
) drain ON TRUE
WHERE NOT EXISTS (
    SELECT 1
    FROM alerts existing
    WHERE existing.drain_id = drain.drain_id
      AND existing.status IN ('ACTIVE', 'PROCESSING')
);

COMMIT;

-- Verification summary: expected result is 4 drains, 224 readings, 2 alerts
-- when the database did not already contain these mock assets.
SELECT
    drain.drain_id,
    drain.address,
    drain.status,
    COUNT(reading.reading_id) AS reading_count,
    MIN(reading.measured_at) AS first_measured_at,
    MAX(reading.measured_at) AS last_measured_at,
    ROUND(MAX(reading.trash_level)::NUMERIC, 1) AS peak_water_level_cm
FROM drains drain
LEFT JOIN sensor_readings reading ON reading.drain_id = drain.drain_id
WHERE drain.address IN (
    '대전광역시 유성구 대학로 291 KAIST N11 카이마루 북서측 진입로 저점',
    '대전광역시 유성구 대학로 291 KAIST N11 카이마루 북동측 하역도로',
    '대전광역시 유성구 대학로 291 KAIST N11-N12 연결 보행로',
    '대전광역시 유성구 대학로 291 KAIST N11-북측운동장 연결로'
)
GROUP BY drain.drain_id, drain.address, drain.status
ORDER BY drain.drain_id;
