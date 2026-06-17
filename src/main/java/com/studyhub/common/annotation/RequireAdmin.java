package com.studyhub.common.annotation;

import java.lang.annotation.*;

/**
 * 需要管理员权限的注解
 * 标记在 Controller 方法上，只有 ADMIN 角色才能访问
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireAdmin {
    String message() default "需要管理员权限";
}