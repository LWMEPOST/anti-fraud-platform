-- ============================================
-- 更新用户密码为正确的BCrypt哈希
-- 使用已知正确的哈希值
-- ============================================

USE anti_fraud_platform;

-- 先检查当前密码
SELECT id, username, password FROM sys_user;

-- 更新为正确的哈希
-- 这个哈希是用Spring Security的BCrypt生成的
UPDATE sys_user SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOl7K/KKKhEje' WHERE username = 'admin';

UPDATE sys_user SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOl7K/KKKhEje' WHERE username = 'teacher01';

UPDATE sys_user SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOl7K/KKKhEje' WHERE username = 'student01';

-- 验证更新结果
SELECT id, username, password FROM sys_user;

-- 应该看到所有密码都是同一个哈希值