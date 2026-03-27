-- 更新教练数据，设置专业领域和标签
UPDATE coach_detail 
SET specialties = '["力量训练","康复训练"]'::jsonb,
    tags = '["增肌塑形","体态矫正","运动康复"]'::jsonb,
    teaching_style = 'encouraging',
    work_years = 8,
    student_count = 2000,
    rating = '99%'
WHERE user_id = 3;

UPDATE coach_detail 
SET specialties = '["瑜伽","普拉提"]'::jsonb,
    tags = '["流瑜伽","普拉提","冥想"]'::jsonb,
    teaching_style = 'gentle',
    work_years = 10,
    student_count = 3500,
    rating = '98%'
WHERE user_id = 4;

UPDATE coach_detail 
SET specialties = '["CrossFit","体能训练"]'::jsonb,
    tags = '["CrossFit","体能训练","减脂"]'::jsonb,
    teaching_style = 'strict',
    work_years = 6,
    student_count = 1500,
    rating = '97%'
WHERE user_id = 24;

UPDATE coach_detail 
SET specialties = '["拳击","格斗"]'::jsonb,
    tags = '["职业拳手","拳击","防身术"]'::jsonb,
    teaching_style = 'professional',
    work_years = 12,
    student_count = 3000,
    rating = '99%'
WHERE user_id = 25;
