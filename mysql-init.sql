-- ========================================================
-- mysql-init.sql
-- 由根目录 init.sql 转换而来，面向 MySQL 8 初始化执行
-- 保留原项目的 Flyway 多版本脚本，此文件仅作为汇总初始化脚本
-- 为提升兼容性，已移除 PostgreSQL 专属语法及 JSON 表达式默认值
-- ========================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '登录密码',
  `phone` VARCHAR(20) COMMENT '手机号',
  `email` VARCHAR(100) COMMENT '邮箱',
  `avatar` VARCHAR(500) COMMENT '头像URL',
  `status` SMALLINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `nickname` VARCHAR(50) COMMENT '用户昵称/姓名，用于展示',
  `balance` DECIMAL(10, 2) DEFAULT 0.00 COMMENT '账户余额'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
  `role_code` VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
  `description` VARCHAR(255) COMMENT '角色描述',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  CONSTRAINT `fk_sys_user_role_user_id` FOREIGN KEY (`user_id`) REFERENCES `sys_user`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_sys_user_role_role_id` FOREIGN KEY (`role_id`) REFERENCES `sys_role`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `permission_name` VARCHAR(50) NOT NULL COMMENT '权限名称',
  `permission_code` VARCHAR(100) NOT NULL UNIQUE COMMENT '权限编码',
  `type` SMALLINT NOT NULL DEFAULT 1 COMMENT '类型：1-菜单，2-按钮，3-接口',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父权限 ID',
  `path` VARCHAR(200) COMMENT '路由路径',
  `icon` VARCHAR(100) COMMENT '图标',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `permission_id` BIGINT NOT NULL COMMENT '权限ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  CONSTRAINT `fk_sys_role_permission_role_id` FOREIGN KEY (`role_id`) REFERENCES `sys_role`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_sys_role_permission_permission_id` FOREIGN KEY (`permission_id`) REFERENCES `sys_permission`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

DROP TABLE IF EXISTS `sys_file`;
CREATE TABLE `sys_file` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `file_name` VARCHAR(255) NOT NULL COMMENT '存储文件名',
  `original_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
  `file_url` VARCHAR(500) NOT NULL COMMENT '文件访问 URL',
  `file_type` VARCHAR(50) COMMENT '文件类型',
  `file_size` BIGINT DEFAULT 0 COMMENT '文件大小 (字节)',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` BIGINT COMMENT '创建人ID'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件记录表';

