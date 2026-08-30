BEGIN;

-- Deleting the four mock drains also removes their sensor readings and alerts
-- through the schema's ON DELETE CASCADE foreign keys.
DELETE FROM drains
WHERE address IN (
    '대전광역시 유성구 대학로 291 KAIST N11 카이마루 북서측 진입로 저점',
    '대전광역시 유성구 대학로 291 KAIST N11 카이마루 북동측 하역도로',
    '대전광역시 유성구 대학로 291 KAIST N11-N12 연결 보행로',
    '대전광역시 유성구 대학로 291 KAIST N11-북측운동장 연결로'
);

COMMIT;
