package com.antifraud.common;

import lombok.Getter;

@Getter
public enum ErrorCode {
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "参数错误"),
    UNAUTHORIZED(401, "未认证"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    ERROR(500, "服务器内部错误"),

    USER_EXIST(1001, "用户名已存在"),
    USER_NOT_FOUND(1002, "用户不存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    USER_DISABLED(1004, "账号已被禁用"),
    TOKEN_EXPIRED(1005, "登录已过期"),
    TOKEN_INVALID(1006, "无效的Token"),

    ROLE_EXIST(2001, "角色已存在"),
    PERMISSION_EXIST(3001, "权限已存在"),

    CONTENT_AUDIT_FAIL(4001, "内容审核失败"),
    EXAM_NOT_FOUND(5001, "测试记录不存在"),
    REPORT_STATUS_ERROR(6001, "举报状态转换错误");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
