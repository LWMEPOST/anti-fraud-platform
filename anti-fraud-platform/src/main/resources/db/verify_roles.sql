-- ============================================
-- 验证数据库中的用户角色关联
-- ============================================

USE anti_fraud_platform;

-- 1. 查看所有用户
SELECT id, username, real_name, status FROM sys_user;

-- 2. 查看所有角色
SELECT id, role_name, role_code, status FROM sys_role;

-- 3. 查看用户角色关联
SELECT 
    u.id AS user_id,
    u.username,
    u.real_name,
    ur.role_id,
    r.role_name,
    r.role_code
FROM sys_user_role ur
JOIN sys_user u ON ur.user_id = u.id
JOIN sys_role r ON ur.role_id = r.id
ORDER BY u.id;

-- 4. 重新设置正确的用户角色关联（如果有问题）
-- 先删除旧的关联
DELETE FROM sys_user_role;

-- 重新插入正确的关联
INSERT INTO sys_user_role (user_id, role_id) VALUES 
(1, 1),  -- admin (id=1) -> 超级管理员 (role_id=1, ADMIN)
(2, 2),  -- teacher01 (id=2) -> 内容管理员 (role_id=2, CONTENT_ADMIN)
(3, 3);  -- student01 (id=3) -> 普通用户 (role_id=3, USER)

-- 5. 验证角色权限关联
SELECT 
    r.role_name,
    r.role_code,
    COUNT(rp.permission_id) AS permission_count
FROM sys_role_permission rp
JOIN sys_role r ON rp.role_id = r.id
GROUP BY r.role_name, r.role_code
ORDER BY r.id;

-- 6. 查看admin有多少权限
SELECT 
    rp.role_id,
    r.role_name,
    COUNT(*) AS admin_permissions
FROM sys_role_permission rp
JOIN sys_role r ON rp.role_id = r.id
WHERE rp.role_id = 1
GROUP BY rp.role_id, r.role_name;

-- 7. 查看USER角色有多少权限
SELECT 
    rp.role_id,
    r.role_name,
    COUNT(*) AS user_permissions
FROM sys_role_permission rp
JOIN sys_role r ON rp.role_id = r.id
WHERE rp.role_id = 3
GROUP BY rp.role_id, r.role_name;