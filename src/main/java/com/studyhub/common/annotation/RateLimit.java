package com.studyhub.common.annotation;

import java.lang.annotation.*;

/**
 * 接口限流注解
 * 标记在 Controller 方法上，限制该接口的调用频率
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 限流 key，支持 SpEL 表达式，如 #noteId 表示取方法参数中的 noteId
     */
    String key() default "";

    /**
     * 时间窗口大小（秒），默认 60 秒
     */
    int window() default 60;

    /**
     * 时间窗口内最大请求次数，默认 10 次
     */
    int limit() default 10;

    /**
     * 超出限制时的提示信息
     */
    String message() default "请求过于频繁，请稍后再试";
}
