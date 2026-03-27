-- ========================================================-- 智能健身系统数据库迁移脚本-- 版本：V11-- 说明：为已有的教练用户初始化 coach_detail 记录-- ========================================================

-- 为所有具有 COACH 角色但还没有 coach_detail 记录的用户创建详情记录
INSERT INTO coach_detail (
    user_id,
    personal_image_url,
    tags,
    work_years,
    specialties,
    teaching_style,
    education,
    training,
    languages,
    bio,
    experience,
    honors,
    emergency_contact,
    certifications,
    availability,
    student_count,
    rating,
    deleted,
    create_time,
    update_time
)
SELECT
    su.id,
    NULL,                           -- personal_image_url
    '[]'::jsonb,                    -- tags
    0,                              -- work_years
    '[]'::jsonb,                    -- specialties
    NULL,                           -- teaching_style
    NULL,                           -- education
    NULL,                           -- training
    '[]'::jsonb,                    -- languages
    NULL,                           -- bio
    NULL,                           -- experience
    '[]'::jsonb,                    -- honors
    '{}'::jsonb,                    -- emergency_contact
    '[]'::jsonb,                    -- certifications
    '{}'::jsonb,                    -- availability
    0,                              -- student_count
    '99%',                          -- rating
    FALSE,                          -- deleted
    CURRENT_TIMESTAMP,              -- create_time
    CURRENT_TIMESTAMP               -- update_time
FROM sys_user su
INNER JOIN sys_user_role sur ON su.id = sur.user_id
INNER JOIN sys_role sr ON sur.role_id = sr.id AND sr.role_code = 'COACH'
WHERE su.deleted = false
  AND NOT EXISTS (
    SELECT 1 FROM coach_detail cd WHERE cd.user_id = su.id
  );

-- 记录迁移信息
DO $$
DECLARE
    inserted_count INTEGER;
BEGIN
    GET DIAGNOSTICS inserted_count = ROW_COUNT;
    RAISE NOTICE '已为 % 名教练初始化 coach_detail 记录', inserted_count;
END $$;
