-- ========================================================
-- 重新初始化课程测试数据 - 6种课程类型
-- 类型：力量训练、有氧燃脂、瑜伽普拉提、拳击格斗、舞蹈操课、康复体态
-- 每种类型 3-7 个课程
-- 新增字段：difficulty_level(难度), duration_minutes(时长), calories_min/max(卡路里)
-- ========================================================

-- 获取教练ID
-- coach1: id=3, coach2: id=4

-- 1. 力量训练 (6个课程)
INSERT INTO fitness_course (course_name, description, coach_id, category, start_time, end_time, capacity, booked_count, status, image_url, deleted, create_time, update_time, difficulty_level, duration_minutes, calories_min, calories_max) VALUES
('杠铃深蹲特训', '系统学习杠铃深蹲技巧，强化下肢力量与核心稳定性', 3, '力量训练', CURRENT_TIMESTAMP + INTERVAL '1 day 9 hours', CURRENT_TIMESTAMP + INTERVAL '1 day 10 hours', 15, 8, 1, 'https://images.unsplash.com/photo-1534367507873-d2d7e24c797f?w=800', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '高级', 60, 400, 600),
('胸背超级组训练', '胸肌与背部肌群交替训练，打造完美上半身线条', 4, '力量训练', CURRENT_TIMESTAMP + INTERVAL '1 day 14 hours', CURRENT_TIMESTAMP + INTERVAL '1 day 15 hours', 12, 6, 1, 'https://images.unsplash.com/photo-1581009146145-b5ef050c149a?w=800', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '中级', 50, 350, 550),
('硬拉技术精讲', '从基础到进阶，掌握传统硬拉与相扑硬拉核心技术', 3, '力量训练', CURRENT_TIMESTAMP + INTERVAL '2 day 10 hours', CURRENT_TIMESTAMP + INTERVAL '2 day 11 hours', 10, 4, 1, 'https://images.unsplash.com/photo-1603287681836-e566f5c150e9?w=800', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '高级', 60, 450, 700),
('肩部塑形训练', '针对三角肌前中后束的专项训练，打造立体肩部', 4, '力量训练', CURRENT_TIMESTAMP + INTERVAL '2 day 16 hours', CURRENT_TIMESTAMP + INTERVAL '2 day 17 hours', 18, 12, 1, 'https://images.unsplash.com/photo-1583454110551-21f2fa2afe61?w=800', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '中级', 45, 300, 450),
('手臂力量轰炸', '二头肌与三头肌超级组训练，突破臂围瓶颈', 3, '力量训练', CURRENT_TIMESTAMP + INTERVAL '3 day 9 hours', CURRENT_TIMESTAMP + INTERVAL '3 day 10 hours', 20, 15, 1, 'https://images.unsplash.com/photo-1581009146145-b5ef050c149a?w=800', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '中级', 40, 250, 400),
('全身力量循环', '复合动作为主的力量循环训练，提升整体力量水平', 4, '力量训练', CURRENT_TIMESTAMP + INTERVAL '3 day 15 hours', CURRENT_TIMESTAMP + INTERVAL '3 day 16 hours 30 minutes', 16, 10, 1, 'https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=800', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '初级', 90, 500, 800);

-- 2. 有氧燃脂 (7个课程)
INSERT INTO fitness_course (course_name, description, coach_id, category, start_time, end_time, capacity, booked_count, status, image_url, deleted, create_time, update_time, difficulty_level, duration_minutes, calories_min, calories_max) VALUES
('燃脂动感单车', '高强度骑行训练，配合动感音乐，快速燃烧脂肪', 3, '有氧燃脂', CURRENT_TIMESTAMP + INTERVAL '1 day 18 hours', CURRENT_TIMESTAMP + INTERVAL '1 day 19 hours', 25, 18, 1, 'https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=800', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '中级', 45, 400, 600),
('HIIT极限挑战', '高强度间歇训练，20分钟高效燃脂，提升心肺功能', 4, '有氧燃脂', CURRENT_TIMESTAMP + INTERVAL '1 day 7 hours', CURRENT_TIMESTAMP + INTERVAL '1 day 7 hours 45 minutes', 30, 22, 1, 'https://images.unsplash.com/photo-1517963879466-e1b54ebd0642?w=800', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '高级', 45, 500, 700),
('跳绳燃脂课', '高强度跳绳训练，快速提升心率，燃烧卡路里', 3, '有氧燃脂', CURRENT_TIMESTAMP + INTERVAL '2 day 8 hours', CURRENT_TIMESTAMP + INTERVAL '2 day 8 hours 45 minutes', 20, 12, 1, 'https://images.unsplash.com/photo-1599058945522-28d584b6f0ff?w=800', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '中级', 45, 350, 500),
('搏击有氧操', '结合拳击动作的有氧运动，燃脂解压，提升协调性', 4, '有氧燃脂', CURRENT_TIMESTAMP + INTERVAL '2 day 19 hours', CURRENT_TIMESTAMP + INTERVAL '2 day 20 hours', 28, 20, 1, 'https://images.unsplash.com/photo-1549576490-b0b4831ef60a?w=800', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '中级', 60, 450, 650),
('阶梯踏板训练', '利用踏板进行有氧训练，锻炼心肺功能与腿部力量', 3, '有氧燃脂', CURRENT_TIMESTAMP + INTERVAL '3 day 18 hours', CURRENT_TIMESTAMP + INTERVAL '3 day 19 hours', 24, 16, 1, 'https://images.unsplash.com/photo-1518310383802-640c2de311b2?w=800', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '初级', 50, 300, 450),
('跑步训练营', '科学的跑步训练方法，提升耐力与燃脂效率', 4, '有氧燃脂', CURRENT_TIMESTAMP + INTERVAL '4 day 7 hours', CURRENT_TIMESTAMP + INTERVAL '4 day 8 hours', 20, 8, 1, 'https://images.unsplash.com/photo-1552674605-db6ffd4facb5?w=800', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '中级', 60, 400, 600),
('有氧舞蹈派对', '结合流行舞蹈的有氧运动，快乐燃脂不枯燥', 3, '有氧燃脂', CURRENT_TIMESTAMP + INTERVAL '4 day 19 hours', CURRENT_TIMESTAMP + INTERVAL '4 day 20 hours', 30, 25, 1, 'https://images.unsplash.com/photo-1524594152303-9fd13543fe6e?w=800', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '初级', 50, 350, 500);

