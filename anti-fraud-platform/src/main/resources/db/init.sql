-- ============================================
-- 高校反诈防骗宣传平台 - 数据库初始化脚本
-- 数据库: MySQL 8.0.44
-- 字符集: utf8mb4
-- 作者: Anti-Fraud Platform Team
-- 日期: 2024-01-01
-- ============================================

CREATE DATABASE IF NOT EXISTS anti_fraud_platform 
DEFAULT CHARSET utf8mb4 
COLLATE utf8mb4_general_ci;

USE anti_fraud_platform;

-- ============================================
-- 1. 用户表 (sys_user)
-- ============================================
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
  username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  password VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
  real_name VARCHAR(50) COMMENT '真实姓名',
  phone VARCHAR(20) COMMENT '手机号',
  email VARCHAR(100) COMMENT '邮箱',
  avatar VARCHAR(255) COMMENT '头像URL',
  status TINYINT DEFAULT 1 COMMENT '状态:0禁用 1正常',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_username (username),
  INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================
-- 2. 角色表 (sys_role)
-- ============================================
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
  role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
  role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
  description VARCHAR(200) COMMENT '角色描述',
  status TINYINT DEFAULT 1 COMMENT '状态:0禁用 1正常',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ============================================
-- 3. 权限菜单表 (sys_permission)
-- ============================================
DROP TABLE IF EXISTS sys_permission;
CREATE TABLE sys_permission (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
  parent_id BIGINT DEFAULT 0 COMMENT '父级权限ID',
  permission_name VARCHAR(50) NOT NULL COMMENT '权限名称',
  permission_code VARCHAR(100) NOT NULL COMMENT '权限编码',
  menu_type TINYINT COMMENT '类型:1目录 2菜单 3按钮',
  path VARCHAR(200) COMMENT '路由路径',
  component VARCHAR(255) COMMENT '组件路径',
  icon VARCHAR(100) COMMENT '图标',
  sort_order INT DEFAULT 0 COMMENT '排序',
  status TINYINT DEFAULT 1 COMMENT '状态:0禁用 1正常',
  INDEX idx_parent_id (parent_id),
  INDEX idx_permission_code (permission_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限菜单表';

-- ============================================
-- 4. 用户角色关联表 (sys_user_role)
-- ============================================
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '用户ID',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  UNIQUE KEY uk_user_role (user_id, role_id),
  INDEX idx_user_id (user_id),
  INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- ============================================
-- 5. 角色权限关联表 (sys_role_permission)
-- ============================================
DROP TABLE IF EXISTS sys_role_permission;
CREATE TABLE sys_role_permission (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_id BIGINT NOT NULL COMMENT '角色ID',
  permission_id BIGINT NOT NULL COMMENT '权限ID',
  UNIQUE KEY uk_role_perm (role_id, permission_id),
  INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- ============================================
-- 6. 内容分类表 (content_category)
-- ============================================
DROP TABLE IF EXISTS content_category;
CREATE TABLE content_category (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  category_name VARCHAR(50) NOT NULL COMMENT '分类名称',
  parent_id BIGINT DEFAULT 0 COMMENT '父级分类ID',
  sort_order INT DEFAULT 0 COMMENT '排序',
  status TINYINT DEFAULT 1 COMMENT '状态:0禁用 1正常',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容分类表';

-- ============================================
-- 7. 反诈知识内容表 (anti_fraud_content)
-- ============================================
DROP TABLE IF EXISTS anti_fraud_content;
CREATE TABLE anti_fraud_content (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '内容ID',
  title VARCHAR(200) NOT NULL COMMENT '标题',
  content_type TINYINT NOT NULL COMMENT '类型:1文章 2视频 3漫画',
  content TEXT COMMENT '正文内容',
  cover_image VARCHAR(255) COMMENT '封面图片',
  video_url VARCHAR(500) COMMENT '视频地址(视频类型)',
  category_id BIGINT COMMENT '分类ID',
  author_id BIGINT COMMENT '作者ID',
  status TINYINT DEFAULT 0 COMMENT '状态:0待审核 1已上线 2已下架',
  view_count INT DEFAULT 0 COMMENT '浏览次数',
  like_count INT DEFAULT 0 COMMENT '点赞数',
  publish_time DATETIME COMMENT '发布时间',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_status (status),
  INDEX idx_category (category_id),
  INDEX idx_type (content_type),
  FULLTEXT INDEX ft_title_content (title, content)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='反诈知识内容表';

-- ============================================
-- 8. 测试题库表 (exam_question)
-- ============================================
DROP TABLE IF EXISTS exam_question;
CREATE TABLE exam_question (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '题目ID',
  question_text VARCHAR(500) NOT NULL COMMENT '题目标题',
  question_type TINYINT NOT NULL COMMENT '题型:1单选 2多选 3判断',
  category_id BIGINT COMMENT '分类ID',
  difficulty TINYINT DEFAULT 1 COMMENT '难度:1简单 2中等 3困难',
  option_a VARCHAR(200) COMMENT '选项A',
  option_b VARCHAR(200) COMMENT '选项B',
  option_c VARCHAR(200) COMMENT '选项C',
  option_d VARCHAR(200) COMMENT '选项D',
  answer VARCHAR(10) NOT NULL COMMENT '正确答案',
  analysis VARCHAR(500) COMMENT '解析',
  status TINYINT DEFAULT 1 COMMENT '状态:0禁用 1启用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_category (category_id),
  INDEX idx_difficulty (difficulty)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试题库表';

-- ============================================
-- 9. 测试记录表 (exam_record)
-- ============================================
DROP TABLE IF EXISTS exam_record;
CREATE TABLE exam_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '用户ID',
  total_score DECIMAL(5,2) COMMENT '总分',
  total_questions INT COMMENT '总题数',
  correct_count INT COMMENT '正确数',
  start_time DATETIME COMMENT '开始时间',
  submit_time DATETIME COMMENT '提交时间',
  duration INT COMMENT '用时(秒)',
  INDEX idx_user_id (user_id),
  INDEX idx_submit_time (submit_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试记录表';

-- ============================================
-- 10. 测试答题详情表 (exam_answer)
-- ============================================
DROP TABLE IF EXISTS exam_answer;
CREATE TABLE exam_answer (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  record_id BIGINT NOT NULL COMMENT '记录ID',
  question_id BIGINT NOT NULL COMMENT '题目ID',
  user_answer VARCHAR(10) COMMENT '用户答案',
  is_correct TINYINT COMMENT '是否正确:0错误 1正确',
  INDEX idx_record_id (record_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='答题详情表';

-- ============================================
-- 11. 举报线索表 (report_clue)
-- ============================================
DROP TABLE IF EXISTS report_clue;
CREATE TABLE report_clue (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  reporter_id BIGINT NOT NULL COMMENT '举报人ID',
  title VARCHAR(200) NOT NULL COMMENT '标题',
  fraud_type VARCHAR(50) COMMENT '诈骗类型',
  description TEXT COMMENT '详细描述',
  evidence_images VARCHAR(1000) COMMENT '证据图片(JSON数组)',
  contact_phone VARCHAR(20) COMMENT '联系电话',
  contact_name VARCHAR(50) COMMENT '联系人',
  status TINYINT DEFAULT 0 COMMENT '状态:0待处理 1处理中 2已解决 3已关闭',
  handler_id BIGINT COMMENT '处理人ID',
  handle_result TEXT COMMENT '处理结果',
  handle_time DATETIME COMMENT '处理时间',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_reporter (reporter_id),
  INDEX idx_status (status),
  INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报线索表';

-- ============================================
-- 12. 系统操作日志表 (sys_operation_log)
-- ============================================
DROP TABLE IF EXISTS sys_operation_log;
CREATE TABLE sys_operation_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT COMMENT '操作人ID',
  username VARCHAR(50) COMMENT '操作人用户名',
  operation VARCHAR(100) COMMENT '操作描述',
  method VARCHAR(200) COMMENT '请求方法',
  params TEXT COMMENT '请求参数',
  ip VARCHAR(50) COMMENT 'IP地址',
  status TINYINT COMMENT '状态:0失败 1成功',
  error_msg TEXT COMMENT '错误信息',
  operation_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  INDEX idx_user_id (user_id),
  INDEX idx_operation_time (operation_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ============================================
-- 初始数据插入
-- ============================================

-- 插入管理员账号 (密码: admin123, BCrypt加密)
INSERT INTO sys_user (username, password, real_name, phone, email, status) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOl7K/KKKhEje', '超级管理员', '13800138000', 'admin@antifraud.edu.cn', 1);

-- 插入测试普通用户
INSERT INTO sys_user (username, password, real_name, phone, status) VALUES 
('teacher01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOl7K/KKKhEje', '张老师', '13800138001', 1),
('student01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOl7K/KKKhEje', '李同学', '13800138002', 1);

-- 插入角色数据
INSERT INTO sys_role (role_name, role_code, description, status) VALUES 
('超级管理员', 'ADMIN', '拥有系统所有权限', 1),
('内容管理员', 'CONTENT_ADMIN', '负责内容审核与管理', 1),
('普通用户', 'USER', '基础浏览和测试权限', 1);

-- 分配管理员角色
INSERT INTO sys_user_role (user_id, role_id) VALUES 
(1, 1),
(2, 2),
(3, 3);

-- 插入权限菜单树形数据
INSERT INTO sys_permission (parent_id, permission_name, permission_code, menu_type, path, component, icon, sort_order, status) VALUES
(0, '系统管理', 'system', 1, '/system', NULL, 'setting', 1, 1),
(1, '用户管理', 'system:user', 2, '/system/user', 'system/user/index', 'user', 1, 1),
(1, '角色管理', 'system:role', 2, '/system/role', 'system/role/index', 'team', 2, 1),
(1, '权限管理', 'system:permission', 2, '/system/permission', 'system/permission/index', 'lock', 3, 1),
(0, '内容管理', 'content', 1, '/content', NULL, 'document', 2, 1),
(5, '知识库管理', 'content:knowledge', 2, '/content/knowledge', 'content/knowledge/index', 'reading', 1, 1),
(5, '分类管理', 'content:category', 2, '/content/category', 'content/category/index', 'menu', 2, 1),
(5, '题库管理', 'content:question', 2, '/content/question', 'content/question/index', 'edit', 3, 1),
(0, '测试中心', 'exam', 1, '/exam', NULL, 'edit-pen', 3, 1),
(9, '在线测试', 'exam:online', 2, '/exam/online', 'exam/online/index', 'notebook', 1, 1),
(9, '成绩查询', 'exam:record', 2, '/exam/record', 'exam/record/index', 'data-line', 2, 1),
(9, '成绩统计', 'exam:statistics', 2, '/exam/statistics', 'exam/statistics/index', 'data-analysis', 3, 1),
(0, '举报中心', 'report', 1, '/report', NULL, 'warning', 4, 1),
(13, '我的举报', 'report:mine', 2, '/report/mine', 'report/mine/index', 'message', 1, 1),
(13, '举报管理', 'report:manage', 2, '/report/manage', 'report/manage/index', 'list', 2, 1),
(13, '举报统计', 'report:statistics', 2, '/report/statistics', 'report/statistics/index', 'pie-chart', 3, 1);

-- 为超级管理员分配所有权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission WHERE status = 1;

-- 为内容管理员分配内容相关权限
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(2, 5), (2, 6), (2, 7), (2, 8);

-- 为普通用户分配基础权限
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(3, 10), (3, 11), (3, 14);

-- 插入内容分类
INSERT INTO content_category (category_name, parent_id, sort_order, status) VALUES
('电信诈骗', 0, 1, 1),
('网络诈骗', 0, 2, 1),
('金融诈骗', 0, 3, 1),
('兼职诈骗', 0, 4, 1),
('购物诈骗', 0, 5, 1),
('冒充公检法', 1, 1, 1),
('刷单诈骗', 1, 2, 1),
('贷款诈骗', 3, 1, 1),
('投资理财诈骗', 3, 2, 1);

-- 插入测试题目分类
INSERT INTO content_category (category_name, parent_id, sort_order, status) VALUES
('反诈基础知识', 0, 1, 1),
('案例分析', 0, 2, 1),
('防骗技巧', 0, 3, 1);

-- 插入示例测试题目
INSERT INTO exam_question (question_text, question_type, category_id, difficulty, option_a, option_b, option_c, option_d, answer, analysis, status) VALUES
('接到自称公检法的电话，要求转账到"安全账户"，应该怎么做？', 1, 13, 1, '立即转账', '挂断电话并报警', '提供银行卡信息', '按照要求操作', 'B', '公检法机关不会通过电话要求转账，这是典型的冒充公检法诈骗。应立即挂断并拨打110或96110报警。', 1),
('以下哪些是常见的网络诈骗手段？（多选）', 2, 13, 2, '冒充客服退款', '网络兼职刷单', '虚假投资理财', '以上都是', 'D', '这些都是当前高发的网络诈骗类型，需要提高警惕。', 1),
('只要不转账就不会被骗，这种说法对吗？', 3, 13, 1, '正确', '错误', '', '', 'B', '错误！诈骗分子可能通过获取验证码、诱导点击钓鱼链接等方式盗取资金，不一定需要主动转账。', 1),
('96110是全国统一的什么电话？', 1, 13, 1, '投诉热线', '反电信网络诈骗专用号码', '政务服务热线', '急救电话', 'B', '96110是公安部推出的反电信网络诈骗专用号码，用于预警劝阻、咨询和举报。', 1),
('收到"中奖通知"要求先交税金才能领奖，应该？', 1, 14, 2, '立即交税', '先核实官方渠道', '告诉亲朋好友', '忽略不管', 'B', '正规中奖不会要求提前支付任何费用，应通过官方渠道核实真实性。', 1);

SELECT '数据库初始化完成！' AS message;
SELECT CONCAT('已创建 ', COUNT(*), ' 张表') AS table_count FROM information_schema.tables WHERE table_schema = 'anti_fraud_platform';
SELECT CONCAT('已插入 ', COUNT(*), ' 条权限菜单') AS perm_count FROM sys_permission;
SELECT CONCAT('已插入 ', COUNT(*), ' 个用户') AS user_count FROM sys_user;
SELECT CONCAT('已插入 ', COUNT(*), ' 个角色') AS role_count FROM sys_role;