DROP TABLE IF EXISTS `fitness_course`;
CREATE TABLE `fitness_course` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `course_name` VARCHAR(100) NOT NULL COMMENT '课程名称',
  `description` TEXT COMMENT '课程描述',
  `coach_id` BIGINT NOT NULL COMMENT '教练 ID(关联 sys_user)',
  `category` VARCHAR(50) COMMENT '课程分类',
  `start_time` TIME NOT NULL COMMENT '开始时间（时分秒，如 14:00:00）',
  `end_time` TIME NOT NULL COMMENT '结束时间（时分秒，如 15:30:00）',
  `capacity` INT NOT NULL DEFAULT 20 COMMENT '容量 (最大人数)',
  `booked_count` INT NOT NULL DEFAULT 0 COMMENT '已预约人数',
  `status` SMALLINT NOT NULL DEFAULT 1 COMMENT '状态：0-已取消，1-可预约，2-已满员，3-已结束',
  `image_url` VARCHAR(500) COMMENT '课程图片 URL',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `difficulty_level` VARCHAR(20) DEFAULT '初级' COMMENT '难度等级：入门/初级/中级/高级/进阶',
  `duration_minutes` INT DEFAULT 60 COMMENT '课程时长（分钟）',
  `calories_min` INT DEFAULT 200 COMMENT '最小卡路里消耗',
  `calories_max` INT DEFAULT 400 COMMENT '最大卡路里消耗',
  `total_booking_count` INT DEFAULT 0 COMMENT '总预约人数（统计所有预约过该课程的独立会员数量）',
  `day_of_week` SMALLINT NOT NULL DEFAULT 1 COMMENT '星期几：1-周一, 2-周二, 3-周三, 4-周四, 5-周五, 6-周六, 7-周日',
  CONSTRAINT `fk_fitness_course_coach_id` FOREIGN KEY (`coach_id`) REFERENCES `sys_user`(`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程表';

DROP TABLE IF EXISTS `fitness_booking`;
CREATE TABLE `fitness_booking` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户 ID',
  `course_id` BIGINT NOT NULL COMMENT '课程 ID',
  `booking_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '预约时间',
  `status` SMALLINT NOT NULL DEFAULT 0 COMMENT '状态：0-待确认，1-已确认，2-已取消，3-已完成',
  `cancel_reason` VARCHAR(255) COMMENT '取消原因',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `session_id` BIGINT COMMENT '关联课程实例ID（周期性课程的某一次具体实例）',
  CONSTRAINT `fk_fitness_booking_user_id` FOREIGN KEY (`user_id`) REFERENCES `sys_user`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_fitness_booking_course_id` FOREIGN KEY (`course_id`) REFERENCES `fitness_course`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预约表';

DROP TABLE IF EXISTS `fitness_equipment`;
CREATE TABLE `fitness_equipment` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `equipment_name` VARCHAR(100) NOT NULL COMMENT '器材名称',
  `location` VARCHAR(200) COMMENT '存放位置',
  `status` SMALLINT NOT NULL DEFAULT 1 COMMENT '状态：0-维修中，1-正常，2-报废',
  `description` TEXT COMMENT '器材描述',
  `image_url` VARCHAR(500) COMMENT '器材图片 URL',
  `purchase_date` DATE COMMENT '购买日期',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `type_code` VARCHAR(50) COMMENT '器材类型编码，关联 fitness_equipment_type.type_code',
  `equipment_no` VARCHAR(100) COMMENT '器材编号',
  CONSTRAINT `uk_equipment_name` UNIQUE (`equipment_name`),
  CONSTRAINT `uk_fitness_equipment_no` UNIQUE (`equipment_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='器材表';

DROP TABLE IF EXISTS `fitness_equipment_repair`;
CREATE TABLE `fitness_equipment_repair` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT COMMENT '报修人 ID',
  `description` TEXT NOT NULL COMMENT '问题描述',
  `status` SMALLINT NOT NULL DEFAULT 0 COMMENT '状态：0-待处理，1-处理中，2-已完成',
  `handle_time` DATETIME COMMENT '处理完成时间',
  `handle_remark` VARCHAR(500) COMMENT '处理备注',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `equipment_id` BIGINT COMMENT '器材 ID',
  `image_urls` VARCHAR(2000) COMMENT '报修图片URL（多张图片以逗号分隔）',
  CONSTRAINT `fk_fitness_equipment_repair_user_id` FOREIGN KEY (`user_id`) REFERENCES `sys_user`(`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_repair_equipment` FOREIGN KEY (`equipment_id`) REFERENCES `fitness_equipment`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='器材报修表';

DROP TABLE IF EXISTS `fitness_plan`;
CREATE TABLE `fitness_plan` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户 ID',
  `plan_name` VARCHAR(100) NOT NULL COMMENT '计划名称',
  `goal` VARCHAR(255) COMMENT '健身目标',
  `duration` INT COMMENT '计划周期 (天)',
  `status` SMALLINT NOT NULL DEFAULT 1 COMMENT '状态：0-已停止，1-进行中',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `level` VARCHAR(50) COMMENT '难度等级: BEGINNER-初级, INTERMEDIATE-中级, ADVANCED-高级',
  `height` DECIMAL(5,2) DEFAULT NULL COMMENT '身高(cm)',
  `weight` DECIMAL(5,2) DEFAULT NULL COMMENT '体重(kg)',
  `age` INTEGER DEFAULT NULL COMMENT '年龄',
  `gender` VARCHAR(10) DEFAULT NULL COMMENT '性别',
  `experience` VARCHAR(20) DEFAULT NULL COMMENT '健身经验',
  `fitness_goal` VARCHAR(50) DEFAULT NULL COMMENT '健身目标',
  `plan_data_json` JSON DEFAULT NULL COMMENT 'LLM返回的完整健身计划JSON数据',
  CONSTRAINT `fk_fitness_plan_user_id` FOREIGN KEY (`user_id`) REFERENCES `sys_user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='健身计划表';

DROP TABLE IF EXISTS `fitness_plan_detail`;
CREATE TABLE `fitness_plan_detail` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `plan_id` BIGINT NOT NULL COMMENT '计划 ID',
  `day_of_week` SMALLINT NOT NULL COMMENT '星期几 (1-7)',
  `exercise_name` VARCHAR(100) NOT NULL COMMENT '运动名称',
  `sets` INT COMMENT '组数',
  `reps` INT COMMENT '每组次数',
  `duration` INT COMMENT '持续时间 (分钟)',
  `notes` VARCHAR(500) COMMENT '备注',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `day_index` SMALLINT COMMENT '天数索引(1-7)',
  `day_name` VARCHAR(20) COMMENT '星期名称(周一、周二等)',
  `focus` VARCHAR(100) COMMENT '训练重点(胸部、背部等)',
  `course_id` BIGINT COMMENT '课程ID',
  `course_name` VARCHAR(200) COMMENT '课程名称',
  `equipment_json` TEXT COMMENT '器械信息(JSON格式)',
  `exercises_json` TEXT COMMENT '训练动作(JSON格式)',
  CONSTRAINT `fk_fitness_plan_detail_plan_id` FOREIGN KEY (`plan_id`) REFERENCES `fitness_plan`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='健身计划详情表';

DROP TABLE IF EXISTS `fitness_equipment_type`;
CREATE TABLE `fitness_equipment_type` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `type_code` VARCHAR(50) NOT NULL UNIQUE COMMENT '器材类型编码',
  `type_name` VARCHAR(100) NOT NULL COMMENT '器材类型名称',
  `icon` VARCHAR(255) COMMENT '类型图标',
  `sort_order` INT DEFAULT 0 COMMENT '排序号',
  `description` TEXT COMMENT '器材类型描述',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '软删除标识',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='器材类型表';

DROP TABLE IF EXISTS `sys_banner`;
CREATE TABLE `sys_banner` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `title` VARCHAR(100) NOT NULL COMMENT '轮播图标题',
  `subtitle` VARCHAR(200) COMMENT '轮播图副标题',
  `image_url` VARCHAR(500) NOT NULL COMMENT '图片URL',
  `link` VARCHAR(500) COMMENT '跳转链接',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序顺序',
  `status` SMALLINT NOT NULL DEFAULT 1 COMMENT '状态：0-隐藏，1-显示',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='轮播图表';

DROP TABLE IF EXISTS `coach_detail`;
CREATE TABLE `coach_detail` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL UNIQUE COMMENT '用户ID，关联 sys_user',
  `personal_image_url` VARCHAR(500) COMMENT '个人展示图片URL',
  `tags` JSON COMMENT '教练标签JSON数组',
  `work_years` INT DEFAULT 0 COMMENT '从业年限',
  `specialties` JSON COMMENT '专业领域JSON数组',
  `teaching_style` VARCHAR(50) COMMENT '教学风格',
  `education` TEXT COMMENT '教育背景',
  `training` TEXT COMMENT '培训经历',
  `languages` JSON COMMENT '语言能力JSON数组',
  `bio` TEXT COMMENT '个人简介',
  `experience` TEXT COMMENT '教学经验',
  `honors` JSON COMMENT '获得荣誉JSON数组',
  `emergency_contact` JSON COMMENT '紧急联系人信息JSON对象',
  `certifications` JSON COMMENT '资格认证JSON数组',
  `availability` JSON COMMENT '可用时间段JSON对象',
  `student_count` INT DEFAULT 0 COMMENT '学员数量统计',
  `rating` VARCHAR(10) DEFAULT '99%' COMMENT '好评率',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `real_name` VARCHAR(50) COMMENT '教练真实姓名，用于展示',
  CONSTRAINT `fk_coach_detail_user_id` FOREIGN KEY (`user_id`) REFERENCES `sys_user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教练详情表';

DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(100) NOT NULL COMMENT '商品名称',
  `code` VARCHAR(50) UNIQUE COMMENT '商品编号',
  `category` VARCHAR(50) NOT NULL COMMENT '商品分类：EQUIPMENT-运动装备，SUPPLEMENT-营养补剂，COURSE-课程优惠，OTHER-其他',
  `image_url` VARCHAR(500) COMMENT '商品图片 URL',
  `description` TEXT COMMENT '商品描述',
  `original_price` DECIMAL(10, 2) NOT NULL DEFAULT 0 COMMENT '商品原价',
  `points_discount_type` VARCHAR(20) DEFAULT 'FIXED' COMMENT '积分抵扣类型：FIXED-固定金额，PERCENT-比例，NONE-不支持',
  `points_discount_value` DECIMAL(10, 2) DEFAULT 0 COMMENT '积分抵扣值 (固定金额或比例)',
  `max_points_discount` DECIMAL(10, 2) DEFAULT 0 COMMENT '最大积分抵扣金额',
  `stock` INTEGER NOT NULL DEFAULT 0 COMMENT '库存数量',
  `sales` INTEGER DEFAULT 0 COMMENT '销量',
  `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-上架，INACTIVE-下架',
  `is_hot` TINYINT(1) DEFAULT 0 COMMENT '是否热销',
  `is_new` TINYINT(1) DEFAULT 0 COMMENT '是否新品',
  `is_recommend` TINYINT(1) DEFAULT 0 COMMENT '是否推荐',
  `sort_order` INTEGER DEFAULT 0 COMMENT '排序',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `coach_id` BIGINT COMMENT '关联教练用户ID，仅私教套餐类商品使用'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

DROP TABLE IF EXISTS `product_order`;
CREATE TABLE `product_order` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `order_no` VARCHAR(50) UNIQUE NOT NULL COMMENT '订单编号',
  `user_id` BIGINT NOT NULL COMMENT '用户 ID',
  `product_id` BIGINT NOT NULL COMMENT '商品 ID',
  `product_name` VARCHAR(100) NOT NULL COMMENT '商品名称',
  `quantity` INTEGER NOT NULL DEFAULT 1 COMMENT '购买数量',
  `original_price` DECIMAL(10, 2) NOT NULL COMMENT '商品原价',
  `points_used` INTEGER DEFAULT 0 COMMENT '使用积分数量',
  `points_discount` DECIMAL(10, 2) DEFAULT 0 COMMENT '积分抵扣金额',
  `final_price` DECIMAL(10, 2) NOT NULL COMMENT '最终支付价格',
  `pay_amount` DECIMAL(10, 2) NOT NULL COMMENT '实际支付金额',
  `pay_method` VARCHAR(50) COMMENT '支付方式',
  `pay_time` DATETIME COMMENT '支付时间',
  `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '订单状态：PENDING-待支付，PAID-已支付，PROCESSING-处理中，SHIPPED-已发货，COMPLETED-已完成，CANCELLED-已取消',
  `tracking_no` VARCHAR(100) COMMENT '物流单号',
  `carrier` VARCHAR(50) COMMENT '物流公司',
  `address` TEXT COMMENT '收货地址',
  `remark` TEXT COMMENT '备注',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `coach_id` BIGINT COMMENT '关联教练用户ID，仅私教套餐订单使用'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品订单表';

DROP TABLE IF EXISTS `user_fitness_profile`;
CREATE TABLE `user_fitness_profile` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL UNIQUE COMMENT '用户 ID',
  `height` DECIMAL(5,2) COMMENT '身高 (cm)',
  `weight` DECIMAL(5,2) COMMENT '体重 (kg)',
  `age` INTEGER COMMENT '年龄',
  `experience` VARCHAR(20) COMMENT '健身经验：BEGINNER-初学者，INTERMEDIATE-中级，ADVANCED-高级',
  `fitness_goal` VARCHAR(20) COMMENT '健身目标：WEIGHT_LOSS-减脂，MUSCLE_GAIN-增肌，BODY_SHAPING-塑形，ENDURANCE-增强体能，HEALTH-保持健康',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `gender` VARCHAR(10) COMMENT '性别：MALE-男，FEMALE-女',
  `private_coach_id` BIGINT COMMENT '专属教练ID（关联sys_user表）',
  CONSTRAINT `fk_user_fitness_profile_user_id` FOREIGN KEY (`user_id`) REFERENCES `sys_user`(`id`),
  CONSTRAINT `fk_user_fitness_profile_private_coach_id` FOREIGN KEY (`private_coach_id`) REFERENCES `sys_user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户健身档案表';

DROP TABLE IF EXISTS `sys_announcement`;
CREATE TABLE `sys_announcement` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `title` VARCHAR(200) NOT NULL COMMENT '公告标题',
  `content` TEXT NOT NULL COMMENT '公告内容',
  `type` VARCHAR(50) NOT NULL DEFAULT 'SYSTEM' COMMENT '公告类型：SYSTEM-系统公告, ACTIVITY-活动通知, IMPORTANT-重要提醒',
  `status` SMALLINT NOT NULL DEFAULT 0 COMMENT '状态：0-草稿，1-已发布',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览量',
  `publish_time` DATETIME COMMENT '发布时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';

DROP TABLE IF EXISTS `chat_session`;
CREATE TABLE `chat_session` (
  `id` BIGINT PRIMARY KEY COMMENT '会话ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `title` VARCHAR(255) COMMENT '会话标题',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='智能客服对话会话表';

DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message` (
  `id` BIGINT PRIMARY KEY COMMENT '消息ID',
  `session_id` BIGINT NOT NULL COMMENT '会话ID',
  `role` VARCHAR(20) NOT NULL COMMENT '角色：user/assistant',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='智能客服对话消息表';

DROP TABLE IF EXISTS `knowledge_category`;
CREATE TABLE `knowledge_category` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `code` VARCHAR(50) NOT NULL UNIQUE COMMENT '分类编码',
  `description` VARCHAR(255) COMMENT '分类描述',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库分类表';

DROP TABLE IF EXISTS `knowledge_document`;
CREATE TABLE `knowledge_document` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `title` VARCHAR(200) NOT NULL COMMENT '文档标题',
  `file_url` VARCHAR(500) COMMENT 'MinIO文件URL',
  `file_name` VARCHAR(255) COMMENT '原始文件名',
  `file_type` VARCHAR(50) COMMENT '文件类型(md/txt)',
  `file_size` BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
  `status` SMALLINT NOT NULL DEFAULT 0 COMMENT '状态：0-草稿，1-已发布，2-已归档',
  `chunk_count` INT DEFAULT 0 COMMENT '切片数量',
  `create_by` BIGINT COMMENT '创建人ID',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `category_id` BIGINT COMMENT '分类ID，关联 knowledge_category 表',
  CONSTRAINT `fk_knowledge_document_category_id` FOREIGN KEY (`category_id`) REFERENCES `knowledge_category`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库文档表';

DROP TABLE IF EXISTS `knowledge_chunk`;
CREATE TABLE `knowledge_chunk` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `document_id` BIGINT NOT NULL COMMENT '关联文档ID',
  `chunk_index` INT NOT NULL COMMENT '切片序号(从0开始)',
  `content` TEXT NOT NULL COMMENT '切片文本内容',
  `content_hash` VARCHAR(64) COMMENT '内容哈希(MD5)',
  `embedding` JSON COMMENT '向量嵌入(768维)',
  `metadata` JSON COMMENT '元数据(JSON格式)',
  `char_count` INT DEFAULT 0 COMMENT '字符数',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  CONSTRAINT `fk_knowledge_chunk_document_id` FOREIGN KEY (`document_id`) REFERENCES `knowledge_document`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库切片表';

DROP TABLE IF EXISTS `fitness_repair_record`;
CREATE TABLE `fitness_repair_record` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `repair_id` BIGINT NOT NULL COMMENT '报修ID',
  `handler_id` BIGINT COMMENT '处理人ID（管理员）',
  `record_type` SMALLINT NOT NULL DEFAULT 1 COMMENT '处理类型：1-提交报修, 2-状态变更, 3-处理备注, 4-取消报修',
  `before_status` SMALLINT COMMENT '处理前状态',
  `after_status` SMALLINT COMMENT '处理后状态',
  `content` TEXT COMMENT '处理内容/备注',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  CONSTRAINT `fk_fitness_repair_record_repair_id` FOREIGN KEY (`repair_id`) REFERENCES `fitness_equipment_repair`(`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_fitness_repair_record_handler_id` FOREIGN KEY (`handler_id`) REFERENCES `sys_user`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报修处理记录表';

DROP TABLE IF EXISTS `analysis_report`;
CREATE TABLE `analysis_report` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `report_title` VARCHAR(255) NOT NULL COMMENT '报告标题',
  `analysis_type` VARCHAR(50) NOT NULL COMMENT '分析类型',
  `report_content` TEXT NOT NULL COMMENT '报告内容',
  `suggestions` TEXT COMMENT '分析建议（Markdown文本）',
  `generate_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT COMMENT '创建人ID',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI数据分析报告表';

DROP TABLE IF EXISTS `membership_card_type`;
CREATE TABLE `membership_card_type` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(100) NOT NULL COMMENT '类型名称，如：月卡、季卡、年卡',
  `code` VARCHAR(50) UNIQUE NOT NULL COMMENT '类型编码',
  `description` TEXT COMMENT '类型描述',
  `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-启用，INACTIVE-禁用',
  `sort_order` INT DEFAULT 0 COMMENT '排序号',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员卡类型表';

DROP TABLE IF EXISTS `membership_card`;
CREATE TABLE `membership_card` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `type_id` BIGINT NOT NULL COMMENT '会员卡类型ID',
  `name` VARCHAR(200) NOT NULL COMMENT '会员卡名称',
  `subtitle` VARCHAR(500) COMMENT '副标题/简介',
  `price` DECIMAL(10, 2) NOT NULL COMMENT '售价',
  `original_price` DECIMAL(10, 2) COMMENT '原价',
  `duration_days` INT NOT NULL COMMENT '有效期天数',
  `points_reward` INT DEFAULT 0 COMMENT '购买赠送积分',
  `daily_price` DECIMAL(10, 2) COMMENT '日均价格（用于展示）',
  `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-上架，INACTIVE-下架',
  `is_recommend` TINYINT(1) DEFAULT 0 COMMENT '是否推荐',
  `sort_order` INT DEFAULT 0 COMMENT '排序号',
  `cover_image` VARCHAR(500) COMMENT '封面图片URL',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `type_code` VARCHAR(50) COMMENT '会员卡类型编码（冗余字段，便于查询）',
  CONSTRAINT `fk_membership_card_type` FOREIGN KEY (`type_id`) REFERENCES `membership_card_type`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员卡定义表';

DROP TABLE IF EXISTS `membership_card_content`;
CREATE TABLE `membership_card_content` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `card_id` BIGINT NOT NULL COMMENT '会员卡ID',
  `content_type` VARCHAR(50) NOT NULL COMMENT '内容类型：BENEFIT-权益说明, RULE-使用规则, PRIVILEGE-特权列表, OTHER-其他',
  `title` VARCHAR(200) NOT NULL COMMENT '标题',
  `description` TEXT COMMENT '详细描述',
  `icon` VARCHAR(200) COMMENT '图标',
  `sort_order` INT DEFAULT 0 COMMENT '排序号',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  CONSTRAINT `fk_card_content_card` FOREIGN KEY (`card_id`) REFERENCES `membership_card`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员卡内容项表';

DROP TABLE IF EXISTS `membership_order`;
CREATE TABLE `membership_order` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `order_no` VARCHAR(64) UNIQUE NOT NULL COMMENT '订单编号',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `card_id` BIGINT NOT NULL COMMENT '会员卡ID',
  `card_name` VARCHAR(200) COMMENT '会员卡名称（快照）',
  `price` DECIMAL(10, 2) NOT NULL COMMENT '订单金额',
  `pay_amount` DECIMAL(10, 2) COMMENT '实际支付金额',
  `pay_method` VARCHAR(50) COMMENT '支付方式：ALIPAY-支付宝, BALANCE-余额',
  `pay_time` DATETIME COMMENT '支付时间',
  `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING-待支付, PAID-已支付, CANCELLED-已取消, TIMEOUT-已超时',
  `expire_time` DATETIME COMMENT '会员到期时间',
  `alipay_trade_no` VARCHAR(128) COMMENT '支付宝交易号',
  `remark` VARCHAR(500) COMMENT '备注',
  `create_time` DATETIME COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  CONSTRAINT `fk_order_card` FOREIGN KEY (`card_id`) REFERENCES `membership_card`(`id`),
  CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员卡订单表';

DROP TABLE IF EXISTS `user_membership`;
CREATE TABLE `user_membership` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNIQUE NOT NULL COMMENT '用户ID',
  `membership_type` VARCHAR(50) COMMENT '当前会员类型',
  `start_time` DATETIME COMMENT '会员开始时间',
  `expire_time` DATETIME COMMENT '会员到期时间',
  `is_active` TINYINT(1) DEFAULT 0 COMMENT '是否有效',
  `total_orders` INT DEFAULT 0 COMMENT '累计购买次数',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  CONSTRAINT `fk_user_membership_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户会员信息表';

DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `dict_name` VARCHAR(100) NOT NULL COMMENT '字典名称',
  `dict_code` VARCHAR(100) UNIQUE NOT NULL COMMENT '字典编码（唯一标识）',
  `description` TEXT COMMENT '描述',
  `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-启用，INACTIVE-禁用',
  `sort_order` INT DEFAULT 0 COMMENT '排序号',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据字典表';

DROP TABLE IF EXISTS `sys_dict_item`;
CREATE TABLE `sys_dict_item` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `dict_id` BIGINT NOT NULL COMMENT '所属字典ID',
  `label` VARCHAR(200) NOT NULL COMMENT '显示名称',
  `value` VARCHAR(200) NOT NULL COMMENT '实际值',
  `description` TEXT COMMENT '描述',
  `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-启用，INACTIVE-禁用',
  `sort_order` INT DEFAULT 0 COMMENT '排序号',
  `extra` JSON COMMENT '扩展属性（JSON格式）',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  CONSTRAINT `fk_dict_item_dict` FOREIGN KEY (`dict_id`) REFERENCES `sys_dict`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据字典项表';

DROP TABLE IF EXISTS `chat_long_term_memory`;
CREATE TABLE `chat_long_term_memory` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `memory_key` VARCHAR(100) NOT NULL COMMENT '记忆唯一键',
  `memory_type` VARCHAR(50) NOT NULL COMMENT '记忆类型',
  `content` TEXT NOT NULL COMMENT '记忆内容',
  `metadata` JSON NOT NULL COMMENT '扩展元数据(JSON格式)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE (`user_id`, `memory_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='健小助长期记忆表';

DROP TABLE IF EXISTS `fitness_video_course`;
CREATE TABLE `fitness_video_course` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `title` VARCHAR(200) NOT NULL COMMENT '视频标题',
  `description` TEXT COMMENT '视频描述',
  `category` VARCHAR(100) COMMENT '分类',
  `cover_url` VARCHAR(500) COMMENT '封面图片URL',
  `video_url` VARCHAR(500) NOT NULL COMMENT '视频文件URL',
  `duration_seconds` INT DEFAULT 0 COMMENT '视频时长(秒)',
  `file_size` BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
  `difficulty_level` VARCHAR(20) COMMENT '难度等级',
  `coach_id` BIGINT COMMENT '教练ID',
  `status` SMALLINT NOT NULL DEFAULT 1 COMMENT '状态：0-下架，1-上架',
  `view_count` INT DEFAULT 0 COMMENT '观看次数',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频课程表';

DROP TABLE IF EXISTS `fitness_course_session`;
CREATE TABLE `fitness_course_session` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `course_id` BIGINT NOT NULL COMMENT '关联课程模板ID',
  `session_date` DATE NOT NULL COMMENT '本次上课的具体日期',
  `day_of_week` SMALLINT NOT NULL COMMENT '星期几（冗余）',
  `start_time` TIME NOT NULL COMMENT '开始时间（时分秒，冗余）',
  `end_time` TIME NOT NULL COMMENT '结束时间（时分秒，冗余）',
  `status` SMALLINT NOT NULL DEFAULT 0 COMMENT '状态：0-待开始, 1-进行中, 2-已结束, 3-已取消',
  `capacity` INT NOT NULL COMMENT '本次最大容量',
  `booked_count` INT NOT NULL DEFAULT 0 COMMENT '本次已预约数',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标识'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程周实例表（周期性课程的具体某一次）';

ALTER TABLE `sys_user` ADD INDEX `idx_sys_user_username` (username);
ALTER TABLE `sys_user` ADD INDEX `idx_sys_user_phone` (phone);
ALTER TABLE `sys_user` ADD INDEX `idx_sys_user_status` (status);
ALTER TABLE `sys_user` ADD INDEX `idx_sys_user_deleted` (deleted);
ALTER TABLE `sys_role` ADD INDEX `idx_sys_role_role_code` (role_code);
ALTER TABLE `sys_role` ADD INDEX `idx_sys_role_deleted` (deleted);
ALTER TABLE `sys_user_role` ADD INDEX `idx_sys_user_role_user_id` (user_id);
ALTER TABLE `sys_user_role` ADD INDEX `idx_sys_user_role_role_id` (role_id);
ALTER TABLE `sys_permission` ADD INDEX `idx_sys_permission_parent_id` (parent_id);
ALTER TABLE `sys_permission` ADD INDEX `idx_sys_permission_type` (type);
ALTER TABLE `sys_permission` ADD INDEX `idx_sys_permission_deleted` (deleted);
ALTER TABLE `sys_role_permission` ADD INDEX `idx_sys_role_permission_role_id` (role_id);
ALTER TABLE `sys_role_permission` ADD INDEX `idx_sys_role_permission_permission_id` (permission_id);
ALTER TABLE `fitness_course` ADD INDEX `idx_fitness_course_coach_id` (coach_id);
ALTER TABLE `fitness_course` ADD INDEX `idx_fitness_course_category` (category);
ALTER TABLE `fitness_course` ADD INDEX `idx_fitness_course_status` (status);
ALTER TABLE `fitness_course` ADD INDEX `idx_fitness_course_start_time` (start_time);
ALTER TABLE `fitness_course` ADD INDEX `idx_fitness_course_deleted` (deleted);
ALTER TABLE `fitness_booking` ADD INDEX `idx_fitness_booking_user_id` (user_id);
ALTER TABLE `fitness_booking` ADD INDEX `idx_fitness_booking_course_id` (course_id);
ALTER TABLE `fitness_booking` ADD INDEX `idx_fitness_booking_status` (status);
ALTER TABLE `fitness_booking` ADD INDEX `idx_fitness_booking_deleted` (deleted);
ALTER TABLE `fitness_equipment` ADD INDEX `idx_fitness_equipment_status` (status);
ALTER TABLE `fitness_equipment` ADD INDEX `idx_fitness_equipment_deleted` (deleted);
ALTER TABLE `fitness_equipment_repair` ADD INDEX `idx_fitness_equipment_repair_equipment_id` (equipment_id);
ALTER TABLE `fitness_equipment_repair` ADD INDEX `idx_fitness_equipment_repair_user_id` (user_id);
ALTER TABLE `fitness_equipment_repair` ADD INDEX `idx_fitness_equipment_repair_status` (status);
ALTER TABLE `fitness_equipment_repair` ADD INDEX `idx_fitness_equipment_repair_deleted` (deleted);
ALTER TABLE `fitness_plan` ADD INDEX `idx_fitness_plan_user_id` (user_id);
ALTER TABLE `fitness_plan` ADD INDEX `idx_fitness_plan_status` (status);
ALTER TABLE `fitness_plan` ADD INDEX `idx_fitness_plan_deleted` (deleted);
ALTER TABLE `fitness_plan_detail` ADD INDEX `idx_fitness_plan_detail_plan_id` (plan_id);
ALTER TABLE `fitness_plan_detail` ADD INDEX `idx_fitness_plan_detail_deleted` (deleted);
ALTER TABLE `sys_file` ADD INDEX `idx_sys_file_file_name` (file_name);
ALTER TABLE `sys_file` ADD INDEX `idx_sys_file_deleted` (deleted);
ALTER TABLE `sys_file` ADD INDEX `idx_sys_file_create_by` (create_by);
ALTER TABLE `sys_file` ADD INDEX `idx_sys_file_create_time` (create_time);
ALTER TABLE `fitness_course` ADD INDEX `idx_fitness_course_total_booking_count` (total_booking_count);
ALTER TABLE `sys_banner` ADD INDEX `idx_sys_banner_status` (status);
ALTER TABLE `sys_banner` ADD INDEX `idx_sys_banner_sort_order` (sort_order);
ALTER TABLE `sys_banner` ADD INDEX `idx_sys_banner_deleted` (deleted);
ALTER TABLE `coach_detail` ADD INDEX `idx_coach_detail_user_id` (user_id);
ALTER TABLE `coach_detail` ADD INDEX `idx_coach_detail_deleted` (deleted);
ALTER TABLE `product_order` ADD INDEX `idx_product_order_user_id` (user_id);
ALTER TABLE `product_order` ADD INDEX `idx_product_order_status` (status);
ALTER TABLE `product_order` ADD INDEX `idx_product_order_created_at` (created_at);
ALTER TABLE `sys_announcement` ADD INDEX `idx_sys_announcement_type` (type);
ALTER TABLE `sys_announcement` ADD INDEX `idx_sys_announcement_status` (status);
ALTER TABLE `sys_announcement` ADD INDEX `idx_sys_announcement_deleted` (deleted);
ALTER TABLE `sys_announcement` ADD INDEX `idx_sys_announcement_publish_time` (publish_time);
ALTER TABLE `chat_session` ADD INDEX `idx_chat_session_user_id` (user_id);
ALTER TABLE `chat_session` ADD INDEX `idx_chat_session_created_at` (created_at);
ALTER TABLE `chat_message` ADD INDEX `idx_chat_message_session_id` (session_id);
ALTER TABLE `chat_message` ADD INDEX `idx_chat_message_created_at` (created_at);
ALTER TABLE `knowledge_category` ADD INDEX `idx_knowledge_category_code` (code);
ALTER TABLE `knowledge_category` ADD INDEX `idx_knowledge_category_deleted` (deleted);
ALTER TABLE `knowledge_document` ADD INDEX `idx_knowledge_document_category_id` (category_id);
ALTER TABLE `knowledge_document` ADD INDEX `idx_knowledge_document_status` (status);
ALTER TABLE `knowledge_document` ADD INDEX `idx_knowledge_document_create_by` (create_by);
ALTER TABLE `knowledge_document` ADD INDEX `idx_knowledge_document_deleted` (deleted);
ALTER TABLE `knowledge_document` ADD INDEX `idx_knowledge_document_create_time` (create_time);
ALTER TABLE `knowledge_chunk` ADD INDEX `idx_knowledge_chunk_document_id` (document_id);
ALTER TABLE `knowledge_chunk` ADD INDEX `idx_knowledge_chunk_content_hash` (content_hash);
-- idx_knowledge_chunk_embedding: PostgreSQL ??????? MySQL ?????????
ALTER TABLE `knowledge_chunk` ADD FULLTEXT INDEX `idx_knowledge_chunk_content_fts` (`content`);
ALTER TABLE `fitness_repair_record` ADD INDEX `idx_fitness_repair_record_repair_id` (repair_id);
ALTER TABLE `fitness_repair_record` ADD INDEX `idx_fitness_repair_record_handler_id` (handler_id);
ALTER TABLE `fitness_repair_record` ADD INDEX `idx_fitness_repair_record_deleted` (deleted);
ALTER TABLE `fitness_plan_detail` ADD INDEX `idx_fitness_plan_detail_day_index` (day_index);
ALTER TABLE `analysis_report` ADD INDEX `idx_analysis_report_type` (analysis_type);
ALTER TABLE `analysis_report` ADD INDEX `idx_analysis_report_generate_time` (generate_time );
ALTER TABLE `analysis_report` ADD INDEX `idx_analysis_report_create_by` (create_by);
ALTER TABLE `analysis_report` ADD INDEX `idx_analysis_report_is_deleted` (is_deleted);
ALTER TABLE `fitness_plan` ADD INDEX `idx_fitness_plan_user_time` (user_id, create_time );
ALTER TABLE `sys_user` ADD INDEX `idx_sys_user_nickname` (nickname);
ALTER TABLE `fitness_equipment_repair` ADD INDEX `idx_repair_equipment_id` (equipment_id);
ALTER TABLE `membership_card` ADD INDEX `idx_membership_card_type_id` (type_id);
ALTER TABLE `membership_card` ADD INDEX `idx_membership_card_status` (status);
ALTER TABLE `membership_card_content` ADD INDEX `idx_card_content_card_id` (card_id);
ALTER TABLE `membership_order` ADD INDEX `idx_membership_order_user_id` (user_id);
ALTER TABLE `membership_order` ADD INDEX `idx_membership_order_status` (status);
ALTER TABLE `membership_order` ADD INDEX `idx_membership_order_no` (order_no);
ALTER TABLE `sys_user` ADD INDEX `idx_user_balance` (balance);
ALTER TABLE `sys_dict` ADD INDEX `idx_sys_dict_code` (dict_code);
ALTER TABLE `sys_dict` ADD INDEX `idx_sys_dict_status` (status);
ALTER TABLE `sys_dict_item` ADD INDEX `idx_dict_item_dict_id` (dict_id);
ALTER TABLE `sys_dict_item` ADD INDEX `idx_dict_item_value` (value);
ALTER TABLE `membership_card` ADD INDEX `idx_membership_card_type_code` (type_code);
ALTER TABLE `chat_long_term_memory` ADD INDEX `idx_chat_long_term_memory_user_id` (user_id);
ALTER TABLE `chat_long_term_memory` ADD INDEX `idx_chat_long_term_memory_type` (memory_type);
ALTER TABLE `fitness_video_course` ADD INDEX `idx_video_course_category` (category);
ALTER TABLE `fitness_video_course` ADD INDEX `idx_video_course_coach_id` (coach_id);
ALTER TABLE `fitness_video_course` ADD INDEX `idx_video_course_status` (status);
ALTER TABLE `fitness_course_session` ADD INDEX `idx_session_course_id` (course_id);
ALTER TABLE `fitness_course_session` ADD INDEX `idx_session_date` (session_date);
-- idx_session_unique: PostgreSQL partial unique index converted to a composite unique index including deleted.
ALTER TABLE `fitness_course_session` ADD UNIQUE INDEX `idx_session_unique` (course_id, session_date, `deleted`);
ALTER TABLE `fitness_booking` ADD INDEX `idx_booking_session_id` (session_id);
ALTER TABLE `user_fitness_profile` ADD INDEX `idx_user_fitness_profile_user_id` (user_id);
ALTER TABLE `user_fitness_profile` ADD INDEX `idx_user_fitness_profile_private_coach_id` (private_coach_id);
ALTER TABLE `coach_detail` ADD INDEX `idx_coach_detail_real_name` (real_name);

SET FOREIGN_KEY_CHECKS = 1;
