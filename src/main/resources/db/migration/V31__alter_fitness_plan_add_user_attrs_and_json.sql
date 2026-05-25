-- 健身计划表扩展：增加用户身体属性字段和完整JSON数据存储
-- 用于会员端健身计划的保存和展示

ALTER TABLE fitness_plan
    ADD COLUMN IF NOT EXISTS height DECIMAL(5,2) DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS weight DECIMAL(5,2) DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS age INTEGER DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS gender VARCHAR(10) DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS experience VARCHAR(20) DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS fitness_goal VARCHAR(50) DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS plan_data_json JSONB DEFAULT NULL;

COMMENT ON COLUMN fitness_plan.height IS '身高(cm)';
COMMENT ON COLUMN fitness_plan.weight IS '体重(kg)';
COMMENT ON COLUMN fitness_plan.age IS '年龄';
COMMENT ON COLUMN fitness_plan.gender IS '性别';
COMMENT ON COLUMN fitness_plan.experience IS '健身经验';
COMMENT ON COLUMN fitness_plan.fitness_goal IS '健身目标';
COMMENT ON COLUMN fitness_plan.plan_data_json IS 'LLM返回的完整健身计划JSON数据';

CREATE INDEX IF NOT EXISTS idx_fitness_plan_user_time ON fitness_plan(user_id, create_time DESC);
