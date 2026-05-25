-- ========================================================
-- 修改时间字段类型：TIMESTAMP WITH TIME ZONE -> TIMESTAMP
-- 说明: 将带时区的时间戳改为不带时区，以支持 Java LocalDateTime
-- ========================================================

-- 1. 修改 sys_user 表
ALTER TABLE sys_user 
    ALTER COLUMN create_time TYPE TIMESTAMP,
    ALTER COLUMN update_time TYPE TIMESTAMP;

-- 2. 修改 sys_role 表
ALTER TABLE sys_role 
    ALTER COLUMN create_time TYPE TIMESTAMP,
    ALTER COLUMN update_time TYPE TIMESTAMP;

-- 3. 修改 sys_user_role 表
ALTER TABLE sys_user_role 
    ALTER COLUMN create_time TYPE TIMESTAMP;

-- 4. 修改 sys_permission 表
ALTER TABLE sys_permission 
    ALTER COLUMN create_time TYPE TIMESTAMP,
    ALTER COLUMN update_time TYPE TIMESTAMP;

-- 5. 修改 sys_role_permission 表
ALTER TABLE sys_role_permission 
    ALTER COLUMN create_time TYPE TIMESTAMP;

-- 6. 修改 sys_file 表
ALTER TABLE sys_file 
    ALTER COLUMN create_time TYPE TIMESTAMP;

-- 7. 修改 fitness_course 表
ALTER TABLE fitness_course 
    ALTER COLUMN start_time TYPE TIMESTAMP,
    ALTER COLUMN end_time TYPE TIMESTAMP,
    ALTER COLUMN create_time TYPE TIMESTAMP,
    ALTER COLUMN update_time TYPE TIMESTAMP;

-- 8. 修改 fitness_booking 表
ALTER TABLE fitness_booking 
    ALTER COLUMN booking_time TYPE TIMESTAMP,
    ALTER COLUMN create_time TYPE TIMESTAMP,
    ALTER COLUMN update_time TYPE TIMESTAMP;

-- 9. 修改 fitness_equipment 表
ALTER TABLE fitness_equipment 
    ALTER COLUMN create_time TYPE TIMESTAMP,
    ALTER COLUMN update_time TYPE TIMESTAMP;

-- 10. 修改 fitness_equipment_repair 表
ALTER TABLE fitness_equipment_repair 
    ALTER COLUMN handle_time TYPE TIMESTAMP,
    ALTER COLUMN create_time TYPE TIMESTAMP,
    ALTER COLUMN update_time TYPE TIMESTAMP;

-- 11. 修改 fitness_plan 表
ALTER TABLE fitness_plan 
    ALTER COLUMN create_time TYPE TIMESTAMP,
    ALTER COLUMN update_time TYPE TIMESTAMP;

-- 12. 修改 fitness_plan_detail 表
ALTER TABLE fitness_plan_detail 
    ALTER COLUMN create_time TYPE TIMESTAMP,
    ALTER COLUMN update_time TYPE TIMESTAMP;

-- 13. 更新触发器函数中的时间类型
CREATE OR REPLACE FUNCTION update_update_time_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';