-- 3. 瑜伽普拉提 (5个课程)
INSERT INTO fitness_course (course_name, description, coach_id, category, start_time, end_time, capacity, booked_count, status, image_url, deleted, create_time, update_time, difficulty_level, duration_minutes, calories_min, calories_max) VALUES
('晨间流瑜伽', '通过流畅的体式串联，唤醒身体能量，开启美好一天', 3, '瑜伽普拉提', CURRENT_TIMESTAMP + INTERVAL '1 day 6 hours', CURRENT_TIMESTAMP + INTERVAL '1 day 7 hours 15 minutes', 20, 15, 1, 'https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=800', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '初级', 75, 200, 350),
('普拉提核心床', '使用核心床进行普拉提训练，强化核心肌群', 4, '瑜伽普拉提', CURRENT_TIMESTAMP + INTERVAL '1 day 11 hours', CURRENT_TIMESTAMP + INTERVAL '1 day 12 hours', 12, 8, 1, 'https://images.unsplash.com/photo-1518310383802-640c2de311b2?w=800', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '中级', 60, 250, 400),
('阴瑜伽深度放松', '长时间保持体式，深度拉伸结缔组织，缓解压力', 3, '瑜伽普拉提', CURRENT_TIMESTAMP + INTERVAL '2 day 20 hours', CURRENT_TIMESTAMP + INTERVAL '2 day 21 hours 30 minutes', 18, 12, 1, 'https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=800', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '初级', 90, 150, 250),
('空中瑜伽入门', '利用吊床进行的瑜伽练习，增强核心与柔韧性', 4, '瑜伽普拉提', CURRENT_TIMESTAMP + INTERVAL '3 day 11 hours', CURRENT_TIMESTAMP + INTERVAL '3 day 12 hours', 10, 6, 1, 'https://images.unsplash.com/photo-1599901860904-17e6ed7083a0?w=800', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '中级', 60, 200, 350),
('垫上普拉提塑形', '经典垫上普拉提动作，塑造修长线条与优雅体态', 3, '瑜伽普拉提', CURRENT_TIMESTAMP + INTERVAL '4 day 10 hours', CURRENT_TIMESTAMP + INTERVAL '4 day 11 hours', 16, 10, 1, 'https://images.unsplash.com/photo-1575052814086-f385e2e2ad1b?w=800', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '初级', 50, 180, 300);

