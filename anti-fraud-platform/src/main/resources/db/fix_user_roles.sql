-- 修复数据库初始数据
-- 确保用户角色关联正确，密码正确

USE anti_fraud_platform;

-- ============================================
-- 1. 清理旧数据（可选，如果需要重新初始化）
-- ============================================
-- 注意：以下操作会删除所有数据，请谨慎使用！
-- DELETE FROM sys_user_role;
-- DELETE FROM sys_role_permission;
-- DELETE FROM sys_user;
-- DELETE FROM sys_role;
-- DELETE FROM sys_permission;
-- DELETE FROM content_category;
-- DELETE FROM anti_fraud_content;
-- DELETE FROM exam_question;

-- ============================================
-- 2. 确保用户表数据正确
-- ============================================

-- 删除现有用户（如果有重复用户名）
DELETE FROM sys_user WHERE username IN ('admin', 'teacher01', 'student01');

-- 插入正确的用户数据
-- BCrypt加密后的密码都是 "admin123"
INSERT INTO sys_user (id, username, password, real_name, phone, email, status) VALUES 
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOl7K/KKKhEje', '超级管理员', '13800138000', 'admin@antifraud.edu.cn', 1),
(2, 'teacher01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOl7K/KKKhEje', '张老师', '13800138001', 'teacher@antifraud.edu.cn', 1),
(3, 'student01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOl7K/KKKhEje', '李同学', '13800138002', 'student@antifraud.edu.cn', 1);

-- ============================================
-- 3. 确保角色表数据正确
-- ============================================

-- 删除现有角色（如果有重复）
DELETE FROM sys_role WHERE role_code IN ('ADMIN', 'CONTENT_ADMIN', 'USER');

-- 插入角色数据
INSERT INTO sys_role (id, role_name, role_code, description, status) VALUES 
(1, '超级管理员', 'ADMIN', '拥有系统所有权限', 1),
(2, '内容管理员', 'CONTENT_ADMIN', '负责内容审核与管理', 1),
(3, '普通用户', 'USER', '基础浏览和测试权限', 1);

-- ============================================
-- 4. 清理用户角色关联表
-- ============================================
DELETE FROM sys_user_role;

-- ============================================
-- 5. 正确分配用户角色关联
-- ============================================
-- admin (user_id=1) → ADMIN (role_id=1)  超级管理员
-- teacher01 (user_id=2) → CONTENT_ADMIN (role_id=2)  内容管理员
-- student01 (user_id=3) → USER (role_id=3)  普通用户
INSERT INTO sys_user_role (user_id, role_id) VALUES 
(1, 1),  -- admin 分配 ADMIN 角色
(2, 2),  -- teacher01 分配 CONTENT_ADMIN 角色
(3, 3);  -- student01 分配 USER 角色

-- ============================================
-- 6. 验证数据
-- ============================================
SELECT '=== 用户列表 ===' AS '';
SELECT id, username, real_name, status FROM sys_user ORDER BY id;

SELECT '=== 角色列表 ===' AS '';
SELECT id, role_name, role_code, status FROM sys_role ORDER BY id;

SELECT '=== 用户角色关联 ===' AS '';
SELECT u.username, r.role_name, r.role_code 
FROM sys_user_role ur
JOIN sys_user u ON ur.user_id = u.id
JOIN sys_role r ON ur.role_id = r.id
ORDER BY u.id;

-- ============================================
-- 7. 测试密码验证（可选）
-- ============================================
-- 在Java代码中，BCrypt验证 "admin123" 应该与上面的密码匹配
-- 如果登录失败，可以运行以下命令重新生成密码：

-- SELECT username, password FROM sys_user WHERE username = 'admin';
-- 预期结果：username='admin', password包含 '$2a$10$'

SELECT '=== 数据修复完成 ===' AS '';
SELECT CONCAT('修复时间: ', NOW()) AS message;