-- 4. 拳击格斗 (5个课程)
INSERT INTO fitness_course (course_name, description, coach_id, category, start_time, end_time, capacity, booked_count, status, image_url, deleted, create_time, update_time, difficulty_level, duration_minutes, calories_min, calories_max) VALUES
('拳击基础入门', '学习拳击基本拳法、步法与防守技巧', 4, '拳击格斗', CURRENT_TIMESTAMP + INTERVAL '1 day 13 hours', CURRENT_TIMESTAMP + INTERVAL '1 day 14 hours', 15, 10, 1, 'https://images.unsplash.com/photo-1549719386-74dfcbf7dbed?w=800', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '初级', 60, 400, 600),
('泰拳实战技巧', '学习泰拳基础动作，提升力量与反应速度', 3, '拳击格斗', CURRENT_TIMESTAMP + INTERVAL '2 day 13 hours', CURRENT_TIMESTAMP + INTERVAL '2 day 14 hours', 12, 7, 1, 'https://images.unsplash.com/photo-1599058945522-28d584b6f0ff?w=800', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '中级', 60, 450, 700),
('综合格斗训练', '融合拳击、踢拳、摔跤的综合格斗技术训练', 4, '拳击格斗', CURRENT_TIMESTAMP + INTERVAL '3 day 13 hours', CURRENT_TIMESTAMP + INTERVAL '3 day 14 hours', 10, 5, 1, 'https://images.unsplash.com/photo-1555597673-b21d5c935865?w=800', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '高级', 60, 500, 800),
('防身术实战', '实用的自我防卫技巧与情景模拟训练', 3, '拳击格斗', CURRENT_TIMESTAMP + INTERVAL '4 day 14 hours', CURRENT_TIMESTAMP + INTERVAL '4 day 15 hours', 20, 12, 1, 'https://images.unsplash.com/photo-1555597673-b21d5c935865?w=800', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '中级', 60, 350, 550),
('搏击体能强化', '专为搏击运动设计的体能训练，提升爆发力与耐力', 4, '拳击格斗', CURRENT_TIMESTAMP + INTERVAL '5 day 13 hours', CURRENT_TIMESTAMP + INTERVAL '5 day 14 hours', 15, 8, 1, 'https://images.unsplash.com/photo-1549719386-74dfcbf7dbed?w=800', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '高级', 60, 500, 750);

-- 5. 舞蹈操课 (4个课程)
INSERT INTO fitness_course (course_name, description, coach_id, category, start_time, end_time, capacity, booked_count, status, image_url, deleted, create_time, update_time, difficulty_level, duration_minutes, calories_min, calories_max) VALUES
('Zumba尊巴舞', '拉丁风格的有氧舞蹈，热情奔放，快乐燃脂', 3, '舞蹈操课', CURRENT_TIMESTAMP + INTERVAL '1 day 20 hours', CURRENT_TIMESTAMP + INTERVAL '1 day 21 hours', 30, 22, 1, 'https://images.unsplash.com/photo-1524594152303-9fd13543fe6e?w=800', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '初级', 50, 350, 500),
('爵士舞基础', '学习爵士舞基本动作与组合，提升身体协调性', 4, '舞蹈操课', CURRENT_TIMESTAMP + INTERVAL '2 day 17 hours', CURRENT_TIMESTAMP + INTERVAL '2 day 18 hours', 25, 15, 1, 'https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=800', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '初级', 60, 300, 450),
('街舞Hip-Hop', '学习Hip-Hop基础律动与舞步，释放青春活力', 3, '舞蹈操课', CURRENT_TIMESTAMP + INTERVAL '3 day 20 hours', CURRENT_TIMESTAMP + INTERVAL '3 day 21 hours', 28, 18, 1, 'https://images.unsplash.com/photo-1535525153412-5a42439a210d?w=800', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '中级', 60, 350, 500),
('民族健身操', '融合民族舞蹈元素的健身操，优美与健康兼具', 4, '舞蹈操课', CURRENT_TIMESTAMP + INTERVAL '4 day 17 hours', CURRENT_TIMESTAMP + INTERVAL '4 day 18 hours', 32, 20, 1, 'https://images.unsplash.com/photo-1518611012118-696072aa579a?w=800', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '初级', 45, 250, 400);

-- 6. 康复体态 (4个课程)
INSERT INTO fitness_course (course_name, description, coach_id, category, start_time, end_time, capacity, booked_count, status, image_url, deleted, create_time, update_time, difficulty_level, duration_minutes, calories_min, calories_max) VALUES
('肩颈舒缓康复', '针对肩颈僵硬的康复训练，缓解办公室综合症', 3, '康复体态', CURRENT_TIMESTAMP + INTERVAL '1 day 12 hours', CURRENT_TIMESTAMP + INTERVAL '1 day 13 hours', 20, 16, 1, 'https://images.unsplash.com/photo-1575052814086-f385e2e2ad1b?w=800', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '初级', 45, 150, 250),
('脊柱侧弯矫正', '通过专业训练改善脊柱侧弯，恢复身体平衡', 4, '康复体态', CURRENT_TIMESTAMP + INTERVAL '2 day 12 hours', CURRENT_TIMESTAMP + INTERVAL '2 day 13 hours', 15, 10, 1, 'https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=800', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '中级', 50, 180, 300),
('产后修复训练', '针对产后妈妈的盆底肌与腹直肌修复训练', 3, '康复体态', CURRENT_TIMESTAMP + INTERVAL '3 day 14 hours', CURRENT_TIMESTAMP + INTERVAL '3 day 15 hours', 12, 8, 1, 'https://images.unsplash.com/photo-1518310383802-640c2de311b2?w=800', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '初级', 45, 120, 200),
('体态矫正训练', '改善圆肩驼背、骨盆前倾等不良体态问题', 4, '康复体态', CURRENT_TIMESTAMP + INTERVAL '4 day 12 hours', CURRENT_TIMESTAMP + INTERVAL '4 day 13 hours', 18, 12, 1, 'https://images.unsplash.com/photo-1575052814086-f385e2e2ad1b?w=800', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, '初级', 50, 150, 250);
